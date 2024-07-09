---
layout: post
title: 'ACEGI: An Example of When to Use AffirmativeBased (instead of UnanimousBased)
Voting when Controlling Access to Methods'
date: '2005-10-25T10:37:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-07-05T12:07:40.153-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-113025110534915591
blogger_orig_url: http://affy.blogspot.com/2005/10/acegi-example-of-when-to-use.md
year: 2005
theme: java
---

<p>In this example I have a simple interface, defined below, which needs role-based access control.</p>


<pre>
public interface IBean {
  <i>Workers and Managers can get the value.</i>
  public String getValue();
  <i>Only Managers can set the value.</i>
  public void setValue(String _value);
}
</pre>

<p>The access control is specified via a security interceptor like this:</p>

<pre>
  &lt;bean id='securityInterceptor' class='...MethodSecurityInterceptor'>
    ...
    &lt;property name='objectDefinitionSource'&gt;
      &lt;value&gt;
        com.affy.IBean.getValue=ROLE_WORKER,ROLE_MANAGER
        com.affy.IBean.setValue=ROLE_MANAGER
      &lt;/value&gt;
    &lt;/property&gt;
&lt;/bean&gt;
</pre>

<p>Since the the <i>getValue</i> method has more than one role associated with it, the type of voter used as the
  accessDecisionManager bean is important. If you choose <i>UnanimousBased</i> then the user must have both ROLE_WORKER
  and ROLE_MANAGER roles which is probably not what your security officer wants.</p>

<p>Using the <i>AffirmativeBased</i> voter means that the user only needs one of the roles to be able to execute the
  <i>getValue</i> method.</p>