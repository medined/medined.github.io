---
layout: post
title: A HelloWorldServlet Generater - You pick the Eclipse Project Name
date: '2003-03-10T17:04:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-10T17:04:48.916-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90473889
blogger_orig_url: http://affy.blogspot.com/2003/03/example-helloworldservlet-generater.md
year: 2003
theme: java
---

After writing the HelloWorld servlet of my last post, I realized that it's usefulness was limited because people new to
servlet programming would not know what needed to be changed in order to use the project as a template. So, I've created
an Ant build.xml file to do the work for you.


Simply unzip my <a
    href="http://affy.blogspot.com/java/HelloWorldTemplateServlet.zip">http://affy.blogspot.com/java/HelloWorldTemplateServlet.zip</a>
file into a convenient directory. Then connect to it via a DOS prompt and type <code>ant</code>. You'll be prompted for
the Eclipse workspace directory (d:\java\workspace, for me) and the name of the project that you want to create. Try
<code>aaa</code> just to see what happens. Follow the directions in the README.txt file in the newly-created directory
and you should have the servlet runing in a short amount of time.