---
layout: post
title: How do I Set the CLASSPATH Variable Using Linux?
date: '2003-03-03T17:47:00.000-05:00'
author: David Medinets
categories:
- "[[bash]]"
modified_time: '2003-03-03T17:47:51.130-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90401909
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-do-i-set-classpath.md
year: 2003
---

How do I Set the CLASSPATH Variable Using Linux?


<p>Create a file called <code>/etc/profile.d/jdk.sh</code> and fill it with this:</p>
<pre>
  if [ -z "${JAVA_HOME}" ] ; then
    export JAVA_HOME=/usr/lib/jdk116
  fi
  if [ -z "${JDK_HOME}" ] ; then
    export JDK_HOME=/usr/lib/jdk116
  fi
  SW=`cat /etc/profile.d/java`
  SW=`echo ${SW}  | sed -e "s/ /:/g"`
  MYPATH="${JDK_HOME}/lib/classes.zip:${SW}"
  export CLASSPATH="${MYPATH}"
  MYPATH="${JDK_HOME}/bin/"
  echo ":${PATH}:"| grep "${MYPATH}" &gt;/dev/null ||\
    PATH="${PATH}:${MYPATH}"

  export PATH
  unset SW MYPATH
</pre>
<p>Create a file called <code>/etc/profile.d/javaM</code> and fill it with a list of directories, jars, and zips.</p>