---
layout: post
title: 'Spring; ApplicationContext, SqlUpdate: Configuring SqlUpdate Inside ApplicationContext.'
date: '2006-06-19T19:23:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-06-19T19:27:33.676-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-115075962609393972
blogger_orig_url: http://affy.blogspot.com/2006/06/spring-applicationcontext-sqlupdate.md
year: 2006
theme: java
---

<p>Configuring SqlUpdate and derived classes within the applicationContext.xml file is not straightforward because the
  class's constructor requires an integer array when a prepared statement is used. I got around this issue by using a
  factory class and method.</p>


<p>We'll start with the object that we actually need. The communicationsUpdate bean:</p>

<pre>&lt;bean id="communicationsUpdate" factory-bean="communicationsUpdateFactory" factory-method="createInstance"/&gt;</pre>

<p>.The Java class for this bean is nearly trivial:</p>

<pre>public class CommunicationsUpdate extends SqlUpdate {
  public int run(final String communication_type, final int source_organization_id, final int destination_organization_id, final int address_to_person_id, final int sent_by_person_id, final int order_id, final int dsid) {
    Object[] params = new Object[] { communication_type, new Integer(source_organization_id), new Integer(destination_organization_id), new Integer(address_to_person_id), new Integer(sent_by_person_id), new Integer(order_id), new Integer(dsid) };
    return update(params);
  }
}</pre>

<p>The run method's parameter includes every field that needs to be updated. It creates an object array and then passes
  the object array into the parent's update method (SqlUpdate is the parent class).</p>

<p>Next we turn our attention to the factory class. Here is the bean description:</p>

<pre>&lt;bean id="communicationsUpdateFactory" class="com.codebits.dao.CommunicationsUpdateFactory"&gt;
  &lt;property name="dataSource" ref="dataSource" /&gt;
  &lt;property name="sql" value="UPDATE communications SET communication_type=?,source_organization_id=?,destination_organization_id=?,address_to_person_id=?,sent_by_person_id=?,order_id=? WHERE dsid=?" /&gt;
  &lt;property name="parameters"&gt;
    &lt;list&gt;
      &lt;ref bean="communicationType_type"/&gt;
      &lt;ref bean="sourceOrganizationId_type"/&gt;
      &lt;ref bean="destinationOrganizationId_type"/&gt;
      &lt;ref bean="addresstoPersonId_type"/&gt;
      &lt;ref bean="sendByPersonId_type"/&gt;
      &lt;ref bean="orderId_type"/&gt;
      &lt;ref bean="dsId_type"/&gt;
    &lt;/list&gt;
  &lt;/property&gt;
&lt;/bean&gt;</pre>

<p>You'll notice that the SQL for the update is specified right in the bean definition. As are the parameters for the
  prepared statement. The Java source behind the factory is quite generic. It looks like this:</p>

<pre>public class CommunicationsUpdateFactory {

  private DataSource dataSource = null;

  private String sql = null;

  private List parameters = null;

  public CommunicationsUpdate createInstance() {
    CommunicationsUpdate action = new CommunicationsUpdate();
    action.setDataSource(getDataSource());
    action.setSql(getSql());
    for (Iterator iterator = parameters.iterator(); iterator.hasNext(); ) {
      action.declareParameter((SqlParameter)iterator.next());
    }
    action.compile();
      return action;
  }
  ... standard getters and setters ...
}</pre>

<p>The SQLParameters are also defined in the applicationContext.xml file:</p>

<pre>&lt;bean id="communicationType_type" class="com.codebits.dao.sqlparameter.CommunicationType"/&gt;
&lt;bean id="sourceOrganizationId_type" class="com.codebits.dao.sqlparameter.SourceOrganizationId"/&gt;
&lt;bean id="destinationOrganizationId_type" class="com.codebits.dao.sqlparameter.DestinationOrganizationId"/&gt;
&lt;bean id="addresstoPersonId_type" class="com.codebits.dao.sqlparameter.AddressToPersonId"/&gt;
&lt;bean id="sendByPersonId_type" class="com.codebits.dao.sqlparameter.SentByPersonId"/&gt;
&lt;bean id="orderId_type" class="com.codebits.dao.sqlparameter.OrderId"/&gt;
&lt;bean id="dsId_type" class="com.codebits.dao.sqlparameter.DsId"/&gt;</pre>

<p>They all look the same except that the Types constant may change.</p>

<pre>public class CommunicationType extends SqlParameter {

  public CommunicationType() { super(Types.VARCHAR); }

}</pre>

<p>I posted this example as much to demonstrate how a factory method can be
  defined as to show how the SqlUpdate class is used. I'm not sure how
  popular SqlUdate is in this era of Hibernate!</p>