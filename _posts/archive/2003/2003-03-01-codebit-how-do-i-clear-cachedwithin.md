---
layout: post
title: How Do I Clear a CachedWithin Query Using ColdFusion?
date: '2003-03-01T12:19:00.000-05:00'
author: David Medinets
categories:
- "[[coldfusion]]"
modified_time: '2003-03-01T12:20:09.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90393121
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-do-i-clear-cachedwithin.md
year: 2003
---

How Do I Clear a CachedWithin Query Using ColdFusion?


<p>If you use a dynamic timespan instead of a fixed timespan, you can clear the cache by setting the timespan to zero when the query is executed.</p>
<pre>
&lt;cfif url.clear_cache eq 1&gt;
    &lt;cfset time_span = CreateTimeSpan(0,0,0,0)&gt;
&lt;cfelse&gt;
    &lt;cfset time_span = CreateTimeSpan(0,0,30,0)&gt;
&lt;/cfif&gt;

&lt;cfquery ... cachedwithin="#time_span#"&gt;
   ... do some sql here.
&lt;/cfquery&gt;
</pre>
