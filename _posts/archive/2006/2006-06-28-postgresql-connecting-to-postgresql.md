---
layout: post
title: Postgresql; Connecting to PostgreSQL from a remote client.
date: '2006-06-28T13:00:00.000-04:00'
author: David Medinets
categories:
- "[[pstgresql]]"
modified_time: '2006-07-17T09:17:18.180-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-115151401237121136
blogger_orig_url: http://affy.blogspot.com/2006/06/postgresql-connecting-to-postgresql.md
year: 2006
---

I see many web pages that mention the pg_hba.conf file which control which hosts and user can connect to which database.


However, there is a <b>listen_addresses</b> parameter in <b>postgresql.conf</b> which needs to be set. By default, it
was commented out so that no remote client connections could be made - certainly a reasonable security precaution.
<p>In order to accept connections from remote client you need to set the <b>listen_addresses</b> parameter to something
    other than localhost. For example, if your PostgreSQL server sits on 192.168.1.123 then that could be the value for
    <b>listen_addresses</b>. If your PostgreSQL server has multiple addresses, you could use * so that remote clients
    can connect to any of the IP addresses. Or use a comma-delimited list if only some of the IP address should be used
    when connecting to PostgreSQL.</p>