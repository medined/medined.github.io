---
layout: post
title: Using synchronized in Java
date: '2003-10-14T03:05:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-10-14T03:05:22.470-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-106611512235169284
blogger_orig_url: http://affy.blogspot.com/2003/10/using-synchronized-in-java.md
year: 2003
theme: java
---

For quite awhile, I've been using the sychnronized keyword in an incorrect manner, probably because of my ColdFusion
background. Synchronization locks in ColdFusion lock access to a block of code based on some arbitrary lock phrase. As
long as two locks have the same lock phrase, they single-thread the process through the protected code.


The concept that I recently relearned was that Java sychronizes on an object - typicallly 'this'. Which has some
interesting ramifications when one object starts two threads which both refer to static objects. The parent can have
every method sychronized without affected the child threads.

The above issue is relative mundane, but my blindness to the true nature of Java synchronization shows how important it
is to constantly reexam even your most basic understandings about the tools that you use everyday.