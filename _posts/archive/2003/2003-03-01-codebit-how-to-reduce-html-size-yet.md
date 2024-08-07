---
layout: post
title: How to Reduce HTML Size Yet Still Use Long Style Names Using ColdFusion
date: '2003-03-01T13:30:00.000-05:00'
author: David Medinets
categories:
- "[[coldfusion]]"
modified_time: '2003-03-01T22:11:28.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90393352
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-to-reduce-html-size-yet.md
year: 2003
---

How to Reduce HTML Size Yet Still Use Long Style Names Using ColdFusion


<p>Here's a suggestion for dealing with styles donated by Steve Runyon.</p>
<p>Long style names, like "tabCellSelected" or "tabCellUnselected", cause HTML page sizes to grow. For example, if you have a table with 8 columns and 20 rows, with 1 column selected, you're expending 2680 bytes on the names of the styles.  ((7 * 20 * 17) + (1 * 20 * 15) = 2680)</p>
<p>The page size can be reduced by creating (application?) variables named like the style, whose values are short placeholders:</p>
<pre>
&lt;cfset application.TabCellSelected_sty = "s001"&gt;
&lt;cfset application.TabCellUnselected_sty = "s002"&gt;
</pre>
<p>Your style definitions then look like this:</p>
<pre>
TD.#application.TabCellSelected_sty# {
	[etc]
}
TD.#application.TabCellUnselected_sty# {
	[etc]
}
</pre>
<p>And your generated html looks like this:</p>
<pre>
&lt;td class=s001&gt;data&lt;/td&gt;
&lt;td class=s002&gt;data&lt;/td&gt;
</pre>
<p>On that same 8x20 table, you're now using only 640 bytes for the styles.  (8 * 20 * 4)</p>
