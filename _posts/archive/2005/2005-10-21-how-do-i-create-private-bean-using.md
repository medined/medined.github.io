---
layout: post
title: How Do I Create a Private Bean Using ACEGI?
date: '2005-10-21T21:17:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-01-16T21:47:06.416-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-112994398577183395
blogger_orig_url: http://affy.blogspot.com/2005/10/how-do-i-create-private-bean-using.md
year: 2005
theme: java
---

<p>Following the directions in the Spring In Action book, I created a small application (only three Java files and two
  configuration files) so that I could experiment with method-level access control using ACEGI.</p>


<p>However, I continously saw a mysterious message in my log:</p>
<pre>Public object - authentication not attempted</pre>
<p>So naturally I started looking around the Internet for information about creating private objects. Sadly, there was
  none. Being stubborn, I downloaded the ACEGI source code to start poking around.</p>

<p>After an hour or so of adding logging messages and tracing the code, I saw the following message in my logging of the
  MethodDefinitionMap.lookupAttributes method:</p>
<pre>
...MethodDefinitionMap; this: {...public java.lang.String
com.affy.BeanA.getValue()=[ROLE_FIELD_OPS, ROLE_DIRECTORY,
ROLE_PRESIDENT]}

...MethodDefinitionMap; method: public abstract java.lang.String
com.affy.IBeanA.getValue()
</pre>
<p>At first I thought the problem lay in the <i>abstract</i> keyword. But then I realized that my Spring configuration
  file contained:</p>
<pre>
  &lt;bean id='securityInterceptor' class='...MethodSecurityInterceptor'&gt;
    ...
    &lt;property name='objectDefinitionSource'&gt;
      &lt;value&gt;
        com.affy.BeanA.setValue=ROLE_PRESIDENT
        com.affy.BeanA.getValue=ROLE_FIELD_OPS,ROLE_DIRECTORY,ROLE_PRESIDENT
      &lt;/value&gt;
    &lt;/property&gt;
 &lt;/bean&gt;
</pre>
<p>The object definition specified the BeanA implementation instead of the Interface. Once I changed to use
  com.affy.IBean I saw the following message:</p>
<pre>
...AbstractSecurityInterceptor - Secure object: invocation: method
'setValue', arguments [FOOBAR]; target is of class [com.affy.BeanA];
ConfigAttributes: [ROLE_PRESIDENT]
</pre>
<p>Now the newly created object was private!</p>
<p><b>CONCLUSION</b> Use Interfaces in the <b>objectDefinitionSource</b> specification.</p>