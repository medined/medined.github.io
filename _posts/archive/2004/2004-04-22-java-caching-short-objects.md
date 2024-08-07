---
layout: post
title: Java; Caching Short Objects
date: '2004-04-22T11:46:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-22T11:51:48.046-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108264876964524178
blogger_orig_url: http://affy.blogspot.com/2004/04/java-caching-short-objects.md
year: 2004
theme: java
---

While looking at the POI source code, I noticed that a lot of Short objects were being created. So I looked around for a
small stand-alone class that would allow me to cache Short object.


I did see some pages devoted to sparse arrays and matrixes (notably the COLT package) however they were too large for my
purposes.
<p>I wrote the <code>shortShortCache</code> class shown below. It usage should be pretty obvious but please contact me
    if you have any difficulty using the class or suggestions for improvement.</p>
<pre>
/**
 * Provides a cache for Short objects. This class should be used when the same short values
 * are used over and over to avoid the cost of continously creating the same Short objects.
 *
 * Since the get() method creates Short objects and adds them to the cache, the put()
 * method never needs to be called outside this class.
 */

public class shortShortCache {

    /** This is how the cache is used. */
    public static void main(String[] args) {
        shortShortCache ssm = new shortShortCache();
        short numEntries = (short) 2000;
        short start = (short) 23454;
        short middle = (short) (start + (numEntries / 2));
        short end = (short) (start + numEntries);
        for (short i = start; i <= end; i++) {
            ssm.put(i);
        }
        // is the first short cached?
        System.out.println(start + ": " + ssm.get(start));
        // is the middle short cached?.
        System.out.println(middle + ": " + ssm.get(middle));
        // is the last short cached?
        System.out.println(end + ": " + ssm.get(end));
        System.out.println("Done.");
    }

    /** The initalize size of the cache. */
    private int   initialSize     = 500;

    /** How much to grow the cache when its capacity is reached. */
    private int   increment       = 500;

    /** The size of the cache, which is not the same as the number of entries in the caceh. */
    private int   currentCapacity = 0;

    /**
     * The number of entries in the cache.
     */
    protected int distinct        = 0;

    /** The maximum number of entries so that the cache doesn't grow unbounded. */
    protected int maxEntries      = 3000;

    /**
     * The cached short values.
     */
    private short table[];

    /**
     * The cached Short methods
     */
    private Short values[];

    /** A no-args constructor which uses all of the defaults. */
    public shortShortCache() {
        super();
        clear();
    }

    /** A constructor that lets the user set the control variables. */
    public shortShortCache(final int _initialSize, final int _increment, final int _maxEntries) {
        super();
        this.initialSize = _initialSize;
        this.increment = _increment;
        // we quietly handle the error of a _maxEntries parameter less than the _initialSize parameter.
        if (_maxEntries < _initialSize) {
            this.maxEntries = _initialSize;
        } else if (_maxEntries > Short.MAX_VALUE) {
            this.maxEntries = Short.MAX_VALUE;
        } else {
            this.maxEntries = _maxEntries;
        }
        clear();
    }

    /** Create and/or clear the cache. */
    public void clear() {
        this.table = new short[this.initialSize];
        this.values = new Short[this.initialSize];
        this.currentCapacity = this.initialSize;
        this.distinct = 0;
    }

    /**
     * Returns the value to which this map maps the specified key. If the short value
     * is not in the cache, then create a Short object automatically.
     */
    public Short get(short key) {
        Short rv = null;

        for (int i = 0; i < this.distinct; i++) {
            if (this.table[i] == key) {
                rv = this.values[i];

            }
        }

        // If the key is not in the cache, then add it
        // to the cache.
        if (rv == null) {
            rv = new Short(key);
            if (this.currentCapacity < this.maxEntries) {
                put(key);
            }
        }

        return rv;
    }

    /**
     * Add a mapping from a short to a Short object.
     *
     * If the size of the cache is too small, then expand it. If the size of the cache
     * is more than the maxEntries, then return. The get() method automatically
     * creates a Short object for any short values not in the cache.
     */
    public void put(short key) {
        if (this.currentCapacity < this.maxEntries) {
            if (this.distinct == this.currentCapacity) {
                int newCapacity = this.currentCapacity + this.increment;

                // store the current cache.
                short oldTable[] = this.table;
                Short oldValues[] = this.values;

                // create new arrays.
                short newTable[] = new short[newCapacity];
                Short newValues[] = new Short[newCapacity];

                this.table = newTable;
                this.values = newValues;

                // move info from old table to new table.
                for (int i = this.currentCapacity; i-- > 0;) {
                    newTable[i] = oldTable[i];
                    newValues[i] = oldValues[i];
                }

                this.currentCapacity = newCapacity;
            }
            this.table[this.distinct] = key;
            this.values[this.distinct] = new Short(key);
            this.distinct++;
        }
    }

    /** Returns the number of key-value mappings in this map. */
    public int size() {
        return this.distinct;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     */
    public boolean containsKey(short key) {
        boolean rv = false;

        for (int i = 0; i < this.distinct; i++) {
            if (this.table[i] == key) {
                rv = true;
                break;
            }
        }
        return rv;
    }

    /**
     * Returns true if this map maps one or more keys to the specified value.
     */
    public boolean containsValue(Short value) {
        boolean rv = false;

        if (value != null) {
            for (int i = 0; i < this.distinct; i++) {
                if (this.values[i].equals(value)) {
                    rv = true;
                    break;
                }
            }
        }
        return rv;
    }

    /**
     * Returns true if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        boolean rv = false;
        if (this.distinct > 0) {
            rv = true;
        }
        return rv;
    }

    /**
     * From http://www.ftponline.com/javapro/2004_03/online/rule4_03_31_04/:
     *
     * Java's object cloning mechanism can allow an attacker to manufacture new
     * instances of classes that you define�without executing any of the class's
     * constructors. Even if your class is not cloneable, the attacker can define a
     * subclass of your class, make the subclass implement java.lang.Cloneable,
     * and then create new instances of your class by copying the memory images
     * of existing objects. By defining this clone method, you will prevent such attacks.
     */
    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * List all short values in the cache.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("shortShortMap[" + this.distinct + "]: ");
        for (int i = 0; i < this.distinct; i++) {
            sb.append(this.table[i] + ", ");
        }
        return sb.toString();
    }
}
</pre>