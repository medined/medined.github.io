---
layout: post
title: How to Find the Parent Frame of a Component Using Java
date: '2003-03-01T22:17:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-01T22:17:10.430-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90394577
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-to-find-parent-frame-of.md
year: 2003
theme: java
---

How to Find the Parent Frame of a Component Using Java


<PRE>
Frame getParentFrame() {
  Component p = this;

  while ( (p = p.getParent()) != null && !(p instanceof Frame) )
    ;
  return( (Frame)p );
}
</PRE>
