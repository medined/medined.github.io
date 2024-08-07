---
layout: post
title: Spring; BeanStoreDefinitionException; Fix for 'Unexpected failure during bean definition parsing'
date: '2006-06-28T10:11:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-06-28T10:21:36.103-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-115150413513746190
blogger_orig_url: http://affy.blogspot.com/2006/06/spring-beanstoredefinitionexception.md
year: 2006
theme: java
---

<p>While working to get one of my projects working under Ubunu Linux which was working under Windows, I ran into the
    following error:</p>
<pre>org.springframework.beans.factory.BeanDefinitionStoreException: Error '<property> element


for property 'location' is only allowed to contain either 'ref' attribute OR 'value'
attribute OR sub-element' in resource'class path resource [applicationContext.xml]'
at: Bean 'propertyConfigurer'</pre>
<p>The error was definitely wrong since the XML file hadn't changed between the two OSes.</p>
<p>The problem was that I was using GNU's Java v1.4 instead of Sun's Java v1.5. I don't know if it was GNU vs Sun or
    v1.4 vs v1.5 that was the real problem. But switching to Sun Java v1.5 made Spring work which is good enough for me.
</p>
<p>Oh... one quick note. Don't simply copy your Windows JDK directory to Linux and expect it to work. Download the Linux
    version from Javasoft. I installed mine to /usr/local.
<p>