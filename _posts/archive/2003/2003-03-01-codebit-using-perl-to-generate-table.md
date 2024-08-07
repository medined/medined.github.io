---
layout: post
title: Using Perl to Generate a Table of Contents for HTML Pages
date: '2003-03-01T22:11:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-01T22:11:16.183-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90394568
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-using-perl-to-generate-table.md
year: 2003
theme: perl
---

Using Perl to Generate a Table of Contents for HTML Pages


<P>This script is designed to create a Table of Contents page for HTML documents. It reads any files listed on the command line (wildcards are OK) and searches for the HTML <code>&lt;Hn&gt;</code>.</p>
<P>To index an entire directory use: <TT>perl toc.pl *.html</TT></p>
<P>NOTE: The files will be read in alphabetical order which may not be the order that you need. Simply cut and paste the resulting HTML until the order is correct.</p>
<pre>
#!/usr/bin/perl -w
#
# To index an entire directory use:
#     perl toc.pl *.html
#
use strict;

# holds the name of each file
# as it is being processed.
my($file);

# holds the text of the heading
# (from the anchor tag).
my($heading);

# holds the last heading level
# for comparision.
my($oldLevel);

# holds each line of the file
# as it is being processed.
my($line);

# used as temporary variables
# to shorten script line widths
my($match);
my($href);

# holds the name of the heading
# from the anchor tag.
my($name);

# holds the level of the current heading.
my($newLevel);

# First, I open an output file and print the
# beginning of the HTML that is needed.
#
$outputFile = "fulltoc.htm";
open(OUT, "&gt;$outputFile");
print OUT ("&lt;HTML&gt;&lt;HEAD&gt;&lt;TITLE&gt;");
print OUT ("Detailed Table of Contents\n");
print OUT ("&lt;/TITLE&gt;&lt;/HEAD&gt;&lt;BODY&gt;\n");

# Now, loop through every file in the command
# line looking for Headers. When found, Look
# for an Anchor tag so that the NAME attribute can
# be used. The NAME attribute might be different
# from the actual heading.
#
foreach $file (sort(@ARGV)) {
    next if $file =~ m/^\.htm$/i;
    print("$file\n");
    open(INP, "$file");
    print OUT ("&lt;UL&gt;\n");
    $oldLevel = 1;
    while (&lt;INP&gt;) {
        if (m!(&lt;H\d&gt;.+?&lt;/H\d&gt;)!i) {
            # remove anchors from header.
            $line = $1;
            $match = '&lt;A NAME="(.+?)"&gt;(.+?)&lt;/A&gt;';
            if ($line =~ m!$match!i) {
                $name = $1;
                $heading = $2;
            }
            else {
                $match = '&lt;H\d&gt;(.+?)&lt;/H\d&gt;';
                $line =~ m!$match!i;
                $name = $1;
                $heading = $1;
            }
            m!&lt;H(\d)&gt;!;
            $newLevel = $1;
            if ($oldLevel &gt; $newLevel) {
                print OUT ("&lt;/UL&gt;\n");
            }
            if ($oldLevel &lt; $newLevel) {
                print OUT ("&lt;UL&gt;\n");
            }
            $oldLevel = $newLevel;
            my($href) = "\"$file#$name\"";
            print OUT ("&lt;LI&gt;");
            print OUT ("&lt;A HREF=$href&gt;");
            print OUT ("$heading&lt;/A&gt;\n");
        }
    }
    while ($oldLevel--) {
        print OUT ("&lt;/UL&gt;\n");
    }
    close(INP);
}

# End the HTML document and close the output file.
#
print OUT ("&lt;/BODY&gt;&lt;/HTML&gt;");
close(OUT);
</pre>
