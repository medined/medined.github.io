---
layout: post
title: Compiling the Jakarta Validator in Eclipse
date: '2002-11-30T09:41:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2007-11-21T12:09:36.571-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-90033271
blogger_orig_url: http://affy.blogspot.com/2002/11/compilig-jakarta-validator-in-eclipse.md
year: 2002
theme: java
---

Nearly a year ago, one of my Blog entries mentioned that I wrote a Java-based Validation service. Well, a few days ago I
ran across the Jakarta Commons Validator project.


It seems to be everything that I had already done but using more portable techniques. For example, I used an Oracle
database instead of XML files. So, I immediately downloaded the project and tried to compile it using Eclipse. I ran
into a few issues, but was successful. All of the tests ran perfectly. This Blog entry highlights the changes that I
made in order to use Eclipse.

<p>First off, let me congratulate and thank the folks who wrote the Validator. My comments below are not negative, they
    just reflect what changes I made to get the code compiled and executing.

<ol>
    <li> The main issue that I ran into was that the directory organization wasn't consistent from an Eclipse point of
        view. Eclipse is project-oriented and wants all classes to reside in their proper directories. For example,
        files in the test.org.foo.com directory must have a package statement that uses test.org.foo.com instead of
        org.foo.com. When working with command-line tools, having different package names from the directories in which
        they reside is fine. However, Eclipse is less flexible. This issue affected files in the example and test
        directory.
        <ol>
            <li>Because I changed the package name, I also needed to update the validation xml files.</li>
            <li>Because I changed the package name, I needed to add import statements.</li>
        </ol>
        </il>
    <li>I created a ValidationLogFactory in order to hide how the Log object was created. The original code used a
        deprecated class. If needed, the deprecated instatiation can be placed back into ValidationLogFactory. This
        class was created so that only one file needs to be changed to flip between the two methods of Log invokation.
    </li>

    <li>Eclipse needed the following Classpath variables to be defined. Of course, the paths to the Jar files would
        change depending on how you installed the various packages:
        <table>
            <tr>
                <td>APACHE_ORO </td>
                <td>G:\java\velocity-1.3.1-rc2\build\lib\oro.jar</td>
            <tr>
            <tr>
                <td>COMMONS_BEANUTILS </td>
                <td>G:\java\jwsdp-1_0_01\server\lib\commons-beanutils.jar</td>
            <tr>
            <tr>
                <td>COMMONS_COLLECTIONS </td>
                <td>G:/java/jwsdp-1_0_01/common/lib/commons-collections.jar</td>
            <tr>
            <tr>
                <td>COMMONS_DIGESTER </td>
                <td>G:\java\jwsdp-1_0_01\server\lib\commons-digester.jar</td>
            <tr>
            <tr>
                <td>COMMONS_LOGGING</td>
                <td>G:/java/xml-axis-10/lib/commons-logging.jar</td>
            <tr>
            <tr>
                <td>JUNIT </td>
                <td>G:/java/velocity-1.3.1-rc2/build/lib/junit-3.7.jar</td>
            <tr>
        </table>
    </li>
    <li>I removed the DERIVED_VALUES section since the values are set in the build.properties file. It would be nice if
        Ant generated an error file if the physical Jar files couldn't be found, but I don't know how to do this.</li>

    <li>Changed source.home to src\org instead of src\shared. I'm not sure where the 'shared' came from. I don't recall
        seeing such a directory in the original distribution.</li>

    <li>Changed the compile.tests and compile.example tasks so that the xml files are copied to the target\classes
        directory tree. This simplies the classpath needed for runtime, I think.</li>
</ol>