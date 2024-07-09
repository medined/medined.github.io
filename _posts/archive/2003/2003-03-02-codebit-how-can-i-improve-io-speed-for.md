---
layout: post
title: How Can I Improve the I/O Speed for System.out.println Using Java?
date: '2003-03-02T11:19:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-02T11:19:09.086-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90395778
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-can-i-improve-io-speed-for.md
year: 2003
theme: java
---

Normally, the System.out print stream has a buffer size of 128 and flushes the buffer whenever a newline character is
encountered.


The following four lines of java change the buffer size to 1024 and doesn't flush the buffer for newline characters.

<pre>
FileOutputStream fdout = new FileOutputStream(FileDescriptor.out);
BufferedOutputStream bos = new BufferedOutputStream(fdout, 1024);
PrintStream ps = new PrintStream(bos, false);
System.setOut(ps);
</pre>