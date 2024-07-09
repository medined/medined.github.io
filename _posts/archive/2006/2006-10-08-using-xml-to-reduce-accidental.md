---
layout: post
title: Using XML to Reduce Accidental Corruption
date: '2006-10-08T01:25:00.000-04:00'
author: David Medinets
categories:
- "[[xml]]"
modified_time: '2006-10-08T01:36:07.786-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-116028576679361586
blogger_orig_url: http://affy.blogspot.com/2006/10/using-xml-to-reduce-accidental.md
year: 2006
---

Elliote Harold wrote an article about Fuzz testing which was published on IBM's developerWorks web site. In it he gives
some great reasons for choosing to use an XML format for your data files instead of using a text-based format (like CVS)
or a binary format (like serialized objects).


The important point was that XML parser are designed to separate good input from bad. Why redo all that error-checking
just to create your own format?

In Elliote's own words:
<blockquote>
    The key characteristic that makes XML formats resistant to fuzz is that an XML parser assumes nothing about the
    input. This is precisely what you want in a robust file format. XML parsers are designed so that any input
    (well-formed or not, valid or not) is handled in a defined way. An XML parser can process any byte stream. If your
    data first passes through an XML parser, all you need to be ready for is whatever the parser can give you. For
    instance, you don't need to check whether the data contains null characters because an XML parser will never pass
    you a null. If the XML parser sees a null character in its input, it throws an exception and stops processing. You
    still need to handle this exception of course, but writing a catch block to handle a detected error is much simpler
    than writing code to detect all possible errors.

    For further security you can validate your document with a DTD and/or a schema. This checks not only that the XML is
    well-formed, but that it's at least close to what you expect. Validation will rarely tell you everything you need to
    know about a document, but it makes it easy to write a lot of simple checks. With XML, it is very straightforward to
    strictly limit the documents you accept to the formats you know how to handle.
</blockquote>