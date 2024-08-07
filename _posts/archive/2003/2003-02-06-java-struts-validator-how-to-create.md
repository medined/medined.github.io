---
layout: post
title: Java; Struts Validator; How to Create Standard Field Definitions in the Validation XML Files
date: '2003-02-06T14:25:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-02-06T14:25:05.036-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90287430
blogger_orig_url: http://affy.blogspot.com/2003/02/java-struts-validator-how-to-create.md
year: 2003
theme: java
---

Hopefully, you read my last post about creating Field Groups. This post shows how to create a standard field defnitions that can reused over and over in form definitions.


My field definitions look like this in my XML:
<pre>
  &lt;form name="FieldDefinitions"&gt;
    &lt;field property="PROJECT_NAME" depends="required,alphanumeric,maxLength"&gt;
       &lt;arg0 key="attributes.project_name.displayname"/&gt;
       &lt;var&gt;&lt;var-name&gt;maxLength&lt;/var-name&gt;&lt;var-value&gt;100&lt;/var-value&gt;&lt;/var&gt;
       &lt;var&gt;&lt;var-name&gt;allowSpaces&lt;/var-name&gt;&lt;var-value&gt;T&lt;/var-value&gt;&lt;/var&gt;
    &lt;/field&gt;
    &lt;field property="PROJECT_DESCRIPTION" depends="alphanumeric"&gt;
       &lt;arg0 key="attributes.project_description.displayname"/&gt;
       &lt;var&gt;&lt;var-name&gt;maxLength&lt;/var-name&gt;&lt;var-value&gt;1000&lt;/var-value&gt;&lt;/var&gt;
       &lt;var&gt;&lt;var-name&gt;allowSpaces&lt;/var-name&gt;&lt;var-value&gt;T&lt;/var-value&gt;&lt;/var&gt;
    &lt;/field&gt;
  &lt;/form&gt;

  &lt;form name="FormOne" &gt;
    &lt;field property="PROJECT_NAME" depends="required" fieldRef="PROJECT_NAME" /&gt;
  &lt;/form&gt;
</pre>
The result of my modification is that the <code>depends</code> attribute of the Field Definition is appended to the <code>depends</code> information of the <code>PROJECT_NAME</code> field. Additionally the <code>arg0</code> element and <code>var</code> elements are copied to the <code>PROJECT_NAME</code> field.
<br>
The following description of how I implemented my change assumed that you already know how to compile the Validator project. Also note that errors are not handled because I use a JUnit test that is run during an automated build and regression test to ensure that the XML files are correct. So the error checking code should never be implemented.
<br>
<ol>
<li>Add <code>public FastHashMap getHForms() { return hForms; }</code> to the FormSet class.
<li>Add <code>protected String fieldRef;</code> with the appropriate getter and setter methods to the Field class.
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

    Form fieldDefinitions = resources.get(Locale.getDefault(), "FieldDefinitions");
    Map fieldDefinitionMap = fieldDefinitions.getFieldMap();

    // Now iterate over the forms looking for formRefList
    // attributes.
    Iterator iForms = formObjects.keySet().iterator();
    while (iForms.hasNext()) {
      String formName = (String)iForms.next();
      Form form = (Form)formObjects.get(formName);
      // see if any fields refer to field definitions.
      List lFields = form.getFields();
      if (lFields == null) {
        System.out.println("No fields in form [" + formName + "].");
      } else {
        Iterator iFields = lFields.iterator();
        while (iFields.hasNext()) {
          Field field = (Field)iFields.next();
          String fieldRef = field.getFieldRef();
          if (fieldRef != null && fieldRef.length() > 0) {
            Field referencedField = (Field)fieldDefinitionMap.get(fieldRef);
            String referencedDepends = referencedField.getDepends();
            if (referencedDepends.length() > 0) {
              String depends = field.getDepends();
              if (depends != null && depends.length() > 0) {
                field.setDepends(depends + "," + referencedDepends);
              } else {
                field.setDepends(referencedDepends);
              }
            }
            field.addArg0(referencedField.getArg0());
            Map referencedFieldVars = referencedField.getVars();
            Iterator iReferencedFieldVars = referencedFieldVars.keySet().iterator();
            while (iReferencedFieldVars.hasNext()) {
              String varName = (String)iReferencedFieldVars.next();
              field.addVar(referencedField.getVar(varName));
            }
            //System.out.println(field);
          }
        }
      }

    }
</pre>
</ol>
