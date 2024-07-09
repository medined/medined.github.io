---
layout: post
title: How Do I Prevent a Page From Caching in the Browser Using ColdFusion?
date: '2003-03-01T12:23:00.000-05:00'
author: David Medinets
categories:
- "[[coldusion]]"
modified_time: '2003-03-04T01:36:12.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90393131
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-do-i-prevent-page-from.md
year: 2003
---

How Do I Prevent a Page From Caching in the Browser Using ColdFusion?


<p>The following code should work in most situations. This code is courtesy of Cameron Childress).</p>
<pre>
&lt;!--- Anti-cache ---&gt;
&lt;CFSET gmts = gettimezoneinfo()&gt;
&lt;CFSET gmt = gmts.utcHourOffset&gt;
&lt;CFIF gmt EQ 0&gt;
  &lt;CFSET gmt = ""&gt;
&lt;CFELSEIF gmt GT 0&gt;
  &lt;CFSET gmt = "+" & gmt &gt;
&lt;/CFIF&gt;

&lt;CFHEADER NAME="Expires" VALUE="Mon, 06 Jan 1990 00:00:01 GMT"&gt;
&lt;CFHEADER NAME="Pragma" VALUE="no-cache"&gt;
&lt;CFHEADER NAME="cache-control" VALUE="no-cache, must-revalidate"&gt;
&lt;CFHEADER NAME="Last-Modified"
  VALUE="#dateformat(now(), 'ddd, dd mmm yyyy')# #timeformat(now(), 'HH:mm:ss')# GMT#gmt#"&gt;
</pre>
