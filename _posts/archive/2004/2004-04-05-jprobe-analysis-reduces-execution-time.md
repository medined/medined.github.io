---
layout: post
title: Jprobe Analysis Reduces Execution Time by 5%
date: '2004-04-05T13:06:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-05T13:15:01.090-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108118477449404470
blogger_orig_url: http://affy.blogspot.com/2004/04/jprobe-analysis-reduces-execution-time.md
year: 2004
theme: java
---

<p>Using JProbe, I noticed that my application was repeatedly compiling the same JudoScript scripts. I added a cache for
    compiled JudoScript scripts which reduced the calls to ParserHelper.parse():</p>


<table border="1" cellspacing="0" cellpadding="4">
    <tr>
        <th>Run</th>
        <th>Calls</th>
        <th>Cumulative Time</th>
        <th>Cumulative Objects Percent</th>
        <th>Cumulative Objects</th>
    </tr>
    <tr>
        <td>1</td>
        <td align="right">1,300</td>
        <td align="right">5.5%</td>
        <td align="right">13.6%</td>
        <td align="right">1,116,603</td>
    </tr>
    <tr>
        <td>2</td>
        <td align="right">13</td>
        <td align="right">0.3%</td>
        <td align="right">0.3%</td>
        <td align="right">23,456</td>
    </tr>
</table>

<p>Not a bad first optimization for our first use of the JProbe profiler.</p>