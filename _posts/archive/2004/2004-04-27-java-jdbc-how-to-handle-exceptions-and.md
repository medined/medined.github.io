---
layout: post
title: Java, JDBC - How to Handle Exceptions and Errors When Closing, or Freeing, Database Resources
date: '2004-04-27T15:46:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-27T15:51:12.013-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108309518673682642
blogger_orig_url: http://affy.blogspot.com/2004/04/java-jdbc-how-to-handle-exceptions-and.md
year: 2004
theme: java
---

When closing database resources it is possible to get multiple exceptions and it is important not to lose any of them.


All of the tutorials that I have read only show the use of <code>printStackTrace()</code> during exception handling.
However, that approach is not sophisticated enough for Enterprise applications. I use the following technique:
Exceptions (and Errors if applicable) are held in an ArrayList until all resources are closed. Then the list is
examined. If exceptions are present, they are processed as needed.
<pre>
    <b>// This code is part of my DataAccess class. Other code in the class is responsible for</b>
    <b>// opening the database connection and creating a prepared statement - both are</b>
    <b>// static objects.</b>

    public synchronized static void free() {
        // This list holds any exception objects that are caught.
        List caughtExceptions = new ArrayList();

        if (ps != null) {
            try {
                ps.close();
                logger.debug("free; closed prepared statement.");
            } catch (SQLException e) {
                caughtExceptions.add(e);
            }
        }
        if (con != null) {
            try {
                con.close();
	    	logger.debuf("free; closed database connection.");
            } catch (SQLException e) {
                caughtExceptions.add(e);
            }
        }

        if (caughtExceptions.size() > 0) {
            LogConfiguration.message(caughtExceptions, "Problem closing database resources.");
        }
    }

    <b>// Here is the LogConfiguration.message() method which resides in a different class</b>
    <b>// from the above code.</b>

	public static void message(final List exceptions, final String message) {
	    logger.fatal(message);
	    int throwableIndex = 1;
	    int throwableCount = exceptions.size();
	    for (Iterator iter = exceptions.iterator(); iter.hasNext(); ) {
	        Throwable t = (Throwable) iter.next();
		    logger.fatal("Exception [" + throwableIndex + "] of [" + throwableCount + "]", t);
		    throwableIndex++;
	    }
	}

</pre>