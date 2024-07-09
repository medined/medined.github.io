---
layout: post
title:
date: '2001-11-15T12:29:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
- "[[oracle]]"
modified_time: '2007-11-21T12:08:36.499-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7147188
blogger_orig_url: http://affy.blogspot.com/2001/11/more-time-studies-today-using-jdbc-to.md
year: 2001
theme: java
---

More time studies today using JDBC to move records from MS Access to Oracle.


With an Oracle Insert taking 25 msecs and the XML creation taking 1 msec, each object starts out taking 26 msecs. When
the whole program (reading records, starting threads, database connections, etc.) was run, each object took 149 msecs to
process. After I adding a database connection pool, each object took 109 msecs. Which is still too long. My goal is 36
msecs to process 100,000 objects per hour.