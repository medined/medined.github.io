---
layout: post
title: Catching Java Errors in the main() Method.
date: '2004-04-21T11:19:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-21T11:23:49.403-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108256078327876080
blogger_orig_url: http://affy.blogspot.com/2004/04/catching-java-errors-in-main-method.md
year: 2004
theme: java
---

I've been using Java for more years than I care to mention but I never realized that you could <code>catch</code> errors
as well as <code>exceptions</code>.i


So I've just added one more item to my toolbox. My <code>main()</code> method always uses this template:

<pre>
try {
  // do something.
} catch (Error e) {
  e.printStackTrace();
} catch (Exception e) {
  e.printStrackTrace();
} finally {
  System.out.println("Done.");
}
</pre>
<p>Of course, in my real code I use <code>log4j</code> so that the errors are persisted.</p>