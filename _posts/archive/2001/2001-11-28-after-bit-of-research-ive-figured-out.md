---
layout: post
title: How to Parse XML Using Oracle And PLSQL
date: '2001-11-28T18:38:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
- "[[oracle]]"
modified_time: '2007-11-21T12:08:53.711-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-7482941
blogger_orig_url: http://affy.blogspot.com/2001/11/after-bit-of-research-ive-figured-out.md
year: 2001
---

<p>After a bit of research, I've figured out how to parse xml using Oracle9i.


  The process is a bit more involved than Oracle8i but there is more functionality. The following bit of PL/SQL code
  will parse the following XML:</p>
<pre>
&lt;r session="sess01" updator="medined" object="1005769145473">
 &lt;f n="PMA_APPROVAL_MEANS" v="Identicality Per FAR 21.303(c)"/>
&lt;/r&gt;
</pre>
<p>You'll need to place the following code into context that makes sense for your projects, but here is the gist:</p>
<pre>
-- declare attributes
p          xmlparser.Parser;
doc        xmldom.DOMDocument;
element    xmldom.DOMElement;
facts      xmldom.DOMNodeList;
node       xmldom.DOMNode;
attributes xmldom.DOMNamedNodeMap;

-- parse the xml packet
p := xmlparser.newParser;
xmlparser.setValidationMode(p, FALSE);
xmlparser.parseBuffer(p, v_xml_packet);
doc  := xmlparser.getDocument(p);

BEGIN
  element := xmldom.getDocumentElement(doc);
EXCEPTION
  RAISE;
END;

v_session := xmldom.getAttribute(element, 'session');
v_updator := xmldom.getAttribute(element, 'updator');
v_object  := xmldom.getAttribute(element, 'object');

facts := xmldom.getChildrenByTagName( element, 'f');
FOR j IN 1..xmldom.getLength(facts) LOOP

        node := xmldom.item(facts, j-1);
        attributes := xmldom.getAttributes(node);

        if (xmldom.isNull(attributes) = FALSE) then
          num_attributes := xmldom.getLength(attributes);

          -- loop through attributes
          for i in 0..num_attributes-1 loop
             node := xmldom.item(attributes, i);
             attribute_name := xmldom.getNodeName(node);
             if attribute_name = 'n' then
                 v_fact_name := xmldom.getNodeValue(node);
             end if;
             if attribute_name = 'v' then
                 v_fact_value := xmldom.getNodeValue(node);
             end if;
          end loop;

          ---- do some processing here.

       end if;
END LOOP;
</pre>