---
layout: post
title: Lucene; Bug; TestPhrasePrefixQuery in 2003.04.21 Build Has Misleading Code?
date: '2003-04-23T09:08:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[lucene]]"
modified_time: '2003-04-23T09:09:26.000-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200187188
blogger_orig_url: http://affy.blogspot.com/2003/04/lucene-bug-testphraseprefixquery-in.md
year: 2003
theme: java
---

<p>The TestPhrasePrefixQuery looks like it is searching for "blueberry pi*" and it even seems to work at first glance.
    However, the test data is not extensive enough to show what is really happening.</p>


<p>The searching technique implemented in TestPhrasePrefixQuery will find not only "blueberry pie" but also "blueberry
    strudel" if both exist in the documents.</p>

<p>The reason is that the IndexReader.terms(Term termToMatch) method looks for the first term equal or larger than
    termToMatch and then returns *all* terms from that point in the index to the end.</p>

<p>One potential solution might be something like the following:</p>

<pre>
String pattern = "pi*";
TermEnum te = ir.terms(new Term("body", pattern));
while (te.term().text().matches(pattern)) {
    termsWithPrefix.add(te.term());
    if (te.next() == false)
        break;
    }
}
</pre>

<p>Of course, the code above only works with JDK1.4 because of the pattern matching.</p>

<p>Comments?</p>