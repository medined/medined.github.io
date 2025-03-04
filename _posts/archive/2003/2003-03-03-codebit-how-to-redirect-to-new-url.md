---
layout: post
title: How to Redirect to New URL Using JavaScript?
date: '2003-03-03T17:37:00.000-05:00'
author: David Medinets
categories:
- "[[javascript]]"
modified_time: '2003-03-03T17:37:12.580-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90401874
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-to-redirect-to-new-url.md
year: 2003
theme: java
---

How to Redirect to New URL Using JavaScript?



<P>This example shows how to redirect a web browser to another page. The JavaScript performs a countdown of the number
  of seconds left before the redirection is performed.</P>
<PRE>
&lt;HTML&gt;
&lt;HEAD&gt;
  &lt;TITLE&gt;"Some Page" has moved!&lt;/TITLE&gt;
  &lt;META HTTP-EQUIV=Refresh CONTENT=6;URL=http://www.codebits.com/&gt;
  &lt;SCRIPT LANGUAGE="Javascript"&gt;
    &lt;!--
    function countdown() {
      if ( document.myform.count.value > 0 ) {
        document.myform.count.value = document.myform.count.value-1;
        setTimeout("countdown()", 1000);
      }
    }
    //--&gt;
  &lt;/SCRIPT&gt;
  &lt;/HEAD&gt;
  &lt;BODY>&lt;H1&gt;"Some Page" has Moved!&lt;/H1&gt;
  The new address is:
  &lt;A HREF="http://www.codebits.com/" TARGET=_top&gt;http://www.codebits.com&lt;/A&gt;.
  &lt;P&gt;&lt;FORM NAME=myform&gt;
  You will be redirected to the above page in
  &lt;INPUT NAME="count" SIZE="1" value="6"&gt; secs.....)&lt;/FORM&gt;
  &lt;SCRIPT LANGUAGE="Javascript"&gt;
    &lt;!--
    countdown();
    //--&gt;
  &lt;/SCRIPT&gt;
  &lt;/BODY&gt;
&lt;/HTML&gt;
</PRE>