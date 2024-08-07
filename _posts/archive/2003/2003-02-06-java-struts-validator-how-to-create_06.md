---
layout: post
title: Java; Struts Validator; How to Create Field Groups in the Validation XML Files
date: '2003-02-06T11:45:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-02-06T14:25:43.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90286375
blogger_orig_url: http://affy.blogspot.com/2003/02/java-struts-validator-how-to-create_06.md
year: 2003
theme: java
---

We've been using a slightly modified version o the Struts Validator on my project. And we've found that it was neccessary to repeat the field definitions over and over again. Since we have (or will have) several hundred forms I needed a way to reduce the amount of repetition.


I took a low-tech approach that only needed a few lines of code to implement by adding a <b><code>formRefList</code></b> attribute to the <code>form</code> tag. So my XML file might look like this:
<pre>
  &lt;form name="FieldGroupOne"&gt;
    &lt;field property="STATUS /&gt;
    &lt;field property="NAME" depends="alphanumeric,maxLength" /&gt;
  &lt;/form&gt;

  &lt;form name="FormOne" formRefList="FieldGroupOne"&gt;
    &lt;field property="NAME" depends="required" /&gt;
  &lt;/form&gt;
</pre>
The result of my modfication is that the <code>FormOne</code> form has two identically-named fields both of which are evaluated when it comes time to validate the field.
<br>
The following description of how I implemented my change assumed that you already know how to compile the Validator project. Also note that errors are simply handled by printing to STDOUT because I use a JUnit test that is run during an automated build and regression test to ensure that the XML files are correct. So the error checking code should never be implemented.
<ol>
<li>Add <code>public FastHashMap getHForms() { return hForms; }</code> to the FormSet class.
<li>Add <code>protected String formRefList;</code> with the appropriate getter and setter methods.</li>
<li>After the XML files are processed, run the following code:
<pre>
		// The Formsets are stored under Locale keys. Store
		// the form object into a hashmap for later processing.
		Iterator iFormSets = ((Vector)resources.getValidatorFormSet().get("en_US")).iterator();
		while (iFormSets.hasNext()) {
			FormSet fs = ((FormSet)iFormSets.next());
			Iterator iForms = fs.getHForms().keySet().iterator();
			while (iForms.hasNext()) {
				String formName = (String)iForms.next();
				formObjects.put(formName, fs.getHForms().get(formName));
			}
		}

		// Now iterate over the forms looking for formRefList
		// attributes.
		Iterator iForms = formObjects.keySet().iterator();
		while (iForms.hasNext()) {
			String formName = (String)iForms.next();
			Form form = (Form)formObjects.get(formName);
			String formRefList = form.getFormRefList();
			if (formRefList != null && formRefList.length() > 0) {
				//System.out.println("Before processing " + form);
				StringTokenizer st = new StringTokenizer(formRefList);
				while (st.hasMoreTokens()) {
					String referencedFormName = st.nextToken();
					Form referencedForm = (Form)formObjects.get(referencedFormName);
					if (referencedForm == null) {
						System.out.println("No object for referenced form [" + referencedFormName + "].");
					} else {
						List lReferencedFields = referencedForm.getFields();
						if (lReferencedFields == null) {
							System.out.println("No fields in referenced form [" + referencedFormName + "].");
						} else {
							Iterator iReferencedFields = lReferencedFields.iterator();
							while (iReferencedFields.hasNext()) {
								Field referencedField = (Field)iReferencedFields.next();
								form.addField(referencedField);
							}
						}
					}
				}
				//System.out.println("After processing Form " + form);
			}
		}
</pre>
</li>
</ol>
