---
layout: post
title: Java; POI Optimization - Speeding up the RecordFactory class.
date: '2004-04-22T13:43:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-22T13:48:01.996-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108265583476561569
blogger_orig_url: http://affy.blogspot.com/2004/04/java-poi-optimization-speeding-up_22.md
year: 2004
theme: java
---

This optimization needs two posts. This post presents a class that maps short values to a <code>Constructor</code>
object.


The <code>RecordFactory</code> class maps the short id value in the Excel file to a Java class. In the current version
of POI, that relationship is kept, quite logically, in a <code>Map</code> object. However, using a <code>Map</code>
object required that the short value be converted into a <code>Short</code> object quite frequently. This conversion is
inefficient and, if some specially coding is done, unneeded. Note that the following class,
<code>shortToConstructorCache</code>, is derivative of my earlier <code>shortShortCache</code> class.</p>
<pre>
/**
 * Cache a mapping from a short value to a Constructor object.
 *
 * This class is designed to optimize the RecordFactory.createRecord()
 * method. It might be useful in other contexts, but I did not check.
 */
public class shortToConstructorCache {

    /**
     * The number of entries in the cache.
     */
    protected int       distinct;

    /**
     * The cached short values.
     */
    private short       table[];

    /**
     * The cached constructor methods
     */
    private Constructor values[];

    /**
     * RecordFactory uses a statically created array of
     * classes to initialize the cache and the entries
     * in the cache are never changed nor added to.
     */
    public shortToConstructorCache(Class[] records) {
        super();

    	this.table = new short[records.length];
    	this.values = new Constructor[records.length];

        Constructor constructor;

        for (int i = 0; i < records.length; i++) {
            Class record = null;
            short sid = 0;

            record = records[i];
            try {
                sid = record.getField("sid").getShort(null);
                constructor = record.getConstructor(new Class[] {short.class, short.class, byte[].class});
            } catch (Exception illegalArgumentException) {
                illegalArgumentException.printStackTrace();
                throw new RecordFormatException("Unable to determine record types");
            }

            if (constructor == null) {
                throw new RecordFormatException("Unable to get constructor for sid [" + sid + "].");
            } else {
                this.table[this.distinct] = sid;
                this.values[this.distinct] = constructor;
                this.distinct++;
            }
        }
    }

    /** Gets the Constructor object related to a given
     * key.
     */
    public Constructor get(short key) {
        Constructor rv = null;

        	for (int i = 0; i < this.distinct; i++) {
        	    if (this.table[i] == key) {
        	        rv = this.values[i];
        	    }
        	}

        return rv;
    }

    /** Returns the number of entries in the cache. */
    public int size() {
        return this.distinct;
    }

    /** We're breaking encapsulation but some code in RecordFactory
     * wants the information and I want to change RecordFactory as
     * little as possible.
     */
    public short[] getTable() {
        return this.table;
    }

}
</pre>