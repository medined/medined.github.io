---
layout: post
title: Spring and RIFE; Creating Database Tables Together
date: '2005-11-03T10:35:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-06-14T19:11:04.426-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-113103285065987843
blogger_orig_url: http://affy.blogspot.com/2005/11/spring-and-rife-creating-database.md
year: 2005
theme: java
---

<p>Two posts ago, I showed how RIFE could be used to generate CREATE TABLE SQL statements.</p>


<p>Let me digress for just a paragraph about <i>why</i> I am exploring this technique to create database tables:
<ul>
    <li>I'm hoping to use the Constraints mechanism used by RIFE to automate some DAO validation.</li>
    <li>Using RIFE lets me use the same code to generate SQL specific to each database product.
    <li>
    <li>My ultimate goal is to create a bean that actually creates the database table not just the SQL - more on this in
        future posts.</li>
    <li>I dislike SQL scripts which don't provide the flexibility and robustness that Java provide - for example,
        logging and exception handling.</li>
</ul>
</p>

<p>Today, I demonstrate how to integrate Spring and RIFE. Here are the files that are involved:
<ul>
    <li><i>DatabaseTableCreator.java</i> - This interface specifies the methods used to generate SQL.</li>
    <li><i>BaseDatabaseTableCreator.java</i> - This abstract class provides the CreateTable attribute to subclasses.
    </li>
    <li><i>BeerTableCreator.java</i> - This concrete class specifies the contstraints needed for the Beer database
        table.</li>
    <li><i>PlayCreateTable.java</i> - This driver file loads the Spring configuration file and instantiates beans.</li>
    <li><i>spring.xml</i> - This configuration file specifies the database configuration and wires the beans together.
    </li>
</ul>
</p>

<h3>DatabaseTableCreator.java</h3>
<pre>
package org.affy.play;

import com.uwyn.rife.database.queries.CreateTable;

public interface DatabaseTableCreator {
    public String getSql();
    public String getTableName();
    public void setCreateTable(final CreateTable _createTable);
    public CreateTable getCreateTable();
}
</pre>

<h3>BaseDatabaseTableCreator.java</h3>
<pre>
package org.affy.play;

import com.uwyn.rife.database.queries.CreateTable;

abstract public class BaseDatabaseTableCreator implements DatabaseTableCreator {

    private CreateTable createTable = null;

    public BaseDatabaseTableCreator() {
        super();
    }

    abstract public String getSql();

    abstract public String getTableName();

    public CreateTable getCreateTable() {
        return this.createTable;
    }

    public void setCreateTable(CreateTable _createTable) {
        this.createTable = _createTable;
    }

}
</pre>

<h3>BeerTableCreator.java</h3>
<pre>
package org.affy.play;

import org.activemapper.Beer;

import com.uwyn.rife.database.DbConnection;
import com.uwyn.rife.database.DbStatement;
import com.uwyn.rife.database.queries.CreateTable;

public class BeerTableCreator extends BaseDatabaseTableCreator {

    public BeerTableCreator() {
        super();
    }

    public String getSql() {
        getCreateTable().table(getTableName())
        .columns(Beer.class)
        .primaryKey("id")
        .precision("brand", 50)
        .nullable("brand", CreateTable.NOTNULL);

        return getCreateTable().getSql();li>
    }

    public String getTableName() {
        return "beer";
    }

}
</pre>

<h3>PlayCreateTable.java</h3>
<pre>
package org.affy.play;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PlayCreateTable {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");

        DatabaseTableCreator beerTableCreator = (DatabaseTableCreator)ctx.getBean("beerTableCreator");
        System.out.println(beerTableCreator.getSql());
    }

}
</pre>

<h3>spring.xml</h3>
<pre>
&lt;?xml version='1.0' encoding='UTF-8'?&gt;
&lt;!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' 'http://www.springframework.org/dtd/spring-beans.dtd'&gt;
&lt;beans&gt;

  &lt;bean id='rifeDatasource' class='com.uwyn.rife.database.Datasource' lazy-init='true'&gt;
    &lt;property name='driver'&gt;&lt;value&gt;org.hsqldb.jdbcDriver&lt;/value&gt;&lt;/property&gt;
    &lt;property name='url' value='jdbc:hsqldb:file:data/relationalDb'/&gt;
    &lt;property name='user' value='sa'/&gt;
    &lt;property name='password' value=''/&gt;
  &lt;/bean&gt;

  &lt;bean id='createTable' class='com.uwyn.rife.database.queries.CreateTable' lazy-init='true'&gt;
    &lt;constructor-arg ref='rifeDatasource'/&gt;
  &lt;/bean&gt;

  &lt;bean id='beerTableCreator' class='org.affy.play.BeerTableCreator' lazy-init='true'&gt;
    &lt;property name='createTable'&gt;&lt;ref bean='createTable'/&gt;&lt;/property&gt;
  &lt;/bean&gt;

&lt;/beans&gt;
</pre>

<p>I don't feel that I need to add any commentary, the code above speaks for itself. If you have questions, feel free to
    ask questions.</p>