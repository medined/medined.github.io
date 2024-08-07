---
layout: post
title: Erik Hatcher's Self-Contained Lucene Example
date: '2003-04-21T17:10:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[lucene]]"
modified_time: '2003-04-21T17:24:33.000-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200178453
blogger_orig_url: http://affy.blogspot.com/2003/04/erik-hatchers-self-contained-lucene.md
year: 2003
theme: java
---

I was fortunate enough to attend <a href="http://www.ehatchersolutions.com/servlets/blogscene">Erik Hatcher's</a> Lucene
presentation at the Northern Virginia Software Symposium. The symposium was organized by <a
	href="http://www.nofluffjuststuff.com/images/nfjs_logo.gif"><img
		src="http://www.nofluffjuststuff.com/images/nfjs_logo.gif" border="0"></a>. I'll talk more about Lucene as I
explore its abilties.


For now, I'm just documenting the self-contained example program that Erik used as his first example:
<pre>
/*
 * Created on Apr 21, 2003
 *
 */
package com.affy.lucene.tutorial;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * This program indexes three strings using Lucene
 * and then searches for the string that contains
 * the "doc1" string.
 */
public class ErikHatcherSelfContainedExample {

	public static void main(String[] args) throws IOException {

		String docs[] = {
			"doc1 - present!",
			"doc2 is right here",
			"and do not forget lil ol doc3"
		};

		Directory directory = new RAMDirectory();

		Analyzer analyzer = new StandardAnalyzer();
		IndexWriter writer = new IndexWriter(directory, analyzer, true);
		for (int j = 0; j < docs.length; j++) {
			Document d = new Document();
			d.add(Field.Text("contents", docs[j]));
			writer.addDocument(d);
		}
		writer.close();

		Searcher searcher = new IndexSearcher(directory);
		Query query = new TermQuery(new Term("contents", "doc1"));
		Hits hits = searcher.search(query);
		System.out.println("doc1 hits: " + hits.length());
		searcher.close();

		System.out.println("Done.");
	}
}
</pre>