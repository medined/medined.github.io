---
layout: post
title: Using Composite Object For Hash Key Better Than Concatenated String
date: '2006-10-08T20:23:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-10-08T20:24:45.763-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116035348540547451
blogger_orig_url: http://affy.blogspot.com/2006/10/using-composite-object-for-hash-key.md
year: 2006
theme: java
---

Kirk Pepperdine has written a guest article for the Java Specialists' newsletter about the DRY or Don't Repeat Yourself
principle.


While I agree with DRY, the important part of the article was his performance timing study. He compared using

personsByName.put(firstName + lastName, person);

versus

personsByName.put(new CompositeKey(firstName, lastName), person);

Cutting to the result of the test, the composite key cut the example's execution by 66% and reduced memory consumption
by about 65Mb. Visit the link above for more details or simply use Composite keys from now on!