---
layout: post
title: Getting Text from an XML CDATA_SECTION_NODE in Java.
date: '2003-12-03T10:12:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-12-03T10:13:05.330-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-107046437478688175
blogger_orig_url: http://affy.blogspot.com/2003/12/getting-text-from-xml-cdatasectionnode.md
year: 2003
theme: java
---

Here is a code snippet showing how to get text from a CDATA node in XML.


<pre>
NodeList nodeList = node.getChildNodes();
if (nodeList != null) {
    for (int i = 0; i &lt; nodeList.getLength(); i++) {
        Node child = nodeList.item(i);
        if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
	CDATASection cdata = (CDATASection)child;
	System.out.println('The CDATA has text: [" + cdata.getData() + "]");
        }
    }
}
</pre>