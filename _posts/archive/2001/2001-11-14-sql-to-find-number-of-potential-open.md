---
layout: post
title:
date: '2001-11-14T12:46:00.000-05:00'
author: David Medinets
categories:
- "[[oracle]]"
modified_time: '2007-11-21T12:08:03.723-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7118932
blogger_orig_url: http://affy.blogspot.com/2001/11/sql-to-find-number-of-potential-open.md
year: 2001
---

SQL to find the number of potential open cursors in Oracle: select value,name from v$parameter where name like
'open_cursors'