---
layout: post
title: How Can I Update All CPAN Perl Modules At Once?
date: '2003-03-03T20:44:00.001-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2008-04-08T09:30:24.866-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90402499
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-can-i-update-all-cpan-perl.md
year: 2003
theme: perl
---

How Can I Update All CPAN Perl Modules At Once?


<p>Run the following from a command-line:</p>
<pre>perl -MCPAN -e 'CPAN::Shell->install(CPAN::Shell->r)'</pre>

<p>2008-04-08 Update: An observant reader noticed that my example was missing an ending quote.</p>