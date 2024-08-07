---
layout: post
title: Getting Hibernate to Create Schema Creation SQL
date: '2006-04-23T05:06:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-04-23T05:07:20.366-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-114578323985788336
blogger_orig_url: http://affy.blogspot.com/2006/04/getting-hibernate-to-create-schema.md
year: 2006
theme: java
---

I've seen some webpages that describe the SchemaExport Ant task. But it did not work when I tried to use it. The
documentation for it was sparse.



In any case, I traced through the underlying Hibernate code and found out that you can generate the schema creation SQL
with just three lines of code:

<pre>
package com.codebits;

import java.io.File;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.PostgreSQLDialect;

public class Play {

 public static void main(String[] args) {
  Configuration cfg = new Configuration();
  cfg.addDirectory(new File("config"));
  String[] lines = cfg.generateSchemaCreationScript(new PostgreSQLDialect());

  for (int i = 0; i < lines.length; i++) {
   System.out.println(lines[i] + ";");
  }
 }

}
</pre>

<p>Place your .hbm.xml files into some directory (I called mine config) and then execute the above class. Your schema
    creation script will be displayed on the console.</p>