---
layout: post
title: Using JudoScript to Convert XML into Java HashMap.
date: '2003-11-19T14:27:00.000-05:00'
author: David Medinets
categories:
- "[[xml]]"
modified_time: '2003-11-19T14:31:36.600-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-106927005060791140
blogger_orig_url: http://affy.blogspot.com/2003/11/using-judoscript-to-convert-xml-into.md
year: 2003
theme: java
---

I'm finally a convert to the ranks of JudoScripters.


The following example doesn't show the Java code (about 15 lines of code) that invokes the JudoScript because I wanted
to keep this entry small.

<p>My task was to maintain some information external to my Java program so I chose an XML format:</p>
<pre>
  &lt;requiredNodes&gt;
    &lt;message type="all"&gt;
      &lt;requiredNode name="messageHeader" xpath="/eanucc:envelope/messageHeader"/&gt;
    &lt;/message&gt;
    &lt;message type="eanucc:tradeItemDocument"&gt;
      &lt;requiredNode name="tid.gtin" xpath="/eanucc:envelope/../gtin"/&gt;
      &lt;requiredNode name="tid.glnOfInformationProvider" xpath="/eanucc:envelope/../gln"/&gt;
    &lt;/message&gt;
  &lt;/requiredNodes&gt;
</pre>
<p>In order to convert this file into an easily used Java object, I used JudoScript:</p>
<pre>
	// ReadRequiredNodes.judo

	requiredNodes = javanew java.util.HashMap();
	$$bsf.registerBean("requiredNodes", requiredNodes);

	do BRRequiredNodesFileName as xml {
		&lt;message&gt;:
			messageMap = requiredNodes.get($_.getAttrValue(0));
			if messageMap == null {
				messageMap = javanew java.util.HashMap();
				requiredNodes.put($_.getAttrValue(0), messageMap);
			}
		&lt;requiredNode&gt;:
			messageMap.put($_.getAttrValue(1), $_.getAttrValue(0));
	}
</pre>
<p>Some issues to note:</p>
<ul>
	<li>When using SAX-based XML processing, I needed to use attributes to hold the name and xpath because the XML
		processing in JudoScript seems to have a flaw. The $_ variable (the current xml node) can't be used as the key
		in a HashMap.</li>
	<li>When using SAX-based XML processing, There is no way to access attributes by name. Therefore the script uses
		index numbers which is brittle coding.</li>
</ul>
<p>Overall, I found that JudoScript was easy to use and quite a compact way to handle the XML->HashMap transformation.
</p>