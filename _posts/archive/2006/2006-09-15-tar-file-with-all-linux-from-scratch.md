---
layout: post
title: Tar File With All Linux From Scratch Source Packages
date: '2006-09-15T16:33:00.000-04:00'
author: David Medinets
categories:
- "[[linux]]"
modified_time: '2006-09-15T23:15:47.150-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-115835267119744664
blogger_orig_url: http://affy.blogspot.com/2006/09/tar-file-with-all-linux-from-scratch.md
year: 2006
---

<a href="http://www.linuxfromscratch.org/lfs/view/stable/chapter03/packages.html">Section 3.2</a> of the <a
    href="http://www.linuxfromscratch.org">Linux From Scratch</a> bookmentions 57(!) packages and 39 patches that need
to be downloaded in order to create your own Linux. I've downloaded all of the packages and patches and combined them
into two TAR files to save *you* time. The files are located at:


<a href="http://www.dbbits.com/LinuxFromScratchPackages.tar">http://www.dbbits.com/LinuxFromScratchPackages.tar</a>
<a href="http://www.dbbits.com/LinuxFromScratchPatches.tar">http://www.dbbits.com/LinuxFromScratchPatches.tar</a>

You can grab these files with these commands:
<pre>wget http://www.dbbits.com/LinuxFromScratchPackages.tar
wget http://www.dbbits.com/LinuxFromScratchPatches.tar
</pre>And you can extract the files with this command:
<pre>tar -xvf LinuxFromScratchPackages.tar
tar -xvf LinuxFromScratchPatches.tar
</pre>Once the files are extracted, you will have a set of *.tar.gz and *.tar.bz2 files. The various package files can
be uncompressed with these commands:
<pre>tar -zxvf packages/vim-7.0-lang.tar.gz
tar --bzip2 -xvf packages/binutils-2.16.1.tar.bz2</pre>