---
layout: post
title: Making ColdFusion Modules More Secure
date: '2003-03-01T12:27:00.000-05:00'
author: David Medinets
categories:
- "[[coldfusion]]"
modified_time: '2003-03-01T22:11:39.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90393151
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-making-coldfusion-modules-more.md
year: 2003
---

Making ColdFusion Modules More Secure


<p>While reading an article written by Matt Reider (Macromedia), I noticed the following tidbit that ensures that a CF module is not called directly via a URL.</p>
<pre>
&lt;!--- security - this template must be called as a custom tag ---&gt;
&lt;CFIF NOT isDefined("caller")&gt;
    &lt;CFABORT&gt;
&lt;CFELSE&gt;
    &lt;!---
        make sure caller is a structure� otherwise
        they could have passed it in the URL
     ---&gt;
    &lt;CFIF NOT isStruct(caller)&gt;
        &lt;CFABORT&gt;
    &lt;/CFIF&gt;
&lt;/CFIF&gt;
