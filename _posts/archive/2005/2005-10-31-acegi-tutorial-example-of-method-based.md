---
layout: post
title: 'ACEGI Tutorial: An Example of Method-based Access Control and JUnit for Testing'
date: '2005-10-31T11:17:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-05-10T21:44:43.370-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-113077569049172784
blogger_orig_url: http://affy.blogspot.com/2005/10/acegi-tutorial-example-of-method-based.md
year: 2005
theme: java
---

<p>This ACEGI tutorial shows how to implement the following security requirements for the BookBean class:
<p>



<p><b>NOTE: Updated 2006, May 10 - changed net.sf to org in package names. And updated code to work with ACEGI v1.0
    RC2</b></p>
<ul>
  <li>Only MANAGER users can replace the existing value (ie, call setValue)</li>
  <li>Only MANAGER users and WORKER users can change the value (ie, call changeValue)</li>
  <li>Any user (ie, someone with no roles) can view the value (ie, call getValue)</li>
</ul>

<p>The BookBean class is extremely simple as shown by its interface:</p>
<pre>
<b>/* BookBean.java */</b>
package com.affy;

public interface BookBean {
    public int getValue();
    public void setValue(int _value);
    public void changeValue(int _value);
}</pre>
<p>Likewise, the implementation of this interface is simple:</p>
<pre>
<b>/* BookBeanImpl.java */</b>
package com.affy;

public class BookBeanImpl implements BookBean {

    private int value = 0;

    public BookBeanImpl() {
        super();
    }

    public int getValue() {
        return this.value;
    }

    // replace the value.
    public void setValue(int _value) {
        this.value = _value;
    }

    // change the value.
    public void changeValue(int _value) {
        this.value += _value;
    }

}
</pre>

<p>Now that we've seen the interface and the implementation, we can look at how a Spring configuration file is used to
  provide declarative access control. I'll note at this point that while the
  user information (ie, names, passwords, etc...) is hard-coded in the configuration file you can also use a database,
  ldap, or whatever persistent storage mechanisn you prefer.</p>
<p>I find it hard to glean useful information when I read XML configuration files; I suspect it's a learned skill.
  However, the configuration file for this example is only 56 lines of code - much of which can be considered
  boilerplate.</p>
<pre>
&lt;?xml version='1.0' encoding='UTF-8'?&gt;
&lt;!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' 'http://www.springframework.org/dtd/spring-beans.dtd'&gt;
&lt;beans&gt;

<b>&lt;-- spring.xml --&gt;</b>

  <b>&lt;-- This is the bean that needs to be protected. --&gt;</b>
  &lt;bean id='bookBean' class='<b>com.affy.BookBeanImpl</b>'/&gt;

  <b>&lt;-- This bean defines a proxy for the protected bean. Notice that --&gt;</b>
  <b>&lt;-- the <i>id</i> defined above is specified. When an application asks Spring --&gt;</b>
  <b>&lt;-- for a <i>bookBean</i> it will get this proxy instead. --&gt;</b>
  &lt;bean id='autoProxyCreator' class='org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator'&gt;
    &lt;property name='interceptorNames'&gt;
      &lt;list&gt;&lt;value&gt;securityInterceptor&lt;/value&gt;&lt;/list&gt;
    &lt;/property&gt;
    &lt;property name='beanNames'&gt;
      &lt;list&gt;&lt;value&gt;<b>bookBean</b>&lt;/value&gt;&lt;/list&gt;
    &lt;/property&gt;
  &lt;/bean&gt;

  <b>&lt;-- This bean specifies which roles are authorized to execute which methods. --&gt;</b>
  &lt;bean id='securityInterceptor' class='org.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor'&gt;
    &lt;property name='authenticationManager' ref='authenticationManager'/&gt;
    &lt;property name='accessDecisionManager' ref='accessDecisionManager'/&gt;
    &lt;property name='objectDefinitionSource'&gt;
      &lt;value&gt;<b>
        com.affy.BookBean.setValue=ROLE_MANAGER
        com.affy.BookBean.changeValue=ROLE_WORKER,ROLE_MANAGER
      </b>&lt;/value&gt;
    &lt;/property&gt;
  &lt;/bean&gt;

  <b>&lt;-- This bean specifies which roles are assigned to each user. You'll notice  --&gt;</b>
  <b>&lt;-- that I'm using an in-memory database implementation instead of using  --&gt;</b>
  <b>&lt;-- LDAP or a 'real' database. The ACEGI-provided in-memory implementation is great for testing! --&gt;</b>
  &lt;bean id='userDetailsService' class='org.acegisecurity.userdetails.memory.InMemoryDaoImpl'&gt;
    &lt;property name='userMap'&gt;
      &lt;value&gt;<b>
        manager=manager,ROLE_MANAGER
        worker=worker,ROLE_WORKER
        anonymous=anonymous,
        disabled=disabled,disabled,ROLE_WORKER
      </b>&lt;/value&gt;
    &lt;/property&gt;
  &lt;/bean&gt;

  <b>&lt;-- This bean specifies that a user can access the protected methods --&gt;</b>
  <b>&lt;-- if they have any one of the roles specified in the <i>objectDefinitionSource</i> above. --&gt;</b>
  &lt;bean id='accessDecisionManager' class='org.acegisecurity.vote.AffirmativeBased'&gt;
    &lt;property name='decisionVoters'&gt;
      &lt;list&gt;&lt;ref bean='roleVoter'/&gt;&lt;/list&gt;
    &lt;/property&gt;
  &lt;/bean&gt;

  <b>&lt;-- The next three beans are boilerplate. They should be the same for nearly all applications. --&gt;</b>
  &lt;bean id='authenticationManager' class='org.acegisecurity.providers.ProviderManager'&gt;
    &lt;property name='providers'&gt;
      &lt;list&gt;&lt;ref bean='authenticationProvider'/&gt;&lt;/list&gt;
    &lt;/property&gt;
  &lt;/bean&gt;

  &lt;bean id='authenticationProvider' class='org.acegisecurity.providers.dao.DaoAuthenticationProvider'&gt;
    &lt;property name='userDetailsService' ref='userDetailsService'/&gt;
  &lt;/bean&gt;

  &lt;bean id='roleVoter' class='org.acegisecurity.vote.RoleVoter'/&gt;

