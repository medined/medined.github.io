---
layout: post
title: Embedded Tomcat; Fixing the 'No Java Compiler available' error.
date: '2006-10-31T10:23:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-11-16T09:29:33.730-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116230820353748985
blogger_orig_url: http://affy.blogspot.com/2006/10/embedded-tomcat-fixing-no-java.md
year: 2006
theme: java
---

I am investigating the use of embedded Tomcat in one of my projects. During my testing, I ran into an error message:
<pre>No Java compiler available</pre> I was unable to find significant information about this message via Google so I
downloaded the Tomcat source and poked around. To make a short story shorter, include the following jar files to fix
this issue:
<pre>TOMCAT_HOME\common\lib\jasper-compiler.jar
TOMCAT_HOME\common\lib\jasper-compiler-jdt.jar</pre>