---
layout: post
title: Use CSS to Ensure Common Navigation HTML on Every Page
date: '2006-10-25T10:21:00.000-04:00'
author: David Medinets
categories:
- "[[css]]"
modified_time: '2006-10-25T10:28:53.343-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116178647587061115
blogger_orig_url: http://affy.blogspot.com/2006/10/use-css-to-ensure-common-navigation.md
year: 2006
---

This tip was developed (or at least described by) Trenton Moss on the ITWALES.com site. I reproduce the essential
elements of only one of the ten tips that he writes about. Please visit itwales.com to see all ten.


Add a class to each navigation item:

<code>&lt;ul&gt;
&lt;li&gt;&lt;a href="#" class="home"&gt;Home&lt;/a&gt;&lt;/li&gt;
&lt;li&gt;&lt;a href="#" class="about"&gt;About us&lt;/a&gt;&lt;/li&gt;
&lt;li&gt;&lt;a href="#" class="contact"&gt;Contact us&lt;/a&gt;&lt;/li&gt;
&lt;/ul&gt;</code>

Then insert an id into the &lt;body&gt; tag that indicates which are it represents. For example, When in 'Home' it
should read &lt;body id="home"&gt;. Now create a CSS rule:

<code>#home .home, #about .about, #contact .contact {
  commands for highlighted navigation go here
}</code>