---
layout: post
title: An Example Using NamingEnumeration to Find JBOSS JNDI Names
date: '2003-01-09T15:42:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2007-11-21T13:41:16.936-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90164222
blogger_orig_url: http://affy.blogspot.com/2003/01/example-using-namingenumeration-to.md
year: 2003
theme: java
---

An Example Using NamingEnumeration to Find JBOSS JNDI Names


<pre>
  Hashtable ht = new Hashtable();
  ht.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
  ht.put(Context.PROVIDER_URL, "localhost");
  ht.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
  Context lContext = new InitialContext(ht);
  String a = "java:/";
  NamingEnumeration ne = lContext.list(a);
  while (ne.hasMore()) {
    System.out.println("A: " + ne.next().toString());
  }
</pre>