---
layout: post
title: Rational ClearCase; Java; a utlity to recursively add files to ClearCase.
date: '2003-01-23T19:40:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-01-23T19:43:08.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90226546
blogger_orig_url: http://affy.blogspot.com/2003/01/rational-clearcase-java-utlity-to.md
year: 2003
theme: java
---

The inability to add entire directories to ClearCase annoyed me endlessly. I didn't know about the <code>clearfsimport</code> command.


And even if I did, I might have written my own utility just for fun. In either case, the code below performs the task. The underlying interaction with ClearCase is handled by the <a href="http://sourceforge.net/projects/clearcase-java/">http://sourceforge.net/projects/clearcase-java/</a> project. If you need to use the ClearcaseCLI class instead of ClearcaseJNI then simply change the <code>import</code> line and the <code>start</code> method. Have Fun!
<pre>
package com.cordiem.clearcase;

import java.io.*;
import java.util.*;
import net.sourceforge.clearcase.api.IClearcase;
import net.sourceforge.clearcase.api.IClearcase.Status;
import net.sourceforge.clearcase.simple.ClearcaseJNI;

/**
 * @author david medinets
 */
public class AddToClearCase {

	String dirName  = null;
	IClearcase ccase = null;

	public AddToClearCase() { }
	public AddToClearCase(String dirName) { setDirName(dirName); }

	private class FilterAllFiles extends Object implements FilenameFilter {
		public FilterAllFiles() { }

		public boolean accept(File dir, String name) {
			return true;
		}
	}

	public void start() {
		ccase = new ClearcaseJNI();

		/* Convert the directory hierarchy into a
		 * stack so that it can be easily traversed.
		 */
		File f = new File(getDirName());
		Stack s = new Stack();
		do {
			s.add(f);
			f = f.getParentFile();
		} while (f != null);

		/*
		 * Try to check on all levels of the directory
		 * hierarchy. If one of them can be checked in
		 * then I assume that it's ok to add files to
		 * this tree.
		 */
		boolean checkedIn = false;
		do {
			f = (File)s.pop();
			if (ccase.isElement(f.toString()) == false) {
				addToClearCase(f);
				checkedIn = CheckoutFromClearCase(f);
			} else {
				if (ccase.isElement(f.toString()) == true && ccase.isCheckedOut(f.toString()) == true) {
					checkedIn = true;
				} else {
					checkedIn = CheckoutFromClearCase(f);
				}
			}
		} while (s.empty() == false);

	}

	private void addToClearCase(File f) {
		if (false == ccase.isElement(f.toString())) {
			System.out.println("adding: " + f.toString());
			Status status = ccase.add(f.toString(), "", f.isDirectory());
			if (status.status == false)
				System.out.println("m: " + status.message);
		}
	}

	private boolean CheckoutFromClearCase(File f) {
		boolean rv = false;
		if (ccase.isElement(f.toString()) == true && ccase.isCheckedOut(f.toString()) == false) {
			System.out.println("checking out: " + f.toString());
			Status status  = ccase.checkout(f.toString(), "", true, false);
			if (status.status == false)
				System.out.println("m: " + status.message);
			else
				rv = true;
		}
		return rv;
	}

	private void CheckinToClearCase(File f) {
		if (ccase.isElement(f.toString()) == true && ccase.isCheckedOut(f.toString()) == true) {
			System.out.println("checking in: " + f.toString());
			Status status  = ccase.checkin(f.toString(), "", false);
			if (status.status == false)
				System.out.println("m: " + status.message);
		}
	}

	// Check the directory hierarchy back into ClearCase.
	public void stop() {
		/* Convert the directory hierarchy into a
		 * stack so that it can be easily traversed.
		 */
		File f = new File(getDirName());
		do {
			CheckinToClearCase(f);
			f = f.getParentFile();
		} while (f != null);

	}

	public void process() {
		process(new File(getDirName()));
	}

	public void process(File path) {
		this.addToClearCase(path);

		if (path.isDirectory() == true)
			this.CheckoutFromClearCase(path);

		String[] sDirs = path.list(new FilterAllFiles());
		if (sDirs != null) {
			for (int i = 0; i < sDirs.length; i++) {
				File newPath = new File(path.toString() + "/" + sDirs[i]);
				System.out.println(newPath.toString());
				this.addToClearCase(newPath);
				if (true == newPath.isDirectory()) {
					CheckoutFromClearCase(newPath);
					// recurse to handle subdirectories.
					process(newPath);
				}

				// The JNI interaction with ClearCase leaves files checked out
				// after adding. And the directory also is checked out. So
				// regardless, the element needs to be checked in at this point.
				this.CheckinToClearCase(newPath);
			}
		}

	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: AddToClearCase [initialDir]");
		} else {
			System.out.println("Processing directory [" + args[0] + "]");
			AddToClearCase c = new AddToClearCase(args[0]);
			c.start();
			c.process();
			c.stop();
			System.out.println("Done!");
		}
	}

	public String getDirName() { return dirName; }
	public void setDirName(String dirName) { this.dirName = dirName; }
	public void setCcase(IClearcase ccase) { this.ccase = ccase; }
}
</pre>
