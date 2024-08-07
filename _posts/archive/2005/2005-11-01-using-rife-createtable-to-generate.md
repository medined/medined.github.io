---
layout: post
title: Using RIFE CreateTable to Generate CREATE TABLE Sql
date: '2005-11-01T21:08:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2005-11-01T21:09:40.006-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-113089737999361444
blogger_orig_url: http://affy.blogspot.com/2005/11/using-rife-createtable-to-generate.md
year: 2005
theme: java
---

<p>The RIFE framework has functionality which abstracts the Sql Create Table statement into Java classes. The
  fundamental reason to use an abstraction instead of writing the Sql directly is that the underlying framework
  generates Sql specific to the targeted database. Why is this important? Frequently I use an open-source database like
  Hypersonic for development but a commercial product like Oracle for production.</p>


<p>The following code demonstrates the technique:</p>

<pre>
  String driverClassname = "org.hsqldb.jdbcDriver";
  String url = "jdbc:hsqldb:hsql://localhost:9101/test";
  String username = "sa";
  String password = "";
  String poolSize = 5;

  Datasource ds = new Datasource(driverClassname, url, username, password, poolSize);

  CreateTable create = new CreateTable(ds);

  create.table("beer")
    .columns(Beer.class)
    .primaryKey("id")
    .precision("brand", 50)
    .nullable("brand", CreateTable.NOTNULL);

  String createSql = create.getSql();
</pre>

<p>When executed, the generated SQL looks like this:</p>

<pre>
CREATE TABLE beer (brand VARCHAR(50) NOT NULL, id INTEGER NOT NULL, price NUMERIC, PRIMARY KEY (id))
</pre>

<p>For completeness, here are the relevant parts of Beer.java</p>

<pre>
public class Beer {

    private String brand = null;

    private BigDecimal price = null;

    private int id = 0;

    // ... snipped out getters and setters.
}
</pre>