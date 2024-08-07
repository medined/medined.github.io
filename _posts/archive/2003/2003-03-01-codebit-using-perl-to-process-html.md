---
layout: post
title: Using Perl to Process HTML Form Information
date: '2003-03-01T10:20:00.000-05:00'
author: David Medinets
categories:
- "[[perl]]"
modified_time: '2003-03-01T10:21:16.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90392829
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-using-perl-to-process-html.md
year: 2003
theme: perl
---

Using Perl to Process HTML Form Information


<p>I've found using a hash variable to hold form information to be the easiest way to work. The function shown below is called <code>getFormData</code>. It reads each form field into a hash, with either 'scalar_' or 'array_'  prepended to the field names. This technique works well when you have form field with the same name, like checkboxes.</p>
<p>In order to effectively use this code, you need to be familiar with references. If you need a refresher on this topic, please see Chapter 8 of my book at  <a href="http://www.CodeBits.com/p5be/ch08.cfm">http://www.CodeBits.com/p5be/ch08.cfm</a>.</p>
<p>Here is the code that I place towards the top of my CGI scripts:</p>
<pre>
    getFormData(\%FORM);
</pre>
<p>The <code>printFORM</code> function is used for debugging. It prints all of the values in the <code>FORM</code> hash. Notice that it displays both scalar and array versions of the form data.<p>
<pre>
sub printFORM {
  print "Form Variables\n";
  print "--------------\n";

  foreach $key (sort(keys(%FORM))) {
    print "$key = @{$FORM{$key}}\n"
      if ref($FORM{$key}) eq "ARRAY";
    print "$key = $FORM{$key}\n"
      if ref($FORM{$key}) ne "ARRAY";
  }
}
</pre>
<p>And then towards the end of the script, I place the following routine:</p>
<pre>
sub getFormData {
  my($hashRef) = shift;
  my($buffer) = "";

  if ($ENV{'REQUEST_METHOD'} eq 'GET') {
    $buffer = $ENV{'QUERY_METHOD'};
  }
  else {
    read(STDIN, $buffer, $ENV{'CONTENT_LENGTH'});
  }

  foreach (split(/&/, $buffer)) {
    my($key, $value) = split(/=/, $_);

    $key = decodeURL($key);
    $value = decodeURL($value);

    $hashRef->{"scalar_$key"} = $value;

    if (! defined($hashRef->{"array_$key"})) {
      $hashRef->{"array_$key"} = [ $value ];
    }
    else {
      push @{$hashRef->{"array_$key"}}, $value;
    }
  }
}
</pre>
