---
layout: post
title: An Example of Object Persistence (or Prevalence) Using Prevayler
date: '2003-03-04T01:31:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-04T01:38:01.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90403417
blogger_orig_url: http://affy.blogspot.com/2003/03/example-of-object-persistence-or.md
year: 2003
theme: java
---

Several years ago, while I was working for Toysrus.com I created an In-Memory database of their inventory items using
ColdFusion. There were about 30,000 parts and ColdFusion was easily able to handle that much memory in their structures.
The time-consuming ascept of that project was developing the code to read the information from the database into memory.
Today, in Java world, Object Prevalance, makes the same task much easier.


<p>The home page for the Object Prevalence system that I use is <a
        href="http://www.prevayler.org/wiki.jsp?topic=StartingPoints">http://www.prevayler.org/wiki.jsp?topic=StartingPoints</a>.
    I won't spend any time describing the concepts, but you can read about them there. I first started looking at Object
    Persistence technologies because I wanted to write a JarSearcher application. I could search the store for any
    class. Frequently when I'm compiling someone else's code, I find class references that I'm unfamilar with and don't
    know which Jar file the class is in. My JarSearcher would pre-search Jar files so that I could quickly find the
    class that I need. The application works in two steps. First, .jar files are indexed. And then class names are found
    as needed. You'll find my source code at <a href="http://affy.blogspot.com/JarSearcher.zip">JarSearcher.zip</a>. The
    docomentation is rather sparse for now. The js.bat file shows how to invoke the application. The java_projects.txt
    file shows how to create a script for the application to follow. Please send comments to myself at
    medined@mtolive.com. After you send email, you'll be asked to verify that you're human (instead of a spammer) by
    SpamArrest.</p>