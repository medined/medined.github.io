---
layout: post
title: Fixing the "No Java compiler available" Error When Running Embedded Tomcat
date: '2006-10-31T02:00:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2008-01-08T10:40:44.666-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116227806001428291
blogger_orig_url: http://affy.blogspot.com/2006/10/fixing-no-java-compiler-available.md
year: 2006
theme: java
---

I created an Eclipse project which runs an embedded version of Tomcat. More about that in a future post. I ran into the
following message when I tried to serve a JSP page:
java.lang.IllegalStateException: No Java compiler available


I downloaded the Tomcat source (gotta love open source!) and found the following lines of code:

<pre>
  jspCompiler = null;
  if (options.getCompiler() == null) {
    jspCompiler = createCompiler("org.apache.jasper.compiler.JDTCompiler");
    if (jspCompiler == null) {
      jspCompiler = createCompiler("org.apache.jasper.compiler.AntCompiler");
    }
  }
</pre>

I did a little digging and found that several jar files where not in my classpath:

jasper-compiler.jar
jasper-compiler-jdt.jar