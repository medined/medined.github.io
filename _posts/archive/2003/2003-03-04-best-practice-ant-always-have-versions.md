---
layout: post
title: Best Practice; Ant; Always Have a Versions Task
date: '2003-03-04T02:03:00.000-05:00'
author: David Medinets
categories:
- "[[ant]]"
modified_time: '2003-03-04T02:03:44.643-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90403493
blogger_orig_url: http://affy.blogspot.com/2003/03/best-practice-ant-always-have-versions.md
year: 2003
---

This task is designed to display the version of every software application used during the build process. It will mainly
be used when sending a debugging report to vendors.


However, it could also be useful when creating archival information. Here is my implementation of a VERSIONS task:

<pre>
&lt;target name="versions"&gt;
	&lt;!-- DOS version --&gt;
	&lt;exec executable="cmd.exe"&gt;&lt;arg line="/c ver" /&gt;&lt;/exec&gt;
	&lt;!-- ANT version --&gt;
	&lt;echo&gt;${ant.version}&lt;/echo&gt;
	&lt;!-- SolarMetric's KODO version --&gt;
	&lt;java classname="com.solarmetric.kodo.conf.JDOVersion" fork="yes"&gt;
		&lt;classpath&gt;
			&lt;pathelement location="${kodo.jdo.classfile}" /&gt;
			&lt;pathelement location="${kodo.impl.home.dir}\lib\jdo1_0.jar" /&gt;
			&lt;pathelement location="${kodo.impl.home.dir}\lib\serp.jar" /&gt;
			&lt;pathelement location="${kodo.impl.home.dir}\lib\jta-spec1_0_1.jar" /&gt;
			&lt;pathelement location="${kodo.impl.home.dir}\lib\jca1.0.jar" /&gt;
		&lt;/classpath&gt;
	&lt;/java&gt;
&lt;/target&gt;
</pre>
<p>When I run ant versions, here is the resulting output:</p>
<pre>
Buildfile: build.xml

versions:

     [exec] Microsoft Windows XP [Version 5.1.2600]
     [echo] Apache Ant version 1.5 compiled on July 9 2002
     [java] Kodo JDO Enterprise Edition version 2.3.1
     [java] version id: kodojdo-2.3.1-20020821-1709

     [java] os.name: Windows XP
     [java] os.version: 5.1
     [java] os.arch: x86

     [java] java.version: 1.4.0_01
     [java] java.vendor: Sun Microsystems Inc.

     [java] java.class.path:
     [java]     D:\java\jdo\impl\kodo-jdo-2.3.1\lib\kodo-jdoee.jar
     [java]     D:\java\jdo\impl\kodo-jdo-2.3.1\lib\jdo1_0.jar
     [java]     D:\java\jdo\impl\kodo-jdo-2.3.1\lib\serp.jar
     [java]     D:\java\jdo\impl\kodo-jdo-2.3.1\lib\jta-spec1_0_1.jar
     [java]     D:\java\jdo\impl\kodo-jdo-2.3.1\lib\jca1.0.jar

     [java] user.dir: D:\java\WORKSP~1\REFERE~1
</pre>