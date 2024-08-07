---
layout: post
title: CFMX and Java Servlet Integration
date: '2002-12-09T23:11:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2007-11-21T12:09:46.177-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90033334
blogger_orig_url: http://affy.blogspot.com/2002/12/cfmx-and-java-servlet-integration.md
year: 2002
theme: java
---

Recently, I was investigating how to integrate CFMX and Java Servlets.


I found that I needed to cycle the CFMX application after each change to my Java classes. In order to make this process
less onerous I developed the following ant build file. You'll notice that the CFMX log files are deleted. This was done
so that I didn't have to wade through large files in order to find the messages related to whatever change I was making
at the time. You might also notice that I've added a four second delay before each delete task. This delay gives the
operating system time to write its buffers to disk and close the files after the service has been stopped.</p>
<pre>
&lt;project name="dispatcher" default="" basedir="."&gt;

    &lt;property name="cfmx.home"            value="c:/CFusionMX" /&gt;
    &lt;property name="cfmx.runtime-log.dir" value="${cfmx.home}/runtime/logs" /&gt;
    &lt;property name="cfmx.log.dir"         value="${cfmx.home}/logs" /&gt;
    &lt;property name="cfmx.service"         value="ColdFusion MX Application Server" /&gt;

    &lt;property name="apache.home"          value="D:/Program Files/Apache Group/Apache" /&gt;
    &lt;property name="apache.service"       value="Apache" /&gt;
    &lt;property name="apache.log.dir"       value="${apache.home}/logs" /&gt;

    &lt;property name="login.url"            value="http://localhost/login.cfm?userid=david&amp;password=password" /&gt;
    &lt;property name="temp.dir"             value="${user.home}/Temp" /&gt;

    &lt;target name="restart_cfmx" description="Deploys Dispatcher servlet to CFMX."&gt;
        &lt;exec executable="c:/WINNT/system32/net.exe"&gt;
            &lt;arg value="stop" /&gt;
            &lt;arg value="${cfmx.service}" /&gt;
          &lt;/exec&gt;

        &lt;echo&gt;Sleeping for 4 seconds.&lt;/echo&gt;
        &lt;sleep seconds="4"/&gt;

        &lt;!-- delete CFMX log files --&gt;
        &lt;delete&gt;
            &lt;fileset dir="${cfmx.log.dir}" includes="*.log"/&gt;
            &lt;fileset dir="${cfmx.runtime-log.dir}" includes="*.log"/&gt;
        &lt;/delete&gt;

        &lt;exec executable="c:/WINNT/system32/net.exe"&gt;
            &lt;arg value="start" /&gt;
            &lt;arg value="${cfmx.service}" /&gt;
        &lt;/exec&gt;

        &lt;echo&gt;Sleeping for 4 seconds.&lt;/echo&gt;
        &lt;sleep seconds="4"/&gt;

        &lt;get src="${login.url}" dest="${temp.dir}" /&gt;
        &lt;echo&gt;Done!&lt;/echo&gt;
    &lt;/target&gt;

    &lt;target name="restart_apache" description="Deploys Dispatcher servlet to CFMX."&gt;
        &lt;exec executable="c:/WINNT/system32/net.exe"&gt;
            &lt;arg value="stop" /&gt;
            &lt;arg value="${cfmx.service}" /&gt;
        &lt;/exec&gt;

        &lt;echo&gt;Sleeping for 4 seconds.&lt;/echo&gt;
        &lt;sleep seconds="4"/&gt;

        &lt;!-- delete CFMX log files --&gt;
        &lt;delete&gt;
            &lt;fileset dir="${cfmx.log.dir}" includes="*.log"/&gt;
            &lt;fileset dir="${cfmx.runtime-log.dir}" includes="*.log"/&gt;
        &lt;/delete&gt;

        &lt;exec executable="c:/WINNT/system32/net.exe"&gt;
            &lt;arg value="stop" /&gt;
            &lt;arg value="${apache.service}" /&gt;
        &lt;/exec&gt;

        &lt;echo&gt;Sleeping for 4 seconds.&lt;/echo&gt;
        &lt;sleep seconds="4"/&gt;

        &lt;!-- delete Apache log files --&gt;
        &lt;delete&gt;
            &lt;fileset dir="${apache.log.dir}" includes="*.log"/&gt;
        &lt;/delete&gt;

        &lt;exec executable="c:/WINNT/system32/net.exe"&gt;
            &lt;arg value="start" /&gt;
            &lt;arg value="${apache.service}" /&gt;
        &lt;/exec&gt;

        &lt;exec executable="c:/WINNT/system32/net.exe"&gt;
            &lt;arg value="start" /&gt;
            &lt;arg value="${cfmx.service}" /&gt;
        &lt;/exec&gt;

        &lt;echo&gt;Sleeping for 4 seconds.&lt;/echo&gt;
        &lt;sleep seconds="4"/&gt;

        &lt;get src="${login.url}" dest="${temp.dir}" /&gt;
        &lt;echo&gt;Done!&lt;/echo&gt;
        &lt;/target&gt;

&lt;/project&gt;

</pre>