---
layout: post
title: Custom Ant Task to Execute SQL DDL Statements And Ignoring Certain Errors.
date: '2004-05-28T15:40:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
modified_time: '2004-05-28T15:43:54.963-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108577343496369949
blogger_orig_url: http://affy.blogspot.com/2004/05/custom-ant-task-to-execute-sql-ddl.md
year: 2004
theme: java
---

<p>This Ant task executes any DDL SQL statements. The value of this task over the standard Ant SQL task is that some
    errors, like 'Table already exists' are ignored. The ReadWimConfiguration class simply sets the Oracle URL,
    username, and password. The OracleBootstrap class gets an Oracle DataSource object and provides a method to get a
    connection. I can post that class if needed.</p>


<pre>
package org.wwre.ant.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.wwre.common.ReadWimConfiguration;
import org.wwre.datastore.oracle.OracleBootstrap;

public class ExecuteDDLTask extends Task{

    /** A list with text lines that contains the query. */
    private List textLines = new ArrayList();

    public void execute() throws BuildException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            if (ReadWimConfiguration.isRead() == false) {
                throw new BuildException("Please call the readWimConfiguration task.");
            }

            OracleBootstrap.initialize();

            try {
                con = OracleBootstrap.getConnection();
                ps = con.prepareStatement(toString());
                ps.executeQuery();
            } catch (SQLException e) {
                boolean validError = false;

                if (e.getMessage().startsWith("ORA-01430: column being added already exists in table")) {
                    validError = true;
                }
                if (e.getMessage().startsWith("ORA-00955: name is already used")) {
                    validError = true;
                }
                if (validError == false) {
                    throw new BuildException(toString() + "\n" + e.getMessage());
                }
            } finally {
                // Note: The connection is not closed so that it can be reused!
            }

        } catch (Error e) {
            e.printStackTrace();
            throw new BuildException("Unhandled Error: " + e.getMessage());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equals("alreadyLogged") == true) {
                // ignore the already handled exception.
            } else {
                e.printStackTrace();
                throw new BuildException("Unhandled Exception: " + e.getMessage());
            }
        }
    }

    /**
     * Add a line of text to the query. If there are any properties, they will be replaced.
     *
     * @param text A line of text that should be added to the query.
     */
    public void addText(String text) {
      this.textLines.add(text);
    }

    /**
     * Retrieve the query as a string and replace properties.
     *
     * @return A string with the query.
     */
    public String toString() {
      StringBuffer query = new StringBuffer();
      for (Iterator iterator = this.textLines.iterator(); iterator.hasNext();) {
        String textLine = (String) iterator.next();
        query.append(textLine);
      }
      return getProject().replaceProperties(query.toString());
    }

}
</pre>