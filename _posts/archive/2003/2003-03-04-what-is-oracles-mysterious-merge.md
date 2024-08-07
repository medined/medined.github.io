---
layout: post
title: What is Oracle's Mysterious MERGE Keyword for?
date: '2003-03-04T12:49:00.000-05:00'
author: David Medinets
categories:
- "[[oracle]]"
modified_time: '2003-03-08T23:50:10.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90405574
blogger_orig_url: http://affy.blogspot.com/2003/03/what-is-oracles-mysterious-merge.md
year: 2003
---

<p>Oracle 9i supports a new SQL statement called MERGE. It's fully <a
		href="http://download-west.oracle.com/otndoc/oracle9i/901_doc/server.901/a90125/statements_916.htm">documented</a>
	but let me provide an example that I've just been working on.</p>


<p>The advantage of MERGE is that a single SQL statement handles two cases:
<ul>
	<li>INSERT if the record doesn't exist.</li>
	<li>UPDATE if the record exists.</li>
</ul>
</p>

<p>The intended use of MERGE was to modify one database table based on another
	table (or view). However, through a little creativity we can use MERGE for more
	mundane tasks - like the standard administrative Add/Edit a single-record functions.</p>

<p>The following SQL shows how to use the MERGE statement to insert or update
	a single record.</p>

<pre>
MERGE INTO
	dr_fulltext A
USING
	(select 'Test41' u_object, 'EN-US' u_locale from dual) B
ON
	(A.u_object = B.u_object AND A.u_locale = B.u_locale)
WHEN MATCHED THEN
	UPDATE SET A.keywords = '33333333333'
WHEN NOT MATCHED THEN
	INSERT (
	    A.u_object, A.u_locale, A.keywords
	) values (
	    'Test41', 'EN-US', '222222'
	);
</pre>

<p>Since the Merge clause is designed for getting information from a table I
	needed to finagle the USING class in order to create what is essentially a
	virtual table of one record. The ON clause checks to see if that single
	record (from the B table) exists in the A table. If it doesn't exist the INSERT statement is
	run. If it does, then the UPDATE statement is run.</p>

<p>The parentheses around the ON clause seem to be critical but I don't know why.
	If the parentheses are left off, then an "ORA-00969: missing ON keyword" error
	results.</p>

<p>The alias names in the UPDATE and INSERT are logically not needed, but Oracle
	demands clarity because the Keywords field is used twice. If the alias name (A)
	is not supplied Oracle responds with an "ORA-00957: duplicate column name" error.</p>