&lt;/beans&gt;
</pre>
<p>So we've seen the interface, the implementation, and the Spring configuration file. The last file shows how to unit
  test the access control. One important thing to note is that the

  SecuriyContextHolder is a static object. So while the approach shown below is valid for an single-thread application
  which can change its user context as needed, there are definitely some

  synchronization issues that need to be addressed for multi-threaded applications.</p>
<pre>
<b>// MethodAclTest.java</b>
package com.affy;

import junit.framework.TestCase;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.DisabledException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MethodAclTest extends TestCase {

    // Read the Spring configuration file. I typically create a directiory called <i>config</i> which
    // gets added to my classpath.
    private static ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

    private static void createSecureContext(final ApplicationContext ctx, final String username, final String password) {
        AuthenticationProvider provider = (AuthenticationProvider) ctx.getBean("authenticationProvider");
        Authentication auth = provider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // Clear the security context after each test.
    public void teardown() {
        SecurityContextHolder.setContext(new SecurityContextImpl());
    }

    // There are three methods with three roles Which means that I need
    // nine tests.

    ///////////////////
    // setValue tests.
    ///////////////////

    public void testManagerAccessForSet() {
        createSecureContext(ctx, "manager", "manager");
        ((BookBean) ctx.getBean("bookBean")).setValue(100);
    }

    public void testWorkerAccessForSet() {
        createSecureContext(ctx, "worker", "worker");
        try {
            ((BookBean) ctx.getBean("bookBean")).setValue(100);
            fail("Expected AccessDeniedException.");
        } catch (AccessDeniedException e) {
            // do nothing.
        }
    }

    public void testAnonymousAccessForSet() {
        createSecureContext(ctx, "anonymous", "anonymous");
        try {
            ((BookBean) ctx.getBean("bookBean")).setValue(100);
            fail("Expected AccessDeniedException.");
        } catch (AccessDeniedException e) {
            // do nothing.
        }
    }

    ///////////////////
    // changeValue tests.
    ///////////////////
    public void testManagerAccessForChange() {
        createSecureContext(ctx, "manager", "manager");
        ((BookBean) ctx.getBean("bookBean")).changeValue(100);
    }

    public void testWorkerAccessForChange() {
        createSecureContext(ctx, "worker", "worker");
        ((BookBean) ctx.getBean("bookBean")).changeValue(100);
    }

    public void testAnonymousAccessForChange() {
        createSecureContext(ctx, "anonymous", "anonymous");
        try {
            ((BookBean) ctx.getBean("bookBean")).changeValue(100);
            fail("Expected AccessDeniedException.");
        } catch (AccessDeniedException e) {
            // do nothing.
        }
    }

    ///////////////////
    // getValue tests.
    ///////////////////
    public void testManagerAccessForGet() {
        createSecureContext(ctx, "manager", "manager");
        ((BookBean) ctx.getBean("bookBean")).getValue();
    }

    public void testWorkerAccessForGet() {
        createSecureContext(ctx, "worker", "worker");
        ((BookBean) ctx.getBean("bookBean")).getValue();
    }

    public void testAnonymousAccessForGet() {
        createSecureContext(ctx, "anonymous", "anonymous");
        ((BookBean) ctx.getBean("bookBean")).getValue();
    }

    ///////////////////
    // disabled user
    ///////////////////
    public void testDisabledUser() {
        try {
            createSecureContext(ctx, "disabled", "disabled");
            fail("Expected DisabledException.");
        } catch (DisabledException e) {
            // do nothing.
        }
    }

    ///////////////////
    // unknown user
    ///////////////////
    public void testUnknownUser() {
        try {
            createSecureContext(ctx, "unknown", "unknown");
            fail("Expected BadCredentialsException.");
        } catch (BadCredentialsException e) {
            // do nothing.
        }
    }

}
</pre>
<p>In conclusion, once I created my own example the ideas and classes used by ACEGI became quite clear and easy to use.
  I will definitely look to ACEGI in future applications.</p>