---
layout: post
title: Using Castor and Java to List XPATHs Defined in an XML Schema.
date: '2004-01-13T09:37:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-01-13T09:38:59.840-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-107400463002213956
blogger_orig_url: http://affy.blogspot.com/2004/01/using-castor-and-java-to-list-xpaths.md
year: 2004
theme: java
---

I'm working on a program to validate XML files. An intermediary step was determining all of the potential XPATHs that
the system could run into. So I co-opted the <code>org.exolab.castor.xml.schema.reader</code> package in order to read
the XSD file into Java objects. I was happy to see that Castor's schema reader worked flawlessly - although the
documentation could be improved. The documentation issue is one reason that I'm posting this code.


<pre>
package com.affy;

import java.util.*;
import org.exolab.castor.xml.schema.*;
import org.exolab.castor.xml.schema.reader.*;
import org.exolab.castor.xml.schema.simpletypes.*;
import org.xml.sax.*;

/**
 * This class reads an XSD file to extract XPATHs. Most of the code is simple. However, a Stack is used to track which types of elements are visited
 * so that the program won't infinitely recurse when a container can contain itself. For example, &lt;foo>&lt;bar&gt;&lt;foo/&gt;&lt;/bar&gt;&lt;/foo&gt;
 */
public class XpathFromSchema {

	private static int numXpaths = 0;
	private static Stack visitedTypes = new Stack();

	/** provide a simple method to start the dump. */
	private static void dump(final ElementDecl elementDecl) {
		dump("", elementDecl);
	}

	/** there may be some situations where the starting xpath is actually
	 * a prefix because the starting node can be different from the root node.
	 * In either case, allowing an xpath as a parameter provides flexibility
	 * for unforeseen needs.
	 */
	private static void dump(final String xpath, final ElementDecl elementDecl) {
		if (elementDecl == null) {
			return;
		}
		List forcedXpaths = new ArrayList();
		XMLType typeReference = elementDecl.getType();

		if (typeReference.getName() != null && visitedTypes.contains(typeReference.getName())) {
			// The type is already in the stack, therefore if we were to continue we would infinitely recurse.
		} else {
			if (typeReference.getName() != null) {
				visitedTypes.push(typeReference.getName());
			}

			String newXpath = xpath + "/" + elementDecl.getName();

			System.out.println(numXpaths + ": " + newXpath);
			numXpaths++;

			if (typeReference.isComplexType()) {
				ComplexType ct = (ComplexType)typeReference;
				Enumeration attributes = ct.getAttributeDecls();
				while (attributes.hasMoreElements()) {
					AttributeDecl attributeDecl = (AttributeDecl)attributes.nextElement();
					System.out.println(numXpaths + ": " + newXpath + "/@" + attributeDecl.getName());
					numXpaths++;
				}
				Enumeration particles = ct.enumerate();
				while (particles.hasMoreElements()) {
					Object o = particles.nextElement();
					if (o instanceof Group) {
						dumpGroup(newXpath, (Group)o);
					} else {
						System.out.println(" [dump] ***** Unknown particle type: " + o.getClass().getName());
					}
				}
			}
		}

		if (typeReference.getName() != null && !visitedTypes.empty()) {
			visitedTypes.pop();
		}
	}

	/** I have no idea what a group is, but a little experimentation
	 * showed the follow method to work.
	 */
	public static void dumpGroup(String xpath, final Group group) {
		Enumeration particles = group.enumerate();
		while (particles.hasMoreElements()) {
			Object o = particles.nextElement();
			if (o instanceof Group) {
				dumpGroup(xpath, (Group)o);
			} else if (o instanceof ElementDecl) {
				dump(xpath, (ElementDecl)o);
			} else {
				System.out.println("[dumpGroup] ***** Unknown particle type: " + o.getClass().getName());
			}
		}
	}

	public static void main(String[] args) {
		String xsdFile = "file:///[PUT_YOUR_XSD_FILESPEC_HERE.";

		try {
			SchemaReader a = new SchemaReader(new InputSource(xsdFile));
			Schema s = a.read();

			// since this is a demonstration program, I select the topmost element to
			// start with.
			ElementDecl elementDecl = s.getElementDecl("[NAME_OF_ELEMENT]");
			dump(elementDecl);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}
}
</pre>