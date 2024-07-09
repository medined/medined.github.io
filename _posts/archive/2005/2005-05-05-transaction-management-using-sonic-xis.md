---
layout: post
title: Transaction Management Using Sonic XIS Server, Oracle, and Spring
date: '2005-05-05T13:27:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-07-14T20:40:19.840-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-111531476594460864
blogger_orig_url: http://affy.blogspot.com/2005/05/transaction-management-using-sonic-xis.md
year: 2005
theme: java
---

<p>The WorldSync application has three types of transactions that need to be managed:</p>
<ul>
  <li>XIS</li>
  <li>Direct use of Oracle</li>
  <li>Spring-mediated use of Oracle</li>
</ul>


<p>Let's look at each transaction type to see how the API is used.</p>

<span style="font-weight:bold;">XIS (with synchronized Oracle transactions)</span>

<p>When working with the XIS engine, you should only deal with the org.wwre.xis.transaction.TransactionHelper class.
  Oracle connections are managed automatically to coincide with XIS actions.</p>

<table>
  <tr>
    <td>TransactionHelper.getClientSession()<br>getLocalReadOnlySession()<br>getLocalUpdateSession()<br></td>
    <td>These three methods return an XIS Session object.</td>
  </tr>
  <tr>
    <td>TransactionHelper.commitSession()</td>
    <td>This method commits XIS and Oracle transactions. If a new Oracle transaction is needed,
      OracleBootstrap.newTransaction() must be called.</td>
  </tr>
  <tr>
    <td>TransactionHelper.releaseSession()</td>
    <td>This method rolls back both the XIS and Oracle transactions if the XIS transaction has been been commited.
      Otherwise, it releases the XIS session and closes the Oracle connection.</td>
  </tr>
</table>

<p>A typical use of these methods might be:</p>

<pre>
  Session session = null;

  try {
    ReadWimConfiguration.processUsingDefaults();
    session = TransactionHelper.getLocalReadOnlySession();
    // do some XIS and Oracle work.
    TransactionHelper.commitSession();
  } catch (Exception e) {
    logger.fatal(e, e);
  } catch (Error e) {
    logger.fatal(e, e);
  } finally {
    TransactionHelper.releaseSession();
    System.out.println("Done.");
  }
</pre>

<span style="font-weight:bold;">Direct use of Oracle</span>

<p>If you only need to work with Oracle, you can directly use the OracleBootstrap class.</p>

<table>
  <tr>
    <td>getConnection()</td>
    <td>This method needs to be called first. It will setup the DataSource object, if needed. A singleton Connection
      object is returned which is managed using the methods in OracleBootstrap. Thise connection has the AutoCommit
      option turned off.</td>
  </tr>
  <tr>
    <td>addException()</td>
    <td>This method is called from within catch} blocks to register than an exception or error occured.</td>
  </tr>
  <tr>
    <td>closeConnection()</td>
    <td>This method closes the Oracle connection and clears any caught exceptions (unless created by the close operation
      itself).</td>
  </tr>
  <tr>
    <td>closeResultSet()</td>
    <td>This method closes the specified result set. It maskes the capture of exceptions thrown by the close operation.
    </td>
  </tr>
  <tr>
    <td>closeStatement()</td>
    <td>This method closes the specified statement (prepared or otherwise). It maskes the capture of exceptions thrown
      by the close operation.</td>
  </tr>
  <tr>
    <td>commitConnection()</td>
    <td>This method commits the connection but does not close it.</td>
  </tr>
  <tr>
    <td>getExceptions()</td>
    <td>This method returns a copy of the collected exceptions.</td>
  </tr>
  <tr>
    <td>hasExceptions()</td>
    <td>This method returns true of any exceptions have been collected.</td>
  </tr>
  <tr>
    <td>newTransaction()</td>
    <td>This method starts a new transaction. It only needs to be called if you want to begin a new transaction after
      rolling back or commiting.</td>
  </tr>
  <tr>
    <td>rollbackConnection()</td>
    <td>This method rolls back the connection but does not close it.</td>
  </tr>
</table>

<p>A typical use of these methods might be:</p>

<pre>
// The doIt() method does not commit nor rollback. It just handles the
// low-level SQL and exception handling.

public static void doIt() {
  Connection con = null;
  PreparedStatement ps = null;

  try {
    con = OracleBootstrap.getConnection();
    ps = con.prepareStatement("sql statement");
    // set parameters and execute query.
  } catch (Exception e) {
    OracleBootstrap.addException(e);
  } finally {
    OracleBootstrap.closeStatement(ps);
  }
  if (OracleBootstrap.hasExceptions()) {
    LogConfiguration.message(OracleBootstrap.getExceptions(), "some message");
  }
}

// This driver method need to be aware of the transaction. If an
// no exception occurs, the connection is commited. Otherwise, it
// is rolled back. In the finally clause, the connection is closed.
pubic static void foo() {
  try {
    doIt();
    OracleBootstrap.commitConnection();
  } catch (Exception e) {
    OracleBootstrap.rollbackConnection();
  } catch (Error e) {
    OracleBootstrap.rollbackConnection();
  } finally {
    OracleBootstrap.closeConnection();
}
</pre>

<span style="font-weight:bold;">Spring-mediated use of Oracle</span>

<p><a url="http://www.springframework.org/">The Spring framework</a> has been around for quite some time. Since
  WorldSYNC v3.5 is making greater use of Oracle, I've explored existing frameworks instead of expanding the use of our
  homegrown JDBC-specific classes.</p>

<p>When using Spring has an mediator for Oracle, you should use the OracleBootstrap class. Its use is similar to that
  shown above except that some of the transaction handling is automated (you don't need to manually call the rollback
  method when an exception happens).</p>

<table>
  <tr>
    <td>getDataSource()</td>
    <td>This method needs to be called first (instead of the getConnection() method). It will setup the DataSource
      object, if needed and start a new transaction.</td>
  </tr>
  <tr>
    <td>addException()</td>
    <td>This method is called from within catch} blocks to register than an exception or error occured.</td>
  </tr>
  <tr>
    <td>closeConnection()</td>
    <td>This method closes the Oracle connection and clears any caught exceptions (unless created by the close operation
      itself).</td>
  </tr>
  <tr>
    <td>commitConnection()</td>
    <td>This method commits the connection but does not close it.</td>
  </tr>
  <tr>
    <td>getExceptions()</td>
    <td>This method returns a copy of the collected exceptions.</td>
  </tr>
  <tr>
    <td>hasExceptions()</td>
    <td>This method returns true of any exceptions have been collected.</td>
  </tr>
  <tr>
    <td>newTransaction()</td>
    <td>This method starts a new transaction. It only needs to be called if you want to begin a new transaction after
      rolling back or commiting. </td>
  </tr>
</table>

</p>A typical use of these methods might be:</p>

<pre>
try {
  ReadWimConfiguration.processUsingDefaults();
  Spring01 a = new Spring01(OracleBootstrap.getDataSource());
  a.makeCachePoolFree("C05");
  OracleBootstrap.commitConnection();
} catch (Exception e) {
  NoOperation.noOp();
} catch (Error e) {
  NoOperation.noOp();
} finally {
  OracleBootstrap.closeConnection();
}
</pre>

<p>The rollback, if needed, is handled automatically when the connection is closed.</p>