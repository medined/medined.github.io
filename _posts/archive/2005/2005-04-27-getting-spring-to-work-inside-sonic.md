---
layout: post
title: Getting Spring to Work Inside Sonic XIS XML Server
date: '2005-04-27T17:18:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-05-27T03:14:40.956-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-111463711466640631
blogger_orig_url: http://affy.blogspot.com/2005/04/getting-spring-to-work-inside-sonic.md
year: 2005
theme: java
---

I encountered an exception trying to use Spring inside the Sonic XIS XML server:


<pre>
java.lang.ExceptionInInitializerError:
java.lang.NullPointerException
   at org.springframework.core.io.ClassPathResource.getURL(ClassPathResource.java:144)
   at org.springframework.core.io.ClassPathResource.getFile(ClassPathResource.java:154)
   ...
</pre>

This issue happened because XIS has no context class load for the current thread. In order to workaround this problem, I
updated the ClassPathResource class in the spring-core.jar file.

I created the following method:

<pre>
   private ClassLoader getClassLoader() {
       ClassLoader rv = this.classLoader;
       if (rv == null) {
           // no class loader specified -> use thread context class loader
           rv = Thread.currentThread().getContextClassLoader();
       }
       if (rv == null) {
           rv = getClass().getClassLoader();
       }
       return rv;
   }
</pre>
And updated the following two methods:
<pre>
   public InputStream getInputStream() throws IOException {
       InputStream is = null;
       if (this.clazz != null) {
           is = this.clazz.getResourceAsStream(this.path);
       }
       else {
           is = getClassLoader().getResourceAsStream(this.path);
       }
       if (is == null) {
           throw new FileNotFoundException(
                   getDescription() + " cannot be opened because it does not exist");
       }
       return is;
   }

   public URL getURL() throws IOException {
       URL url = null;
       if (this.clazz != null) {
           url = this.clazz.getResource(this.path);
       }
       else {
           url = getClassLoader().getResource(this.path);
       }
       if (url == null) {
           throw new FileNotFoundException(
                   getDescription() + " cannot be resolved to URL because it does not exist");
       }
       return url;
   }
</pre>
After these changes, it was a simple matter to create my custom spring-core-wwre.jar file.