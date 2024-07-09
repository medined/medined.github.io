---
layout: post
title: java.lang.VerifyError &amp; Sonic XIS Excelon
date: '2004-01-07T13:09:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-01-07T13:09:40.860-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-107349896106741678
blogger_orig_url: http://affy.blogspot.com/2004/01/javalangverifyerror-sonic-xis-excelon.md
year: 2004
theme: java
---

The other day I ran into a java.lang.VerifyError message while using the Sonic Excelon API. The problem arose because
the dxeclient.jar file has classes that overlap those in xercesImpl.jar. I removed the xerces library from my classpath
and the code worked.