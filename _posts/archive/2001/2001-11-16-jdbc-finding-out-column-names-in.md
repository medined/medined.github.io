---
layout: post
title: Java JDBC Metadata Holds Query Column Names
date: '2001-11-16T14:50:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2007-11-21T12:08:45.821-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7177112
blogger_orig_url: http://affy.blogspot.com/2001/11/jdbc-finding-out-column-names-in.md
year: 2001
theme: java
---

Java JDBC code to find column names in a query.


<pre>
  private Connection mAccess;
  private Statement mStatement;
  private ResultSet mResultSet;
  private ResultSetMetaData mResultSetMetaData;

  Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
  mAccess = DriverManager.getConnection("jdbc:odbc:pma_boeing");

  mStatement = mAccess.createStatement();
  ResultSet mResultSet = mStatement.executeQuery("Select top 10 * from boeing");

  mResultSetMetaData = mResultSet.getMetaData();
  int TotalColumn = mResultSetMetaData.getColumnCount();

  for (int j = 1; j <= TotalColumn; j++) {
    System.out.println("[" + j + "] " + mResultSetMetaData.getColumnName(j));
  }
</pre>