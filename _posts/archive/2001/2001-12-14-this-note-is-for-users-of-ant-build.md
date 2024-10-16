---
layout: post
title: How to Find and Execute All JUnit Tests in a Directory Hierarchy
date: '2001-12-14T09:56:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
modified_time: '2007-11-21T12:09:12.733-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7926094
blogger_orig_url: http://affy.blogspot.com/2001/12/this-note-is-for-users-of-ant-build.md
year: 2001
theme: java
---

This note is for users of the Ant build tool. I've modified an AllTests class to automatically add Test classes to a
TestSuite by recursively checking a directory hierarchy for classes with "test" or "Test" in their names.


The customization needed to incorporate these classes into your own system is minimal. Look for "CUSTOMIZE_THIS"
comments.
- change the package names
- change the hard-coded directory and class hierarchy reference.

First is the AllTests.java file:
<pre>
/* CUSTOMIZE_HERE */
package com.cordiem.tests;

import java.util.Enumeration;
import java.util.Vector;
import junit.framework.*;
import junit.runner.BaseTestRunner;
/* CUSTOMIZE_HERE */
import com.cordiem.examples.FindTestClasses;

/**
 * TestSuite that runs all the sample tests. Customize this
 * class to your environment by looking for CUSTOMIZE_HERE
 * comments.
 *
 * @author David Medinets
 */
public class AllTests {

 public static void main(String[] args) {
  junit.textui.TestRunner.run(suite());
 }

 public static Test suite() {
  Vector v = null;

  try {
   FindTestClasses ftc = new FindTestClasses();
/* CUSTOMIZE_HERE */
   ftc.setRootDir("com\\cordiem\\tests");
   ftc.findFiles();
   v = ftc.results();
  } catch (Exception e) {
   e.printStackTrace();
   System.exit(1);
  }

  TestSuite suite = new TestSuite("Framework Tests");

     for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
   Class c = (Class)e.nextElement();
/* CUSTOMIZE_HERE */
   if (! c.getName().equals("com.cordiem.tests.AllTests")) {
    suite.addTestSuite(c);
    System.out.println("Adding " + c.getName() + " to the Test suite.");
   }
  }
  return suite;
 }

}
</pre>

Second is the FindTestClasses.java file:
<pre>
/* CUSTOMIZE_HERE */
package com.cordiem.examples;

import java.io.*;
import java.util.Vector;
import java.lang.reflect.*;

/** Finds classes with "Test" in their names.
 *
 * @author David Medinets
**/
public class FindTestClasses {

 /** the directory to start looking in */
 String rootDir = null;

 /** a place to store the file names. */
 Vector classList = new Vector();

 // TODO: create an excluded directory list.

 /** a do-nothing no-parameter constructor. */
 public FindTestClasses() { }

 public void setRootDir( String inpRootDir ) { rootDir = inpRootDir; }

 public void findFiles() throws Exception {
  build_stack(rootDir);
 }

 public Vector results() {
  return(classList);
 }

 /** The workhorse of this class. It does the job of reading directories and
  * recursing down the directory tree.
  **/
 private void build_stack(String p_directory_name) throws Exception {
  if (null == p_directory_name || 0 == p_directory_name.length()) {
   return;
  }

  // TODO: If the directory being examined should be ignored, then leave this method.

  String[] sFiles = null;
  File     dir    = new File(p_directory_name);

  if (false == dir.canRead()) {
   return;   // silently ignore unreadable directories.
  }

  sFiles = dir.list( new FilterJava() );
  if (null == sFiles) {
   return;
  }

  String filePath = null;
  for (int i = 0; i &lt; sFiles.length; i++) {
   int testIndex = sFiles[i].indexOf("test");
   if (-1 == testIndex) {
    testIndex = sFiles[i].indexOf("Test");
   }
   if (-1 != testIndex) {
    filePath = dir.toString() + (String)System.getProperty("file.separator") + sFiles[i];
    // change slashes to periods and remove the .java from the
    // end of the path to create the class name.
    StringBuffer className = new StringBuffer(replace(filePath, '\\', '.'));
    className.setLength( className.length() - 5);
    try {
     Class c = Class.forName(className.toString());
     classList.add(c);
    } catch (ClassNotFoundException e) {
     throw new Exception("Problem loading class [" + className.toString() + "].");
    }
   }
  }

  // Get list of subdirectories for recursion.
  String[] sDirs = dir.list(new FilterDirectoryOnly());
  for (int i = 0; i &lt; sDirs.length; i++) {
   build_stack(p_directory_name + (String)System.getProperty("file.separator") + sDirs[i]);
  }
 }

 private String replace(String str, char oldChar, char newChar) {
  char curChar;
  StringBuffer sbStr = new StringBuffer(str);
  StringBuffer sbRv = new StringBuffer();

  for (int i=0; i &lt; sbStr.length(); i++) {
   curChar = sbStr.charAt(i);
   if (curChar == oldChar) {
    sbRv.append(newChar);
   } else {
    sbRv.append(curChar);
   }
  }
  return(sbRv.toString());
 }

 /** implements FilenameFilter to find directories
  *
  * @author David Medinets
  */
 private class FilterDirectoryOnly extends Object implements FilenameFilter
 {
  /** a do-nothing no-parameter constructor. */
  public FilterDirectoryOnly() { }

  /** accepts directories. */
  public boolean accept(File dir, String name) {
      File temp = new File(dir + (String)System.getProperty("file.separator") + name);
      return(temp.isDirectory());
  }
 }

 /** implements FilenameFilter to find .java files.
  *
  * @author David Medinets
  */
 private class FilterJava extends Object implements FilenameFilter
 {
  /** a do-nothing no-parameter constructor. */
  public FilterJava() { }

  /** accepts .java files. */
  public boolean accept(File dir, String name) {
      return(name.endsWith(".java"));
  }
 }
}
</pre>