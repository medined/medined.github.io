---
layout: post
title: From the past, Version Control and XML Documents
author: David Medinets
categories: xml
year: 2017
---

Back in 2004, I was working on a project that stored information in XML. I wrote the following notes about how to arrange the XML to make version control easier. The requirement was to store an unregulated set of attributes, any of which could be associated with a project. Another need was to quickly see changes from one version of the data to another.

Version Control is an inherently complex topic. This short note only touchs on some best practices, ideas for storage, and examples using XML. While focusing on using XML as the storage medium for version-controlled objects there are several other mediums that can be used. For example, text files or tables in a relational database.

XML is a strong contender for use as the predominent storage mechanism when dynamic data is needed by an application. For example, descriptions of commercial products typically deal with hundreds of attributes, any of which or none could be associated with any given commercial product.

The project needs to handle objects with any number and any types of constraints. Additionally, allowance needs to be made for unknown constraints that may be needed in the future.

# Best Practices

## Element and Attribute Order are Important

When saving XML files it is important to maintain the same element and attribute order so that version comparision is relatively straightforward. This idea implies that all XML should be schema-based which would naturally result in element retaining a specific order in order to pass validation.

## One Element or Attribute Per Line

If you place each XML element and attribute on a separate line, it makes it easier to detect changes via automated Diff programs.

## Avoid Magic Values

Instead of this:

```
  modifiers="97"
```

Do the following:

```
  <modifiers>
    <public />
    <final />
    <synchronized />
  </modifiers>
```

If you explicitly state the modifiers the XML is more robust (ie, about to withstand system changes) and changes are easier to detect using automated Diff programs.

## Sort Repeated Elements

When using repeated elements like the WIDGET tag in the following:

```
  <widgets>
    <widget name="Obeloid" />
    <widget name="Plasmoid" />
    <widget name="Sigroid" />
    <widget name="Tetroid" />
    <widget name="Turboid" />
  </widgets>  

Always sort the elements via some mechanism. In this example, the name is the obvious choice. While sorting can't be enforced using a schema, the sort will make detection of changes much easier to detect.

## Storage

If XML is used to store versions, it will still be necessary to create indexes. Storing the indexed value in a relational database would duplicate information (which would never change) but would enable standard SQL queries and easy integration of version control queries with queries about other aspects of the SASS application.

### Oracle Berkeley DB

Features: xpath retreival, pre-parsing, more efficient for large numbers of files?

Oracle Berkeley DB is a family of open source embeddable databases that allows developers to incorporate within their applications a fast, scalable, transactional database engine with industrial grade reliability and availability. As a result, customers and end-users will experience an application that simply works, reliably manages data, can scale under extreme load, but requires no ongoing database administration.

### Subversion

Features: command-line administration, well-known, stable, feature-rich, variable substitution, file system storage

### XML Examples

```
  <version_control>
    <controlled_resource version="0" crID="[GUID1]" creation_date="[TIMESTAMP]">
      <access_control_list>
        <user id="2324" type="owner"/>
        <user id="3223" type="viewer"/>
      </access_control_list>
      <path>[DIRECTORY]</path>
      <resource rID="[GUID2]">
        ... data ...
      </resource>
    </controlled_resource>
  </version_control>
```

It's important to note that the system as two ids. The crID or controlled resource id is maintained by the version control system and provides a unique handle for each version of a resource. The rID or resource id remains constant over all versions of a resource. However, each resource has an id unique from all other resources.

Using XML for storage enables several functions. One such function is the ability to request several versions from the version control system at once. That XML might look like (notice that the viewer ACL has been removed in the second version).

```
<version_control>
  <controlled_resources>
    <controlled_resource version="0" crID="[GUID1]" creation_date="">
      <access_control_list>
        <user id="2324" type="owner"/>
        <user id="3223" type="viewer"/>
      </access_control_list>
      <path>[DIRECTORY]</path>
      <resource rID="[GUID2]">
        ... data ...
      </resource>
    </controlled_resource>
    <controlled_resource version="1" crID="[GUID3]" creation_date="">
      <access_control_list>
        <user id="2324" type="owner"/>
      </access_control_list>
      <path>[DIRECTORY]</path>
      <resource rID="[GUID2]">
        ... data ...
      </resource>
    </controlled_resource>
  </controlled_resources>
</version_control>
```

## Questions

What if the ACL for an object changes?

Scenario:

1. Jan 1, John is given read access to Gun Handling 101. This causes the version number of the course to increment to 3.4.5.

2. Jan 5, Harry is given read access to Gun Handling 101 and a new constraint is added. This causes the version number of the course to increment to 3.4.6.

3. Jan 15, John's access is removed. This causes the version number of the course to increment to 3.4.7.

4. Feb 1, Mary, an administrator, pulls up version 3.4.5 of the Gun Handling 101 course as a template for Gun handling 201 because the constraint added for 3.4.6 is not needed.

Should the ACL be cleared when any non-current version is used? Should the ACL be updated to the current version's ACL? Should the user be given a choice?
