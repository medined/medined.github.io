---
layout: post
title: ActiveMapper - Using UUID Values for Primary Keys
date: '2005-11-01T16:34:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2005-11-01T16:44:12.706-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-113088145270713483
blogger_orig_url: http://affy.blogspot.com/2005/11/activemapper-using-uuid-values-for.md
year: 2005
theme: java
---

<p>I am a big fan of using UUID values as primary keys but I won't use this blog to articulate why. Other people in the
    blogosphere have argued back and forth on the subject of primary keys - you don't need me to repeat the arguments.
</p>


<p>If you do want to use UUID values, then ActiveMapper will not be useful in its current version. The good news is that
    adding support is trivally easy.</p>

<p>In order to use UUID values with ActiveMapper you need to do three things:</p>
<ol>
    <li>Update the ActiveMapper.assignNewId() method as shown below - just add the two highlighted lines.
        <pre>
    protected Object assignNewId(Object o) {
        PersistentField pf = (PersistentField) persistentObject.getPersistentFields().get("id");
        Object newId = null;
        if (pf.getJavaType() == (java.lang.Long.class))
            newId = new Long(persistentObject.
getIncrementer().nextLongValue());
<b>        else if (pf.getJavaType() == UUID.class)
            newId = UUID.randomUUID();</b>
        else if (pf.getJavaType() == (java.lang.Integer.class))
            newId = new Integer(persistentObject.getIncrementer().
nextIntValue());
        try {
            Method m = o.getClass().getMethod(ActiveMapperUtils.
setterName(pf.getFieldName()), new Class[] { newId.getClass() });
            m.invoke(o, new Object[] { newId });
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        }
        return newId;
    }
</pre>
    </li>
    <li>
        Use the following instance variable in your domain objects:
        <pre>private UUID id = null;
</pre>
    </li>
    <li>
        Use the following field definition when creating SQL tables:
        <pre>
id char(36) not null
</pre>
        </il>
</ol>