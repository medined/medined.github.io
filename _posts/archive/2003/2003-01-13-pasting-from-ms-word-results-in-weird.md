---
layout: post
title: Pasting From MS Word Results in Weird Characters
date: '2003-01-13T11:24:00.000-05:00'
author: David Medinets
modified_time: '2003-01-13T11:24:44.700-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90177859
blogger_orig_url: http://affy.blogspot.com/2003/01/pasting-from-ms-word-results-in-weird.md
year: 2003
categories:
- "[[coldfusion]]"
---

Pasting From MS Word Results in Weird Characters



The following fixes the problem for the ColdFusion server (from Cameron Childress). Add these lines to the application.cfm:
<pre>
&lt;cfprocessingdirective pageencoding="iso-8859-1"&gt;
&lt;cfset setEncoding("form","iso-8859-1")&gt;
&lt;cfset setEncoding("URL","iso-8859-1")&gt;
&lt;cfcontent type="text/html; charset=iso-8859-1"&gt;
</pre>
