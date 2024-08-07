---
layout: post
title: 'Optimization for JudoScript: JavaObject.pickMethod is an expensive call.'
date: '2004-04-08T18:53:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-08T18:58:11.466-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108146480746455538
blogger_orig_url: http://affy.blogspot.com/2004/04/optimization-for-judoscript.md
year: 2004
theme: java
---

The <code>getMethods()</code> method of the <code>Class</code> class is an expensive call to make. So I added a static
map to hold the method array between invokations of <code>pickMethod()</code>.


This change reduced execution time of my test case by 3.4%. Instead of calling <code>getMethods()</code> 15,032 times it
is only called once. And instead of creating 3.5 million objects only 386 objects are created. A nice bit of
optimization if I do say so myself!

<pre>
    private static final Map methodsInClass = new HashMap();

    final MatchFinder pickMethod(String methodName, Variable[] paramVals, int[] javaTypes) throws Exception {
        Class cls = null;
        for (int x = 0; x < this.classes.length; x++) {
            cls = this.classes[x];

            // make sure the map can't grow unbounded.
            if (methodsInClass.size() > 200) {
                methodsInClass.clear();
            }
            Method[] mthds = (Method[]) methodsInClass.get(cls);
            if (mthds == null) {
                mthds = cls.getMethods();
                methodsInClass.put(cls, mthds);
            }

            int len = (paramVals == null) ? 0 : paramVals.length;
            MatchFinder ret = new MatchFinder(0.0, len, javaTypes);
            for (int i = 0; i < mthds.length; i++) {
                if (mthds[i].getName().equals(methodName)) {
                    MatchFinder mf = matchParams(mthds[i].getParameterTypes(), paramVals, javaTypes);
                    if (mf.score > ret.score) {
                        ret = mf;
                        ret.method = mthds[i];
                    }
                }
            }
            if (ret.score > 0.0)
                return ret;
        }
        return NOT_MATCH;
    }
</pre>