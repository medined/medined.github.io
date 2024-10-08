---
layout: post
title: Why Use UUID Values?
date: '2003-03-04T13:41:00.000-05:00'
author: David Medinets
categories:
- "[[uuid]]"
modified_time: '2003-03-04T13:41:52.190-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90405845
blogger_orig_url: http://affy.blogspot.com/2003/03/why-use-uuid-values.md
year: 2003
---

<p>In this note, I attempt to articulate why I feel strongly that using UUID values is better than Sequences (or
    Autonumbering) for Primary Keys.</p>


<p><b>Debugging Production Issues</b> - When subtle production issues arise, it is often good to be able to copy
    production data into a development database. When using Sequences, the process of copying production data normally
    involves a DBA because Primary Keys in the production system may already exist in development leads to various
    insert and update errors. When UUID values are used, there is no possibility of duplicate values. Therefore, moving
    production data back to staging and/or the development environments is much easier.</p>

<p><b>Copying Dynamic Lists Between Environments</b> - Imagine information like a user list that starts in the
    development environment during the creation of a customer service application. Then the application is moved to a
    production customer service server. Imagine further that that data is also used as part of a company directory. Then
    in true-to-life fashion, a subtle bug is hit that moves the Trenton, NJ employees into the Boston, MA business unit.
    A bug might not cause this problem. For example, a company could be sold or a layoff could happen. If only one or
    two records are affected, they can be fixed using the Customer Service application. But what if 3,000 records were
    affected and a simple SQL statement isn�t enough to correct the bad records? In this situation, the data needs to be
    copied back to the development environment, a program must be written to fix the data, and then the data must be
    copied back to staging for testing, and then into production. Moving data is hard enough when simple tables are
    used. When parent-child relationships are involved the data movements routines to ensure that no Sequence classes
    occur are more complex. If Sequences are used each time that the data is moved, the DBA needs to write SQL to modify
    the Sequences for each affected table. A potentially arduous task. If UUID values are used, the problem can be
    resolved simply. Simply write an SQL SELECT�INTO statement to move the data between the two databases.</p>

<p><b>Synchronizing Development With Production</b> - Much of the time, I like the data in my development environment to
    mirror that in production. And yet, I still want the freedom to create test data. When using Sequence numbers this
    scenario causes problems because the Sequence numbers between the two databases will clash.
    If needed a Boolean field called <code>bFromProduction</code> can be maintained so that production data can be
    separated from development data. Another method, perhaps preferred for its simplicity, could be to maintain a
    productionUUID table that is populated before the production data is moved. With UUID values, only the UUID need be
    stored, with Sequence numbers, such a table would also need to store the table name to avoid value clashes.</p>

<p><b>Easy Elimination of Redundant Form Submits</b> - One issue when working with stateless environments revolves
    around users submitting their forms twice. This issue is critical when inserting and updating information. If not
    properly handled, duplicate records can be created in the database. Duplicate inserts can be eliminated creating a
    UUID value beforehand and making it part of the form information. The database (or application) can be check for the
    UUID value before inserting any record. Duplicate updates can be avoided by using a UUID value as an audit marker.
    Each change to the database can be logged to an audit table. When an update form is submitted, the UUID value can be
    checked against the audit table.</p>

<p><b>Permissibility Frameworks</b> - UUID values also have advantages when used in permissibility frameworks. This
    advantage arises because one table can be used to store permission information for all objects in the system.
    Actually, the advantage works two ways. One, any object can be protected. And two, any object can be the accessor.
    So, ensuring that a printer can only be accessed by a specific application is easy to accomplish with the same
    framework that protects a file from being accessed by a user.</p>