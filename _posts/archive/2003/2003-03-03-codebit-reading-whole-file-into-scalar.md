---
layout: post
title: Reading a whole file into a scalar Using Perl
date: '2003-03-03T20:24:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-03T20:24:22.556-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90402439
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-reading-whole-file-into-scalar.md
year: 2003
theme: perl
---

Reading a whole file into a scalar Using Perl


<P>Quite often, it is very useful to read the contents of a file into a scalar variable. The following example shows how
  this can be done. <code>$fileName</code> is the name of the input file. <code>$buffer</code> is a scalar where the
  file contents is stored. The <code>$/</code> variable contains the end-of-record delimitor. By undefining the
  variable, Perl will be forced to read the entire file in one shot.</P>
<P><BIG>Usage:</BIG></P>
<PRE>
$buffer = readFileContents($fileName)
</PRE>
<P><BIG>Code:</BIG></P>
<PRE>
sub readFileContents {
  my($fileName) = shift;
  my($buffer);
  local($/) = undef;

  open(FILE, $fileName)
    or die "couldn't open $filename for reading: $!\n";

  $buffer = &lt;FILE&gt;;
  close(FILE);
  return($buffer);
}
</PRE>