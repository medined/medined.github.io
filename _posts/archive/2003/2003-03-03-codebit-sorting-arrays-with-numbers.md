---
layout: post
title: Sorting Arrays With Numbers Using Perl
date: '2003-03-03T20:29:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-03T20:29:25.626-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90402452
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-sorting-arrays-with-numbers.md
year: 2003
theme: perl
---

Sorting Arrays With Numbers Using Perl


<p>If you are looking to use the sort method to sort an array which contains numbers, the regular
   <code>sort @array</code> technique won't do you much good.</p>
<P>To sort an array, do the following:</p>
<pre>
# from least to greatest
foreach$number(sort { $a &lt;=&gt; $b } @numbers) {
   print $number\n";
}
# from greatest to least:
foreach$number(sort { $b &lt;=&gt; $a } @numbers) {
   print $number\n";
}
</pre>