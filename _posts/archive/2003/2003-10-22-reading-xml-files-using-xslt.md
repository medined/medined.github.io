---
layout: post
title: Reading XML Files Using XSLT
date: '2003-10-22T01:48:00.000-04:00'
author: David Medinets
categories:
- "[[xml]]"
- "[[xslt]]"
modified_time: '2003-10-22T01:48:48.946-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-106640474657948077
blogger_orig_url: http://affy.blogspot.com/2003/10/reading-xml-files-using-xslt.md
year: 2003
---

I'm learning how to represent non-English data in XML and ran into an article titled "Well-structured XML Goes
Cosmopolitan" written by Ilari Aarnio and hosted by DevX.com. Buried in the article was an interesting technique that
showed how to read XML documents from within XSLT.



&lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
&lt;xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://www.w3.org/1999/xhtml"&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:param name="currLang"&gt;en&lt;/xsl:param&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:output method="xml" encoding="UTF-8" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0
Strict//EN"/&gt;<br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:template match="/"&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:variable name="cvData"&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:text&gt;cv-&lt;/xsl:text&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:value-of select="$currLang"/&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:text&gt;.xml&lt;/xsl:text&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsl:variable&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsl:apply-templates select="document($cvData)"/&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsl:template&gt;<br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;!-- add normal XSLT processing here --&gt;<br>
<br>
&lt;/xsl:stylesheet&gt;<br>