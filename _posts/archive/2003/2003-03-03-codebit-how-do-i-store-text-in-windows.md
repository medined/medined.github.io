---
layout: post
title: How Do I Store Text in the Windows Clipboard Using JavaScript?
date: '2003-03-03T20:14:00.000-05:00'
author: David Medinets
categories:
- "[[javascript]]"
modified_time: '2003-03-04T01:35:24.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90402417
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-do-i-store-text-in-windows.md
year: 2003
---

How Do I Store Text in the Windows Clipboard Using JavaScript?


<p>Click on the first line of text in order to see that text pasted into the third line of text:</p>
<pre>
&lt;HEAD&gt;
&lt;SCRIPT&gt;
function IsWhitespace(ch) {
  return(
    ch == ' ' || ch == '\n' || ch == '\r' ||
    ch == '\t' || ch == '\f' || ch == '\v' || ch == '\b'
  );
}

// SaveToClibboard
function S2CB() {
  maxLen = 80;
  numLines = event.srcElement.innerText.length / maxLen;
  newStr = '';
  startPos = 0;
  for (x = 0; x &lt; numLines; x++) {
    endPos = startPos + maxLen;
    while (! IsWhitespace(event.srcElement.innerText.charAt(endPos)))
      endPos--;
    newStr = newStr + event.srcElement.innerText.substring(startPos, endPos) + '\n';
    startPos = endPos + 1;
  }
  newStr = newStr + event.srcElement.innerText.substring(
    startPos, event.srcElement.innerText.length
  ) + '\n';
  window.clipboardData.setData('Text', newStr);
  oSource2.innerText = window.clipboardData.getData('Text');
}
&lt;/SCRIPT&gt;
&lt;/HEAD&gt;
&lt;BODY&gt;
&lt;B ID="oSource" onClick="S2CB()"&gt;@changes 2000-Nov-01
  DMM DMM00013 Silentsurf(nph-index.cgi,Interface.pm):
  Initial hits to the server are immediately routed to the startup
  form which eliminates a lot of module loading in the
  Silentsurf package./&lt;/B&gt;&lt;br&gt;
&lt;B ID="oSource1" onClick="S2CB()"&gt;222&lt;/B&gt;&lt;br&gt;
&lt;pre&gt;!!&lt;B ID="oSource2" onClick="S2CB()"&gt;333&lt;/B>!!&lt;/pre&gt;&lt;br&gt;
&lt;/BODY&gt;
&lt;/HTML&gt;
</pre>