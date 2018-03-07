---
layout: default
title: Categories
category: menu
---

{% for category in site.categories %}
### {{ category[0] }}

{% for post in category[1] %}
  * <a href="{{ post.url }}">{{ post.title }}</a></li>
{% endfor %}
{% endfor %}
