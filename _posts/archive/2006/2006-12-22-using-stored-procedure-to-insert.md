---
layout: post
title: Using a Stored Procedure to Insert Records with Spring & Hibernate
date: '2006-12-22T11:45:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-12-22T11:52:54.336-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116680637339020359
blogger_orig_url: http://affy.blogspot.com/2006/12/using-stored-procedure-to-insert.md
year: 2006
theme: java
---

It took a little research to discover how to integrate a stored procedure for inserting records into my application.
However, once I found the right piece of information, it became easy.


Here is what I am using:

<pre>&lt;hibernate-mapping&gt;
   &lt;class name="com.codebits.vo.SuperSimpleRecord" table="super_simple"&gt;
       &lt;id name="id" type="string" length="32" unsaved-value="null"&gt;
           &lt;generator class="uuid.hex" /&gt;
       &lt;/id&gt;
       &lt;property name="name" type="string" length="100" not-null="true" /&gt;
       &lt;property name="count" type="integer" not-null="false" /&gt;
       &lt;property name="created" type="date" not-null="true" insert="false" /&gt;
       <b>&lt;sql-insert callable="true" <i>check="none"</i>&gt;
         {call insert_record(?, ?, ?)}
       &lt;/sql-insert&gt;</b>
   &lt;/class&gt;
&lt;/hibernate-mapping&gt;</pre>
<p>Notice the <b>check</b> attribute? That's what took me awhile to find, I think it was introduced in Hibernate 3.2.
  Just for completeness, here is my stored procedure:</p>
<pre>create or replace procedure insert_record(
  in_name in varchar2, in_count in number, in_id in varchar2
) as begin
  insert into super_simple
  values(in_id, in_name, in_count, sysdate);
  commit;
end insert_record;</pre>