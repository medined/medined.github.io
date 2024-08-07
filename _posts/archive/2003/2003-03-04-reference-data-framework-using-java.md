---
layout: post
title: A Reference Data Framework Using Java Data Objects (JDO)
date: '2003-03-04T02:00:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-04T02:00:49.686-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90403487
blogger_orig_url: http://affy.blogspot.com/2003/03/reference-data-framework-using-java.md
year: 2003
theme: java
---

<p>This paper describes one of the packages developed during a consultancy engagement between Ogilvie Partners Ltd and
    Eclectic Consulting in Arlington, VA, during July 2002. Our joint aim in publishing our results is to add to the
    evolving body of knowledge about the application of JDO to real-world projects.</p>


<p>The MS Word version of the document can be found at <a href="/assets/reference-data-framework.doc">Reference Data
        Framework</a>.</p>

<p>The source code that supports the framework can be found at <a
        href="/assets/reference_data_framework.zip">reference_data_framework.zip</a>.

<p><b>Abstract</b> - This paper describes one of the packages developed during a consultancy engagement between Ogilvie
    Partners Ltd and Eclectic Consulting in Arlington, VA, during July 2002. Our joint aim in publishing our results is
    to add to the evolving body of knowledge about the application of JDO to real-world projects.</p>

<p>Every application requires some form of reference data to be persisted and managed. Projects tend to treat the topic
    in different ways which results in duplication of design and implementation effort. As part of a much larger design
    effort we faced this issue and attempted to write a generic framework that could be reused across different
    projects.</p>