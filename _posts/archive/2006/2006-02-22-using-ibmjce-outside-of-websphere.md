---
layout: post
title: Using IBMJCE Outside of Websphere
date: '2006-02-22T09:21:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-03-07T13:38:10.863-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-114061858875175157
blogger_orig_url: http://affy.blogspot.com/2006/02/using-ibmjce-outside-of-websphere.md
year: 2006
theme: java
---

In order to run unit tests involving encryption, I needed to compile my code with the same JCE providers available
inside Websphere.


The IBMJCE Java classes are located in <i>[install_dir]\java\jre\lib\ext\ibmjceprovider.jar</i>. However those classes
have a dependency on com.ibm.misc.Debug. References on the internet indicated that the Debug class was located in
ibmpkcs.jar but I did not have such a file on my computer.

<p>I found the Debug class in <i>[install_dir]\java\jre\lib\security.jar</i>. In order to find out which jar file
  contained the needed class, I modified the startserver.bat file by adding '-verbose:class' to the Java command line.
  Additionally, I installed the 4NT utility from JPSOFT.com which allowed me to redirect the console output to a file
  which I examined to find the following line:</p>
<pre>
  [Loaded com.ibm.misc.BASE64Decoder from .....\security.jar]
</pre>

<p>Since the com.ibm.misc was the package I was interested in I unziped security.jar to see if it contained the Debug
  class. It did!</p>