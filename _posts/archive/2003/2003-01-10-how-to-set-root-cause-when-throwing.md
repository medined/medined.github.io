---
layout: post
title: How to set the root cause when throwing an exception
date: '2003-01-10T18:23:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2007-11-21T13:41:08.878-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90169379
blogger_orig_url: http://affy.blogspot.com/2003/01/how-to-set-root-cause-when-throwing.md
year: 2003
theme: java
---

How to set the root cause when throwing an exception


<pre>
NamingException ne = new NamingException("foo bar blah blah");
ne.setRootCause(e);
throw ne;
</pre>