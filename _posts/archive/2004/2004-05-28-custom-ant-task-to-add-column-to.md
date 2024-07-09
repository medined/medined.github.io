---
layout: post
title: Custom Ant Task to Add Column To Oracle Database Table
date: '2004-05-28T15:30:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
modified_time: '2006-03-07T11:08:12.013-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108577293695807570
blogger_orig_url: http://affy.blogspot.com/2004/05/custom-ant-task-to-add-column-to.md
year: 2004
theme: java
---

<p>This Ant task adds a column to an Oracle database table if it does not already exist. The
    <code>ReadWimConfiguration</code> class simply sets the Oracle URL, username, and password. The
    <code>OracleBootstrap</code> class gets an Oracle DataSource object and provides a method to get a connection. I can
    post that class if needed.
</p>


<pre>
package org.wwre.ant.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.wwre.common.ReadWimConfiguration;
import org.wwre.datastore.oracle.OracleBootstrap;

/**
 * This class adds a column to a database table. The column is only
 * added if it does not already exist.
 *
 */
public class AddDatabaseColumnTask extends Task {

    private String          table  = null;

    private String          column = null;

    private String          format = null;

    /**
     * The method executing the task.
     */
    public void execute() throws BuildException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (ReadWimConfiguration.isRead() == false) {
                throw new BuildException("Please call the readWimConfiguration task.");
            }

            if (getTable() == null) {
                throw new BuildException("Please specify the table attribute.");
            }

            if (getColumn() == null) {
                throw new BuildException("Please specify the column attribute.");
            }

            String sql = "SELECT column_name FROM user_tab_columns WHERE table_name = '" + getTable() + "' AND column_name = '" + getColumn() + "'";

            OracleBootstrap.initialize();

            try {
                con = OracleBootstrap.getConnection();
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next() == false) {
                    ps = con.prepareStatement("ALTER TABLE " + getTable() + " ADD (" + getColumn() + " " + this.format + ")");
                    rs = ps.executeQuery();
                }

            } catch (SQLException e) {
                boolean validError = false;

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
            e.printStackTrace();
            throw new BuildException("Unhandled Exception: " + e.getMessage());
        }
    }

    /**
     * @return Returns the column.
     */
    private String getColumn() {
        if (this.column == null)
            return null;
        else
            return this.column.trim().toUpperCase();
    }

    /**
     * @param _format The format to set.
     */
    public void setFormat(String _format) {
        this.format = _format;
    }

    /**
     * @return Returns the table.
     */
    private String getTable() {
        if (this.table == null)
            return null;
        else
            return this.table.trim().toUpperCase();
    }

    /**
     * @param _table The table to set.
     */
    public void setTable(String _table) {
        this.table = _table;
    }
}
</pre>