---
layout: post
title: Java; Using Flag Classes to Replace boolean values.
date: '2004-07-01T10:49:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2004-07-01T11:02:44.753-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108869412332350105
blogger_orig_url: http://affy.blogspot.com/2004/07/java-using-flag-classes-to-replace.md
year: 2004
theme: java
---

<p>Let me supply some background information before talking about the Flag classes. Part of my project involves using
    Java classes to access XML. I don't want to use the currently available unmarshalling tools because I only need part
    of the XML data inside my Java application. Therefore, I use XPATHs to select the data as needed. In order to ensure
    that I can use the same class to selectively unmarshall from XML and provide the ability to run test cases, I use
    the following technique:</p>


<pre>
  private String senderName = null;

  public String getSenderName() {
    if (this.senderName == null) {
      this.senderName = XmlHelper.getTextAtXpath(getCurrentElement(), "./senderName");
    }
    return this.senderName;
  }

  public void setSenderName(final String _senderName) {
    this.senderName = _senderName;
  }
</pre>
<p>This technique works fine for strings because they are nullable. However, boolean values are harder to handle. So I
    create a Flag class:</p>
<pre>
public class MsgIsParent{

    private boolean value;

    public static final MsgIsParent TRUE = new MsgIsParent(true);
    public static final MsgIsParent FALSE = new MsgIsParent(false);

    private MsgIsParent(final boolean _value) {
        this.value = _value;
    }

    public static MsgIsParent factory(final boolean _value) {
        if (_value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
}
</pre>
<p>With this small helper available, I can follow the same technique that I used for Strings:</p>
<pre>
    private MsgIsParent msgIsParent = null;

    public MsgIsParent getMsgIsParent() {
        if (this.msgIsParent == null) {
            this.msgIsParent = MsgIsParent.factory(getRootElement().getNodeName().equals(getMessageType()));
        }
        return this.msgIsParent;
    }

    public void setMsgIsParent(final MsgIsParent _msgIsParent) {
        this.msgIsParent = _msgIsParent;
    }
</pre>