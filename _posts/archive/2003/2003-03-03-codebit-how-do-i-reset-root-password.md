---
layout: post
title: How do I Reset the Root password of MySQL Using Linux?
date: '2003-03-03T17:56:00.000-05:00'
author: David Medinets
categories:
- "[[mysql]]"
modified_time: '2003-10-03T17:38:08.303-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90401939
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-how-do-i-reset-root-password.md
year: 2003
---

How do I Reset the Root password of MySQL Using Linux?


<ol>
    <li>/usr/local/mysql/bin/safe_mysqld --skip-grant-tables &</li>
    <li>/usr/local/mysql</li>
    <li>use mysql;</li>
    <li>update user set password = password('.......') where user = 'root' and host='localhost';</li>
    <li>Stop and Start the MySQL server. [2003Oct03 Note] A reader mentioned that it is easier to 'flush privileges'
        instead of restarting the server.</li>
</ol>