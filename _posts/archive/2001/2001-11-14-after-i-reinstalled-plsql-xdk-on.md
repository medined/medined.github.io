---
layout: post
title: Java Oracle Stored Procedure Processing XML
date: '2001-11-14T20:10:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
- "[[oracle]]"
modified_time: '2007-11-21T12:08:22.204-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7129854
blogger_orig_url: http://affy.blogspot.com/2001/11/after-i-reinstalled-plsql-xdk-on.md
year: 2001
theme: java
---

After I reinstalled the plsql xdk on Oracle 8i, my jdbc stored procedure started working again.


Now I can perform some additional time studies. My technique is flexible since I'm passing an xml packet to the procedure. The procedure loops over all of the attributes in the packet and inserts each one into the data repository. My xml looks like this:
<pre>
&lt;r session="sess01" updator="medined" object="1005769145473"&gt;
 &lt;f n="PMA_APPROVAL_MEANS" v="Identicality Per FAR 21.303(c)"/&gt;
 &lt;f n="PMA_MANUFACTURER" v="Boeing"/&gt;
&lt;/r&gt;
</pre>

Each <i>f</i> tag defines one fact, or attribute about an object. The object attribute of the <i>r</i> tag links all of the attributes together. The session attribute lets me know which objects were loaded together.
