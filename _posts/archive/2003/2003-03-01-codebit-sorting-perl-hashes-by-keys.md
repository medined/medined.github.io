---
layout: post
title: Sorting Perl Hashes by Keys and by Values
date: '2003-03-01T10:10:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-01T10:15:07.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90392805
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-sorting-perl-hashes-by-keys.md
year: 2003
theme: perl
---

Sorting Perl Hashes by Keys and by Values


<p>Code</p>
<pre>
#/usr/bin/perl -w
use strict;
my(%hash);
# Create a hash with four elements.
$hash{'01'} = 'D';
$hash{'02'} = 'C';
$hash{'03'} = 'B';
$hash{'04'} = 'A';
# Sort the hash according to its keys.
my(@sortedByKey) = sort(keys(%hash));
# Print out the hash entries in sorted order.
print "Sorted by Key:\n";
foreach (@sortedByKey) {
  print "\t$_: $hash{$_}\n";
}
# Sort the hash according to its keys. The
# $a and $b scalars are part of the main
# package namespace. Therefore, the main
# package name needs to be specified when
# using the strict pragma.
my(@sortedByValue) = sort { $hash{$main::a} cmp $hash{$main::b} } keys %hash;
# Print out the hash entries in sorted order.
print "Sorted by Value:\n";
foreach (@sortedByValue) {
  print "\t$_: $hash{$_}\n";
}
</pre>
<p>output</p>
<pre>
Sorted by Keys:
        01: D
        02: C
        03: B
        04: A
Sorted by Values:
        04: A
        03: B
        02: C
        01: D
</pre>
