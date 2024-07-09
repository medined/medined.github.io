---
layout: post
title: How to Unset or Undefine Environment Variables Using Perl
date: '2003-03-01T11:04:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-01T11:04:09.263-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90392924
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-to-unset-or-undefine.md
year: 2003
theme: perl
---

How to Unset or Undefine Environment Variables Using Perl


<p>Environment variables are stored as entries in the <code>%ENV</code> hash. Therefore you can use the <code>delete()</code> function to unset them. Sometimes unsetting environment variables is usefull to control the environment of a child process.</p>
