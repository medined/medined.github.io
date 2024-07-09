---
layout: post
title: Documentation Error; Sonic XIS XML Database; Setting Address Size and Cache Size should be KB not MB.
date: '2004-05-28T14:57:00.000-04:00'
author: David Medinets
categories:
- "[[xml]]"
modified_time: '2004-05-28T15:03:15.363-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108577095540233906
blogger_orig_url: http://affy.blogspot.com/2004/05/documentation-error-sonic-xis-xml.md
year: 2004
---

Documentation Error; Sonic XIS XML Database; Setting Address Size and Cache

* public void setAddressSpaceSize(long size) - The documentation says that size is "the size in megabytes". However, it really needs to be the size in kilobytes.
* public void setSize(long size) - The documentation says that size is "the size in megabytes". However, it really needs to be the size in kilobytes.

Most of the time, you won't be dynamically changing the sizes so it's probably better to use the `com.exln.dxe.defaultcachesize` and `com.exln.dxe.defaultassize` parameters in the `xlnserver.properties` file. Note that these values should be in bytes!
