---
layout: post
title: Using External Jar Files In Your Custom Ant Task Classpath.
date: '2004-01-19T18:10:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
modified_time: '2004-01-19T18:15:19.543-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-107455380453342078
blogger_orig_url: http://affy.blogspot.com/2004/01/using-external-jar-files-in-your.md
year: 2004
theme: java
---

It took me a while to figure out that I could specify several items in the <code>classpath</code> attribute of the
<code>taskdef</code> tag in my build.xml file.


Here is one example:
<pre>
    &lt;taskdef name="CreatePartition"
        classname="org.wwre.ant.tasks.xis.CreatePartition"
        classpath="build/classes;${xis.client.jar};${log4j.jar}"
    /&gt;
</pre>