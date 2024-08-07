---
layout: post
title:
date: '2001-11-13T23:17:00.000-05:00'
author: David Medinets
modified_time: '2001-11-13T23:17:10.626-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7106594
blogger_orig_url: http://affy.blogspot.com/2001/11/spent-two-hours-at-northern-virginia.md
year: 2001
categories:
- "[[java]]"
- "[[oracle]]"
---

Spent two hours at the <a href="http://www.novajug.org/">Northern Virginia Java User's Group</a> listening
to a talk on Enterprise Java Beans (EJBs).


While I can see the value, I worry that the EJB servers are essentially trying to replicate the basics of a
database in middleware. For example, there is a EJB Query language that is very much like SQL Lite. And
the Deployment XML file holds the relationships between Entity objects (also known as database tables).
While EJB 2.0 allows for complex object-to-relational mappings, the technology is very new. How much of
Oracle or SQL Server needs to be replicated inside the middleware server? Why should it be necessary to
pay for developing the same functionality in the middleware and in the database?

On another note, I've decided that the Nested Tree Model (NTM) package that I'm developing for Oracle
needs to be more independant. I currently have the module tightly coupled with a Data Repository package for
table creation, auditing, and action validation. Tomorrow, I'll decouple.