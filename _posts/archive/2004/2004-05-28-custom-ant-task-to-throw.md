---
layout: post
title: Custom Ant Task to Throw BuildException if Specified Ant Property is Not Defined
date: '2004-05-28T15:35:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
modified_time: '2006-04-19T18:08:16.706-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108577317845331196
blogger_orig_url: http://affy.blogspot.com/2004/05/custom-ant-task-to-throw.md
year: 2004
theme: java
---

<p>This Ant task throws a BuildException if a specified Ant property is not defined. I use it like this:</p>


<pre>
    &lt;target name="generateXlnServerPropertyFile"&gt;
        &lt;checkDefinedAntProperty propertyName="classpath.delimiter"/&gt;
        &lt;checkDefinedAntProperty propertyName="velocity.template.directory"/&gt;
        &lt;checkDefinedAntProperty propertyName="xlnserver.property.file"/&gt;
        &lt;generateXlnServerPropertyFile .../&gt;
    &lt;/target&gt;
</pre>

<pre>
package org.wwre.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This class configures the XIS server for WIM use.
 *
 */
public class CheckDefinedAntPropertyTask extends Task{

    private String propertyName = null;

    /**
     * The method executing the task.
     *
     * @throws BuildException If srcdir or classname are null. This exception is also thrown
     * if the variable cannot be found, The java file does not exist,
     */
    public void execute() throws BuildException {

        if (getPropertyName() == null) {
            throw new BuildException("Please set the propertyName attribute.");
        }

        String propertyValue = getProject().getProperty(getPropertyName());

        if (propertyValue == null) {
            throw new BuildException("Please define property[" + getPropertyName() + "].");
        }

    }

    /**
     * @return Returns the propertyName.
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * @param _propertyName The propertyName to set.
     */
    public void setPropertyName(String _propertyName) {
        this.propertyName = _propertyName;
    }
}
</pre>