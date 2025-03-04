---
layout: post
title: 'Optimization for JudoScript: JudoUtil.registerToBSF is an expensive method to call.'
date: '2004-04-08T18:01:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-04-08T18:07:28.513-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108146170079374786
blogger_orig_url: http://affy.blogspot.com/2004/04/optimization-for-judoscript_08.md
year: 2004
theme: java
---

<p>It seems to me that a scripting engine only needs to register with the BSF engine once. I think that JudoScript
    registers with the engine every time a script is invoked. In order to avoid this situation, I added a static
    variable to track the bsfStatus. In the test case that I was executed, I was able to reduce calls to Class.forName()
    from 8,061 to 43 and the created objects from 649,620 to 162. This also saved 2.6% of execution time.</p>


<pre>
    public static String bsfStatus = "UNKNOWN";

    public static void registerToBSF() {
        if (bsfStatus.equals("UNKNOWN")) {
            try {
                Class[] params = new Class[] {String.class, String.class, Class.forName("[Ljava.lang.String;")};
                Method m = Class.forName("com.ibm.bsf.BSFManager").getMethod("registerScriptingEngine", params);
                Object[] vals = new Object[] {"judoscript", "com.judoscript.BSFJudoEngine", new String[] {"judo", "jud"}};
                m.invoke(null, vals);
                bsfStatus = "AVAILABLE";
            } catch (Exception e) {
                bsfStatus = "UNAVAILABLE";
            } // if BSF is not there, so be it.
        }
    }
</pre>