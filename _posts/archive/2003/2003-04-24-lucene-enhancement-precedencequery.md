---
layout: post
title: Lucene; Enhancement; The PrecedenceQuery Class.
date: '2003-04-24T14:27:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[lucene]]"
modified_time: '2003-04-24T14:27:21.623-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200194642
blogger_orig_url: http://affy.blogspot.com/2003/04/lucene-enhancement-precedencequery.md
year: 2003
theme: java
---

As I was working with Lucene the other day, I envisioned looking for a document about a terrorist (call me XXX) and how
the resulting list of results should be ranked. So I created the PrecedenceQuery class so that I could ask for all
document containing XXX <b>and</b> a series of tokens such as /iraq/syria/israel/usa/united states.


The document should be ranked higher (more relevant) as the list moves from left to right. This type of search is
applicable to many different situations. In the realm of computer applications, this technique could be used in
configuration management so that global values are overridden by environment which in turn can be overridden by
machine-specific information. Here is the code for the class:

<pre>
/*
 * Created on Apr 24, 2003
 *
 */
package com.affy.lucene;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * @author medined
 */
public class PrecedenceQuery extends Query {
	private Term term;
	private String delimiter;

	/**
	 * Accepts a Term whose value looks like
	 * \default\development\dmedinets. The idea
	 * is that the score of the located documents
	 * are boosted higher for each successive token
	 * in the Term. The weight given to each term
	 * is double that of the previous term. I hope
	 * that this simplistic weighting scheme is ok.
	 */
	public PrecedenceQuery(Term term, String delimiter) {
		this.term = term;
		this.delimiter = delimiter;
	}

	public PrecedenceQuery(Term term) {
		this.term = term;
		this.delimiter = "/";
	}

	public Query rewrite(IndexReader reader) throws IOException {
		BooleanQuery query = new BooleanQuery();
		StringTokenizer st = new StringTokenizer(this.term.text(), this.delimiter);
		int boost = 1;
		while (st.hasMoreTokens()) {
		  String token = st.nextToken();
		  TermQuery tq = new TermQuery(new Term(this.term.field(), token));
		  tq.setBoost(boost);
		  query.add(tq, false, false);
		  boost = boost + boost;
		}
		return query;
	}

	public Query combine(Query[] queries) {
		return Query.mergeBooleanQueries(queries);
	}

	/** Prints a user-readable version of this query. */
	public String toString(String field) {
		StringBuffer buffer = new StringBuffer();
		if (!term.field().equals(field)) {
			buffer.append(term.field());
			buffer.append(":");
		}
		buffer.append(term.text());
		buffer.append('*');
		if (getBoost() != 1.0f) {
			buffer.append("^");
			buffer.append(Float.toString(getBoost()));
		}
		return buffer.toString();
	}

}
</pre>