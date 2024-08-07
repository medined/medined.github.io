---
layout: post
title: Ubuntu Disk Partition Sizes
date: '2006-06-28T12:38:00.000-04:00'
author: David Medinets
categories:
- "[[ubuntu]]"
modified_time: '2006-06-28T13:19:34.453-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-115151293907641808
blogger_orig_url: http://affy.blogspot.com/2006/06/ubuntu-disk-partition-sizes.md
year: 2006
---

Various documentation that I read said that the root of Ubuntu needed less than 200Mb. However, I've found that
<b>not</b> to be true.


In my situation, I ran out of disk space. So I've bumped the root partition to 500Mb. On my 80Gb drive, Here are my
partition sizes:
<pre>
medined@thog:~$ <b>df</b>
Filesystem           1K-blocks      Used Available Use% Mounted on
/dev/hda1               481764    282766    173296  63% /
/dev/hda8             51675672    923812  48126844   2% /data
/dev/hda5              3099260   1593760   1348068  55% /usr
/dev/hda7             10317828    556548   9237164   6% /usr/local
/dev/hda6             10317828    337728   9455984   4% /var
</pre>
<p>I also have a 1,000Gb swap - mainly because when I tried to install Oracle it asked for a 750Mb swap file.
<p>
<p><i><b>UPDATE:</b> One mistake (there are probably others) that I made was that the /tmp path needs a lot of room
        because that is where installation place files. So two choices - either give the root partition another couple
        of gigabytes just for software installation. Or give /tmp its own 2Gb partition. Or, as I am doing, constantly
        tell each installation process to place temp files in a /data/tmp (or whatever) directory.</i></p>