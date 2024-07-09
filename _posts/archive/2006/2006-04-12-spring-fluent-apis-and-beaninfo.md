---
layout: post
title: Spring, Fluent APIs, and BeanInfo
date: '2006-04-12T21:31:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-04-16T02:15:15.756-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-114489189610553159
blogger_orig_url: http://affy.blogspot.com/2006/04/spring-fluent-apis-and-beaninfo.md
year: 2006
theme: java
---

Fluent APIs use setters that return objects instead of void. Normally, Spring won't be able to use classes that
implement a fluent API because the setter method won't be found.


However, I have learned that it is possible to write a BeanInfo class which helps the Java reflection system. Here is a
simple example:
<pre>
package com.codebits;

public class PersonBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(beanClass);
    }

    private final static Class beanClass = Person.class;

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor name =
              new PropertyDescriptor("name", beanClass);

            PropertyDescriptor rv[] = { name };

            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
</pre>

<p>which provides guildance for this bean:

<pre>
package com.codebits;

public class Person {

    private String name = null;

    public String getName() { return this.name; }

    public Person setName(String _name) {
        this.name = _name;
        return this;
    }

    public Person() { super(); }
}
</pre>