---
layout: post
title: Solving the "Second-level cache is not enabled" Exception
date: '2006-12-21T11:06:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-12-22T00:20:41.513-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116671752844610901
blogger_orig_url: http://affy.blogspot.com/2006/12/solving-second-level-cache-is-not.md
year: 2006
theme: java
---

<p>I ran into the "Second-level cache is not enabled" error recently even though I had the
    <i>hibernate.cache.use_query_cache</i> and <i>hibernate.cache.use_second_level_cache</i> parameters defined as true.
    The issue was resolved when I <b>also</b> defined the <i>hibernate.cache.provider_class</i> as
    org.hibernate.cache.EhCacheProvider.</p>