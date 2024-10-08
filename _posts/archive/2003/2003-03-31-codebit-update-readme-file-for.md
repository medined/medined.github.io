---
layout: post
title: A ReadMe file for the Simplest Bean.
date: '2003-03-31T10:20:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-31T10:20:45.540-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200073007
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-update-readme-file-for.md
year: 2003
theme: java
---

In response to an email request, I've added a README.txt file to <a href="/assets/SimpleBean.zip">SimpleBean.zip</a> to
indicate how to use the <code>build.xml</code> file and what the output looks like.


Here it is for those who don't want to grab the zip file for just one file:
<pre>
How to Test the SimpleBean Class
--------------------------------
1. Edit build.xml file to customize the properties at the beginning of the file.

2. Create an environmental variable called JBOSS_HOME that points to the
installation directory of JBOSS.

	set JBOSS_HOME=D:\java\support\jboss-3.0.6_tomcat-4.1.18   # example
	set JBOSS_CONFIG=test

3. Create a clone of the %JBOSS_HOME%/server/default directory so that we don't
affect the standard installation.

	xcopy
	  %JBOSS_HOME%\server\default
	  %JBOSS_HOME%\server\%JBOSS_CONFIG%
	  /E /I /F

4. Start JBOSS from the DOS command-line.

	%JBOSS_HOME%/bin/run.bat -c test

5. Deploy the bean to JBoss using the "deploy" task. Note that I changed the
log4j options of JBoss so that the SQL statements that it executes are
displayed on the console as DEBUG messages.

	Output From Ant Task
	--------------------

	Buildfile: d:\java\workspace\SimpleBean\build.xml

	check-environment:

	check-ant:

	wrong-ant:

	check-java:

	wrong-java:

	check-jboss:

	wrong-jboss:

	check-jboss-configuration:

	wrong-jboss-configuration:

	check-ejb-jar:

	check-jakarta-logging-jar:

	check-jboss-version:

	check-log4j-jar:

	check-xdoclet:

	wrong-xdoclet:

	init:

	xdoclet-generate:
	   	[ejbdoclet] Running <remoteinterface/>
   		[ejbdoclet] Generating Remote interface for 'com.affy.entity.SimpleBean'.
		[ejbdoclet] Running <homeinterface/>
		[ejbdoclet] Generating Home interface for 'com.affy.entity.SimpleBean'.
		[ejbdoclet] Running <localinterface/>
		[ejbdoclet] Generating Local interface for 'com.affy.entity.SimpleBean'.
		[ejbdoclet] Running <localhomeinterface/>
		[ejbdoclet] Generating Local Home interface for 'com...SimpleBean'.
		[ejbdoclet] Running <utilobject/>
		[ejbdoclet] Generating Util class for 'com.affy.entity.SimpleBean'.
		[ejbdoclet] Running <deploymentdescriptor/>
		[ejbdoclet] Generating EJB deployment descriptor (ejb-jar.xml).
		[ejbdoclet] Running <jboss/>
		[ejbdoclet] Generating jboss.xml.
		[ejbdoclet] Generating jbosscmp-jdbc.xml.
        [echo] Done!

	compile:
        [echo] Compiling Generated Files.
       [javac] Compiling 5 source files to D:\...\classes
        [echo] Compiling EJB Files.
       [javac] Compiling 1 source file to D:\...\classes
        [echo] Compiling Client Files.
       [javac] Compiling 1 source file to D:\...\classes
        [echo] Making Jar File.
         [jar] Building jar: D:\...\build\deploy\SimpleBean.jar

	deploy:
        [echo] Copying Jar File to Hot Deploy Directory.
        [copy] Copying 1 file to D:\...\server\test\deploy

	BUILD SUCCESSFUL
	Total time: 14 seconds

	Output From JBoss Server
	------------------------
	00:31:24,940 INFO  [MainDeployer] Starting deployment of package:
		file:/D:.../SimpleBean.jar
	00:31:25,541 INFO  [EjbModule] Creating
	00:31:25,561 INFO  [EjbModule] Deploying SimpleBean
	00:31:25,641 INFO  [EjbModule] Created
	00:31:25,641 INFO  [EjbModule] Starting
	00:31:25,691 DEBUG [SimpleBean] Initializing CMP plugin for SimpleBean
	00:31:25,701 DEBUG [SimpleBean] Loading standardjbosscmp-jdbc.xml :
		file:/D:.../server/test/conf/standardjbosscmp-jdbc.xml
	00:31:25,881 DEBUG [SimpleBean] jar:file:/D:/.../META-INF/jbosscmp-jdbc.xml
		found. Overriding defaults
	00:31:26,042 DEBUG [SimpleBean] Entity Exists SQL:
		SELECT COUNT(*) FROM SIMPLEBEAN WHERE pKey=?
	00:31:26,052 DEBUG [SimpleBean] Insert Entity SQL:
		INSERT INTO SIMPLEBEAN (pKey) VALUES (?)
	00:31:26,052 DEBUG [SimpleBean] Remove SQL:
		DELETE FROM SIMPLEBEAN WHERE pKey=?
	00:31:26,242 INFO  [SimpleBean] Table 'SIMPLEBEAN' already exists
	00:31:26,252 DEBUG [findByPrimaryKey] SQL:
		SELECT pKey FROM SIMPLEBEAN WHERE pKey=?
	00:31:26,252 DEBUG [SimpleBean]
		Added findByPrimaryKey query command for home interface
	00:31:26,252 DEBUG [findByPrimaryKey] SQL:
		SELECT pKey FROM SIMPLEBEAN WHERE pKey=?
	00:31:26,252 DEBUG [SimpleBean]
		Added findByPrimaryKey query command for local home interface
	00:31:26,252 INFO  [EjbModule] Started
	00:31:26,252 INFO  [MainDeployer] Deployed package: file:/D:/...SimpleBean.jar

6. Run the SimpleBeanClient program. I run it using the
Run->Run As->Java Application menu option of Eclipse. For some reason, I am
unable to run it from Ant because I get the following error:

     [java] java.lang.IllegalAccessException: Class
     org.apache.tools.ant.taskdefs.ExecuteJava can not access a member of
     class com.affy.client.SimpleBeanClient with modifiers "public static"

I don't want to take time to figure out what's wrong, so I'm content to run
the client from inside Eclipse. After all, the IDE should make my life easier, no?

Here is the output of the client program:

	ID: 446ced64c0a822650064160eae80b6fd
	ID: 446ced64c0a822650064160eae80b6fd
	Done.

The ID displayed by your execution of the program will, of course, be
different.

7. That's it. Have Fun!
</pre>