---
layout: post
title: Creating an Ant Task with Eclipse
date: '2003-10-03T11:13:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
- "[[eclipse]]"
modified_time: '2003-10-03T11:13:26.730-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-106519400668551770
blogger_orig_url: http://affy.blogspot.com/2003/10/creating-ant-task-with-eclipse.md
year: 2003
theme: java
---

Creating an Ant Task with Eclipse


<ol>
    <li>Add ant.jar to your eclipse project.
    <li>Create package to hold task classes. I recommend xxx.yyy.ant.tasks
    <li>Create task class. Here is a simple one:
        <pre>
package com.affy.ant.tasks;
import org.apache.tools.ant.*;
public class HelloWorld extends Task {
    public void execute() throws BuildException {
        System.out.println("Hello World!");
    }
}
</pre>
    <li>Select Window, Preferences, Ant, Classpath
    <li>Set ANT_HOME
    <li>Add the folder where your class files are stored (xxx/build/classes, for example)
    <li>Select Task, Add Task
    <li>Assign a task name (ie, HelloWorld) to use in build.xml
    <li>Select location (the folder xxx/build/classes for example)
</ol>
<p>That's how easy it is to start writing your own Ant tasks. If you're going to invoke build.xml from within Eclipse,
    then you only need the following lines:
<pre>
    &lt;target name="HelloWorld"&gt;
        &lt;HelloWorld/&gt;
    &lt;/target&gt;
</pre>
</p>
<p>Otherwise, you need to tell Ant where to find the class file. For example,
<pre>
    &lt;path id="cp"&gt;
        &lt;pathelement path="xxx/build/classes"/&gt;
    &lt;/path&gt;
    &lt;target name="HelloWorld"&gt;
        &lt;taskdef name="HelloWorld" classname="org.wwre.ant.tasks.HelloWorld" classpathref="cp"/&gt;
        &lt;HelloWorld/&gt;
    &lt;/target&gt;
</pre>
</p>