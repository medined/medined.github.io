---
layout: post
title: 'Ubuntu, Oracle Installation: No response from /etc/init.d/oracle-xe configure'
date: '2006-06-25T10:46:00.000-04:00'
author: David Medinets
categories:
- "[[ubuntu]]"
modified_time: '2008-01-08T10:39:32.479-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-115124739843271386
blogger_orig_url: http://affy.blogspot.com/2006/06/ubuntu-oracle-installation-no-response_25.md
year: 2006
---

<p>I've been working with virtual machines. Which turned out to be a good idea while trying to install Oracle on Ubuntu
    Linux. I ran into a few problems.</p>


<ul>
    <li>install the <code>libaio1</code> package.</li>
</ul>

<b>2008-Jan-07 Update: A reader mentioned that the package name ends in a one not an el.</b>

<p>Oracle needs this package installed before you install the <code>.deb</code> package. After the package is installed
    with the <code>dpkg -i</code> command, you are supposed to run the <code>/etc/init.d/oracle-xe configure</code>
    command. However this command produced no response. Nor did trying to stop and start Oracle. After a bit of playing,
    I did the followig:</p>

<pre>
ORACLE_HOME=/usr/lib/oracle/xe/app/oracle/product/10.2.0/server
export ORACLE_HOME
$ORACLE_HOME/config/script/XE.sh
</pre>

I still can't get Oracle to work but at least that XE script did something.