---
layout: post
title: Using a CustomEditorConfigurer to Specify a Lucene FSDirectory Using Spring
date: '2005-09-07T08:21:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-06-25T21:35:36.326-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-112609696770860017
blogger_orig_url: http://affy.blogspot.com/2005/09/using-customeditorconfigurer-to.md
year: 2005
theme: java
---

<p>When I started to use Spring with my Lucene application, I searched for an easy way to specify the directory name
  where the Lucene repository is stored. Since the FSDirectory class is not instantiated directly (you need to call
  FSDirectory.getDirectory), I couldn't see how to configure Spring to create the object for me.</p>


<p>Then I discovered that the JavaBeans api has the ability to automatically convert from a String to any other class
  using a configurator object. With this ability, specifying a bean that uses an FSDirectory can be done in the
  following way:
<pre>&lt;bean id="serverDatabase" class="com.server.DB" method="setup"&gt;
  &lt;property name='directory'&gt;&lt;value&gt;data/LuceneRepsitory&lt;/value&gt;&lt;/property&gt;
&lt;/bean&gt;</pre>
</p>
<p>The idea is that when Spring wired beans together some configurer object would convert that String into a Lucune
  FSDirectory object. This conversion is done by the following Java class:
<pre>public class FileSystemLuceneDirectoryEditor extends PropertyEditorSupport {
    public void setAsText(final String textValue) {
        try {
            final boolean fileExists = new File(textValue).exists();
            final boolean createDirectory = fileExists == true ? false : true;
            setValue(FSDirectory.getDirectory(textValue, createDirectory));
        } catch (IOException e) {
            throw new CriteriaWatchException(e);
        }
    }
}</pre>
</p>
<p>The configurer class needs to be introduced to Spring through its XML configuration file:
<pre>  &lt;bean id='customEditorConfigurer' class='org.springframework.beans.factory.config.CustomEditorConfigurer'&gt;
    &lt;property name='customEditors'&gt;
      &lt;map&gt;
        &lt;entry key='org.apache.lucene.store.Directory'&gt;
          &lt;bean id='luceneDirectoryEditor' class='FileSystemLuceneDirectoryEditor'/&gt;
        &lt;/entry&gt;
      &lt;/map&gt;
    &lt;/property&gt;
  &lt;/bean&gt;</pre>
</p>
<p>After the above work is done, Spring automatically converts any String values that are destined to be Directory
  parameters.</p>