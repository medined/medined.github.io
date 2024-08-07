---
layout: post
title: Ant; An Example of The Ant Condition Task
date: '2003-01-24T11:55:00.000-05:00'
author: David Medinets
categories:
- "[[ant]]"
modified_time: '2003-01-24T11:57:44.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90229360
blogger_orig_url: http://affy.blogspot.com/2003/01/ant-example-of-ant-condition-task.md
year: 2003
---

I needed to test the condition code returned by stopping the Apache service so that I could delete the log files.


I settled on the following script:

<pre>
  &lt;property name="apache.service"    value="Apache" /&gt;
  &lt;property name="sleep.in.seconds"  value="4" /&gt;

  &lt;target name="shutdown_apache" description="stops the Apache service."&gt;
    &lt;exec executable="c:/WINNT/system32/net.exe" resultproperty="apache.stop.resultproperty" outputproperty="apache.stop.outputproperty"&gt;
      &lt;arg value="stop" /&gt;
      &lt;arg value="${apache.service}" /&gt;
    &lt;/exec&gt;
    &lt;condition property="apache.not.stopped"&gt;
      &lt;equals arg1="${apache.stop.resultproperty}" arg2="2" /&gt;
    &lt;/condition&gt;
    &lt;antcall target="sleep_apache_stop" /&gt;
  &lt;/target&gt;

  &lt;target name="sleep_apache_stop" unless="apache.not.stopped"&gt;
    &lt;echo&gt;Sleeping for ${sleep.in.seconds} seconds.&lt;/echo&gt;
    &lt;sleep seconds="${sleep.in.seconds}" /&gt;
  &lt;/target&gt;
</pre>
