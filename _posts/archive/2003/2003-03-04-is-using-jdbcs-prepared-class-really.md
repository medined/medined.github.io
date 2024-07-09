---
layout: post
title: Is using JDBC's Prepared Class really faster than the Statement Class?
date: '2003-03-04T12:39:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-04T12:39:04.473-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90405513
blogger_orig_url: http://affy.blogspot.com/2003/03/is-using-jdbcs-prepared-class-really.md
year: 2003
theme: java
---

Most of my reading about JDBC indicated that we should use the PreparedStatement class to take advantage of bind
variables and pre-compilation of SQL. However, one article said that using the Statement class was better because of how
vendors implement the JDBC drivers. So, of couse, I felt compelled to perform my own timing tests.


<p>NOTE: I used the OracleConnectionPoolDataSource class to connect to Oracle9i for these tests.</p>

<p>I executed the following simple select statement 1,000 times with both classes. Of course, when using the
   PreparedStatement, the string literal was replaced by a bind variable.</p>
<pre>
  select uuid, display_name, description from dr_locales where uuid = 'EN-US'
</pre>
<p>The example using the statement class took 3,564 milliseconds to run. While the PreparedStatement example took 2,594
   seconds to run.</p>
<p>Of course, I dug a little deeper. How much overhead is involved in instantiating the two classes? In order to answer,
   I placed the object instantation inside the loop:</p>
<pre>
                      InsideLoop     OutsideLoop
   Statement:         3,915          3,564
   PreparedStatement: 4,486          2,594 <-- best elapsed time.
</pre>
<p>The following figure shows the relationships graphically.</p>
<img src="http://affy.blogspot.com/images/Statement_vs_PreparedStatement.jpg" width="482" height="340" border="0"
   align="middle" alt="A rehash of the text table.">
<p>This test shows that it really pays to reuse PreparedStatement objects.</p>
<p>The last question I tried to answer in my tests was; Which class is better if no bind variables are used?</p>
<pre>
   Statement:         4,636
   PreparedStatement: 3,445
</pre>
<p>So, my empiric results agree with my common sense and the majority of the literature that I've read. </p>
<p>It seems that the PreparedStatement class should be used in all cases.</p>