---
layout: post
title: Per-User Connection Pooling for Spring, Hibernate and Oracle VPD (Virtual Private Database)
date: '2006-12-22T09:52:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-12-22T09:59:56.900-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116679959339199467
blogger_orig_url: http://affy.blogspot.com/2006/12/per-user-connection-pooling-for-spring.md
year: 2006
theme: java
---

<p>There are several forum messages about how to handle per-user connection pooling but none of them clearly state what
  works. It is quite simple. Here is part of my applicationContext.xml.</p>


<pre>&lt;!-- this datasource must hande per-user connection pooling --&gt;
&lt;bean id="targetDataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource" destroy-method="close"&gt;
  &lt;property name="driverClassName" value="oracle.jdbc.driver.OracleDriver"/&gt;
  &lt;property name="url" value="jdbc:oracle:thin:@pooz:1521:ACC"/&gt;
&lt;/bean&gt;

&lt;!-- this object wraps the original datasource allows change of user when needed --&gt;
&lt;bean id="dataSource" class="org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter"&gt;
  &lt;property name="targetDataSource" ref="targetDataSource"/&gt;
  &lt;property name="username"&gt;&lt;value&gt;unknown&lt;/value&gt;&lt;/property&gt;
  &lt;property name="password"&gt;&lt;value&gt;unknown&lt;/value&gt;&lt;/property&gt;
&lt;/bean&gt;

&lt;!-- this bean does real work, inject the user adapter so it can change the user credentials --&gt;
&lt;bean id="create" class="com.codebits.dao.hibernate.actions.Create"&gt;
  &lt;property name="sessionFactory" ref="sessionFactory" /&gt;
  &lt;property name="userAdapter" ref="dataSource" /&gt;
&lt;/bean&gt;</pre>
<p>The Java code is also simple, just before you grab the Hibernate session, do the following:</p>
<pre>  userAdapter.setCredentialsForCurrentThread("test", "test");</pre>