---
layout: post
title: Embedded Tomcat 5.5.17 Eclipse Project
date: '2006-10-31T16:28:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-10-31T17:34:10.756-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116233404897048512
blogger_orig_url: http://affy.blogspot.com/2006/10/embedded-tomcat-5517-eclipse-project.md
year: 2006
theme: java
---

I like to use Tomcat because of its simplicity. I'm sure that there are many reasons to use the embedded version of
Tomcat, I'm using it because I can start a vanilla Tomcat engine in under one second. Additionally, it's easy to place
all of the configuration files under version control when all of the files are part of a Eclipse project.


You can download a Zip file containing a working example of embedded Tomcat by clicking the title above. Make sure that
you edit the .classpath file and change the location of the Jar files to whereever you have installed Tomcat.

If you want to follow the steps that I took instead of downloading the zip file, here they are:

1. Create a Java class to start Tomcat:
<pre>package com.affy;
import org.apache.catalina.startup.Bootstrap;
public class EmbeddedTomcat {
 public static void main(String[] args) throws Exception {
  Bootstrap bootStrap = new Bootstrap();
  bootStrap.init();
  bootStrap.start();
 }
}</pre>
2. Update your classpath so it contains the following jar files.
<pre>TOMCAT_HOME/bin/bootstrap.jar
TOMCAT_HOME/bin/commons-logging-api.jar
TOMCAT_HOME/common/lib/commons-el.jar
TOMCAT_HOME/common/lib/jasper-compiler.jar
TOMCAT_HOME/common/lib/jasper-compiler-jdt.jar
TOMCAT_HOME/common/lib/jasper-runtime.jar
TOMCAT_HOME/common/lib/jsp-api.jar
TOMCAT_HOME/common/lib/naming-factory.jar
TOMCAT_HOME/common/lib/naming-resources.jar
TOMCAT_HOME/common/lib/servlet-api.jar
TOMCAT_HOME/server/lib/catalina.jar
TOMCAT_HOME/server/lib/catalina-cluster.jar
TOMCAT_HOME/server/lib/catalina-storeconfig.jar
TOMCAT_HOME/server/lib/commons-modeler.jar
TOMCAT_HOME/server/lib/servlets-default.jar
TOMCAT_HOME/server/lib/tomcat-ajp.jar
TOMCAT_HOME/server/lib/tomcat-coyote.jar
TOMCAT_HOME/server/lib/tomcat-util.jar
TOMCAT_HOME/server/lib/tomcat-http.jar</pre>

3. Create a <code>conf</code> directory with the following files (copied directly from the <code>conf</code> in
TOMCAT_HOME.
<pre>catalina.policy
catalina.properties
context.xml
logging.properties
server.xml
tomcat-users.xml
web.xml</pre>

4. Create the <code>webapps</code> and <code>webapps/ROOT</code> directories.

5. Create a <code>webapps/ROOT/index.html</code> file with any content you'd like.

6. Run the EmbeddedTomcat Java program. This step will automatically create a <code>work</code> directory.

7. Connect to http://localhost:8080/ with your browser and you should see the index.html page that you created in step
5.

Good luck and Have Fun playing with your embedded Tomcat!