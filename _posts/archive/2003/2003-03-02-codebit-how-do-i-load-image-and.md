---
layout: post
title: How Do I Load An Image and Determine Height and Width Using Java?
date: '2003-03-02T11:26:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-02T11:26:14.053-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90395803
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-do-i-load-image-and.md
year: 2003
theme: java
---

When loading an image over the net you frequently have to allow for long load times. The MediaTracker class is used to
monitor a resource so that other things can happen (ie. threads) while the image is loading.

However, sometimes you really need for the image to totally load because you need the height and width measurements
before continuing. The following code fragment show how to wait for an image to finish loading. - from Bob Withers in
comp.lang.java.api.

<PRE>
// url is a String containing the URL of the image to load.
MediaTracker tr;
Image        im = toolkit.getImage(url);

tr.addImage(im, 0);
tr.waitForID(0);

// at this point the image is finished
// loading and the height and width can
// be determined.
int w = im.getWidth(this);
int h = im.getHeight(this);
</PRE>