---
layout: post
title: Using Hashes in Bash Scripts
date: '2003-03-01T10:00:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-01T10:02:07.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90392785
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-using-hashes-in-bash-scripts.md
year: 2003
theme: perl
---

Using Hashes in Bash Scripts


<p>This codebit was donated by Phil Howard on 1998Dec03. Hash data structures are very convenient. They let you use strings as indexes instead of numbers. The <code>value</code> script, shown below, demonstrates how to use Bash's eval command to simulate a hash structure. The command, <code>value david</code> prints <code>"The email address for david is medined@mtolive.com"</code>.</p>
<pre>
#!/bin/bash
# value.sh

email_phil="phil@rigel.ipal.net"
email_david="medined@mtolive.com"

eval 'email=${email_'"${1}"'}'
echo "The email address for ${1} is ${email}"
</pre>
