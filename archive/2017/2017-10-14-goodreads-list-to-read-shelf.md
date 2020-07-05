---
layout: post
title: Goodreads - Using Python To List To Read Shelf
author: David Medinets
categories: python goodreads
year: 2017
---

I've been using http://goodreads.com for several years and have good substantial list of books on my To Read shelf. I thought it would be interesting to see how hard using the GoodReads API would be.

Not too difficult.

First I registered with GoodReads to get an API key. Then I ran the following curl command.

```
export GDKEY=XXX
curl --silent \
  -o goodreads-to-read.xml \
  "https://www.goodreads.com/review/list/32902953.xml?key=$GDKEY&v=2&shelf=to-read&per_page=200"
```

I created a small python script to handle reading the XML.

```
import xml.etree.ElementTree as etree
tree = etree.parse('/usr/src/app/goodreads-to-read.xml')
root = tree.getroot()
for child in root:
    if child.tag == 'reviews':
        for review in child:
            for reviewElements in review:
                if reviewElements.tag == 'book':
                    for bookElements in reviewElements:
                        if bookElements.tag == 'title':
                            print(bookElements.text)
```

The last step runs the script:

```
docker run \
  --rm=true \
  -v $(pwd):/usr/src/app \
  python:3.6.3 \
  python /usr/src/app/parsexml.py | head | sort
```

You could use python directly, of course, if you have it already installed.
