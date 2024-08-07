---
layout: post
title: Faceted Classification
date: '2003-04-14T00:41:00.000-04:00'
author: David Medinets
categories:
- "[[search]]"
modified_time: '2003-04-14T00:44:23.000-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200142878
blogger_orig_url: http://affy.blogspot.com/2003/04/faceted-classification.md
year: 2003
---

I worked to create a product hierarchy at Toyrus.com back in 1999. Little did I know that I was actually creating a
system to support Faceted Classifications. I love learning new words that codify my real-world experience. My
understanding of Faceted Classification is that several exclusive descriptive hierarchies are used to describe objects.


At the beginning of a search, each node in the hierarchy shows the number of objects associated with that node. However,
as more specific-nodes are selected from any hierarchy, the number of objects belonging to each node (in all
hierarchies) is reduced to exclude objects that don't include the trait associated with the selected nodes. An online
demonstration of this type of search can be found at <a href="http://www.facetmap.com">FacetMap.com</a>.

<p>Here is are examples of classifications:</p>
<p><b>Bad Classification</b></p>
<ul>
    <li>evening red dress
    <li>morning dress
    <li>green dress
    <li>wool green dress
    <li>silk dress
</ul>
<p>Notice that you can't build an object such as a "green evening dress" because "evening dress" appear only in the
    compound phrase of "evening red dress".</p>
<p><b>Good Classification</b></p>
<ul>
    <li>dresses by colour
        <ul>
            <li>red
            <li>green
            <li>yellow
        </ul>
    <li>dresses by material
        <ul>
            <li>silk
            <li>wool
            <li>cotton
        </ul>
    <li>dresses by purpose
        <ul>
            <li>evening
            <li>bathrobe
            <li>sleeping garments
        </ul>
</ul>
<p>Using the above hierarchies, it is easy to select object traits such as "yellow cotton evening dress".</p>
<p>BTW, I stole this example from an email message on the FacetedClassification group at Yahoo Groups.</p>