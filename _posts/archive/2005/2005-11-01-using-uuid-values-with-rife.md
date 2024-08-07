---
layout: post
title: Using UUID Values With RIFE CreateTable
date: '2005-11-01T21:11:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2005-11-01T21:17:37.273-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-113089785727166607
blogger_orig_url: http://affy.blogspot.com/2005/11/using-uuid-values-with-rife.md
year: 2005
theme: java
---

<p>As I mentioned in other entries, I like to use UUID values. The current version of RIFE doesn't handle UUID
  properties correctly. However, a few minutes digging into the source code showed me that the changes needed for
  support where trivial.</p>


<p>I created a com.uwyn.rife.database.types.databasedrivers package in my own project and then copied the
  org_hsqldb_jdbcDriver.java into it.Then I updated the getSqlType() method by added the highlighted code shown below
  immediately after the test for the character class.</p>

<pre>
  else if (type == java.util.UUID.class) {
    return "VARCHAR(36)";
  }
</pre>

<p>I'd love for this change to be propagated to the rest of the driver files and slipped into an upcoming release.</p>