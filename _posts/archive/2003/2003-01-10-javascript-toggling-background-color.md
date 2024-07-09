---
layout: post
title: Toggling Background Color When Checkbox is Checked
date: '2003-01-10T18:41:00.000-05:00'
author: David Medinets
categories:
- "[[javascript]]"
modified_time: '2007-11-21T13:40:50.782-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90169438
blogger_orig_url: http://affy.blogspot.com/2003/01/javascript-toggling-background-color.md
year: 2003
---

Toggling Background Color When Checkbox is Checked


<pre>
&lt;script language="javascript"&gt;
  function toggleRowColor(cb, row) {
    if (navigator.appName != "Netscape") {
      if (cb.checked == true) {
        eval("top.row" + row + ".style.background = '#d1e6f9';");
      } else {
        eval("top.row" + row + ".style.background = '#ffffff';");
      }
    }
}
&lt;/script&gt;

&lt;tr id="row2" bgcolor="#ffffff"&gt;&lt;td&gt;
  &lt;input type="checkbox" name='senderid' value='931904' onclick="toggleRowColor(this, 2)"&gt;
&lt;/td&gt;&lt;/tr&gt;
</pre>
