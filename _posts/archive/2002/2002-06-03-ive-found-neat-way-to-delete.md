---
layout: post
title: How to Delete Duplicate Records Using Oracle's Rank Function
date: '2002-06-03T10:45:00.000-04:00'
author: David Medinets
categories:
- "[[oracle]]"
modified_time: '2007-11-21T12:09:20.020-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-77288050
blogger_orig_url: http://affy.blogspot.com/2002/06/ive-found-neat-way-to-delete.md
year: 2002
---

I've found a neat way to delete duplicates from my database tables using Oracle's RANK() function.


<pre>
DELETE FROM __table__
WHERE ROWID IN (
  SELECT MyKey
  FROM (
    -- use the RANK() function to assign a sequential number to each set of
records.
    SELECT MyKey, display_name, RANK() OVER (PARTITION BY display_name ORDER BY MyKey) AS SeqNumber
    FROM (
      -- use the ROWID psuedo-column to get a unique id for each record.
      SELECT ROWID AS MyKey, display_name
      FROM __table__
      WHERE (display_name) IN (
        -- select the set of records that are duplicates.
        SELECT display_name FROM __table__ GROUP BY display_name HAVING COUNT(*) > 1
      )
    )
  )
  WHERE SeqNumber > 1
)
</pre>