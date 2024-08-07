---
layout: post
title:
date: '2001-11-19T10:58:00.000-05:00'
author: David Medinets
modified_time: '2001-11-19T10:58:42.886-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7239057
blogger_orig_url: http://affy.blogspot.com/2001/11/unspsc-codes-are-four-sets-of-two.md
year: 2001
categories:
- "[[java]]"
---

<a href="http://www.unspsc.org/">UNSPSC</a> codes are four sets of two characters.


Each pair of character helps to define a category in a hierarchical manner. So the left pairs (like 10 00 00 00) define
a very broad category while the right pairs (like 11 23 44 02) defines a very narrow category. Giving any category code,
you can find its parent category code via the following java code:
<pre>
// Move the unspsc code into a stringbuffer so it can be manipulated
StringBuffer uc = new StringBuffer(unspsc_code);

// replace digits 7 & 8 with zeros.
// if code has changed then that's the parent category
// The setCharAt function uses a zero-based index.So the
// range is 0 to 7 instead of 1 to 8.
uc.setCharAt(7, '0');
uc.setCharAt(6, '0');

if (! unspsc_code.equals(uc.toString())) {
  System.out.println(uc + " is the parent category");
}
else {
  // replace digits 5 & 6 with zeros.
  uc.setCharAt(5, '0');
  uc.setCharAt(4, '0');
  if (! unspsc_code.equals(uc.toString())) {
    System.out.println(uc + " is the parent category");
  }
  else {
    // replace digits 3 & 4 with zeros.
    uc.setCharAt(3, '0');
    uc.setCharAt(2, '0');
    if (! unspsc_code.equals(uc.toString())) {
      System.out.println(uc + " is the parent category");
    }
    else {
      System.out.println(uc + " is a top-level category.");
    }
  }
}
</pre>