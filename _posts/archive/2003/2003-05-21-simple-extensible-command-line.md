---
layout: post
title: A Simple Extensible Command-Line Interpreter in Java
date: '2003-05-21T08:47:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-05-21T08:55:57.000-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200321380
blogger_orig_url: http://affy.blogspot.com/2003/05/simple-extensible-command-line.md
year: 2003
theme: java
---

I've found command-line interpreters are useful tools. Especially interpreter's that can read commands from both files
and keyboard.
I've used such interpreter's for printer manipulation, database and object creation, and other tasks. I can hear someone
saying, 'Use Ant', but creating Ant tasks can be a daunting proposition at first. So the class presented here is simple,
simple, simple so that beginning java developers can use it.


<p>
	This simple command-line interpreter is extensible and easy-to-use as
	shown in test code below. The source code for the command line class is
	about 500 lines and is available at <a
		href="http://www.codebits.com/java/CommandLine.java">http://www.codebits.com/java/CommandLine.java</a>.
</p>
<hr>
CommandLineTest.java
<hr>
<pre>
package com.affy.utils.cmdline;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Vector;

/**
 * This class shows how the CommandLine class
 * can be used. It initialized a CommandLine object
 * and defines a FOO command. Notice that this class
 * (CommandLineTest) acts as both test harness and
 * command executor. When run, you'll be presented with
 * a command-line prompt. Type 'foo bar baz' and you'll
 * see the output from the doIt() method. Type 'aaa aaa aaa'
 * and you'll see an error message. Command-line parameters
 * with spaces need to be quoted.
 *
 * @author medined
 * Created on May 20, 2003
 */
public class CommandLineTest
implements CommandLine.ICommand {

	/**
	 * Shows how to use the CommandLine class.
	 * @param args The command-line parameters
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args)
	throws FileNotFoundException {
		// This technique of reading from either
		// a script file or
		// from the console was 'borrowed' from BeanShell.
		Reader inputSrc = null;
		if ( args.length > 0 ) {
			inputSrc = new BufferedReader(
				new FileReader(args[0])
			);
		} else {
			inputSrc = new InputStreamReader(System.in);
		}

		// initialize the command line object.
		CommandLine jr = new CommandLine();
		jr.setCommandLinePrompt("Command> ");
		jr.setCommandLineVersion("Command Line v.01");
		jr.assignClassToCommnd("foo",
			"com.affy.utils.cmdline.CommandLineTest");
		jr.init();
		if ( args.length > 0 ) {
			jr.setIsInteractive(false);
		}

		// parse and execute commands.
		jr.parseStream(new StreamTokenizer(inputSrc));

		System.out.println("\nDone.");
	}

	/**
	 * This method is invoked when the FOO command is
	 * used.
	 */
	public boolean doIt(Vector v) {
		System.out.println(
			"Inside CommandLineTest.doIt(); v="
			+ v
		);
		return true;
	}
}
</pre>