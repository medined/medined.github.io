---
layout: post
title: Segmentation Fault Caused by /dev/zero Not Being Writable.
date: '2006-06-01T13:36:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-06-01T13:40:03.586-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-114918360256190210
blogger_orig_url: http://affy.blogspot.com/2006/06/segmentation-fault-caused-by-devzero.md
year: 2006
theme: java
---

On Solaris, I am running a Java application inside a chroot using sudo to change the userid to be non-root. However, I
repeatedly ran into segementation faults. The solultion to the problem was making the /dev/zero file writable by
everyone (user, group, and world).