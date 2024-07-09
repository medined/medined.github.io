---
layout: post
title: 'CodeBit: The Simplest Possible EJB for JBoss?'
date: '2003-03-21T14:32:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2003-03-21T14:35:42.000-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200024751
blogger_orig_url: http://affy.blogspot.com/2003/03/codebit-simplest-possible-ejb-for.md
year: 2003
theme: java
---

<p>I've been trying to simplify the process of making an EJB. And I think I have the simplest possible bean. And a
	simple client program. I've packaged this all rather neatly into a zip file so that you can instantly create an
	Eclipse project. Here is the zip file: <a href="/assets/SimpleBean.zip">SimpleBean.zip</a></p>


<p><b>SimpleBean.java</b></p>
<p>Make sure to get your JBoss server started. Then customize the build.xml file for your configuration (mostly just the
	first 10 lines or so). Then open a DOS window and run <code>ant deploy</code>. After the Ant process is done, run
	the SimpleBeanClient class from Eclipse.</p>
<p>Let me know if you have any problems.</p>
<pre>
package com.affy.entity;

import java.rmi.RemoteException;
import javax.ejb.*;
import com.affy.util.SimpleBeanUtil;

/**
 * @ejb.bean name="SimpleBean" type="CMP" cmp-version="2.x" jndi-name="ejb/affy/SimpleBean" primkey-field="pKey"
 * @ejb.util generate="physical"
 */
public abstract class SimpleBean implements EntityBean {

	public EntityContext mContext;

	/** @ejb.persistence */
	public abstract String getPKey();
	public abstract void setPKey(String pKey);

	/** @ejb.create-method view-type="remote" */
	public String ejbCreate() throws EJBException, CreateException {
		this.setPKey(SimpleBeanUtil.generateGUID(this));
		return this.getPKey();
	}

	public void ejbPostCreate() { /* do nothing */ }
	public void ejbRemove() throws RemoveException, EJBException, RemoteException {}
	public void ejbActivate() throws EJBException, RemoteException {}
	public void ejbPassivate() throws EJBException, RemoteException {}
	public void ejbLoad() throws EJBException, RemoteException {}
	public void ejbStore() throws EJBException, RemoteException {}
	public void setEntityContext(EntityContext lContext) { mContext = lContext; }
	public void unsetEntityContext() { mContext = null; }
}
</pre>
<p><b>SimpleClient.java</b></p>
<pre>
package com.affy.client;

import com.affy.remote.SimpleBean;
import com.affy.home.SimpleBeanHome;
import com.affy.util.SimpleBeanUtil;

class SimpleBeanClient {

	public static void main(String[] args) {
		System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		System.setProperty("java.naming.provider.url", "localhost:1099");

		try {
			SimpleBeanHome home = SimpleBeanUtil.getHome();

			SimpleBean sb = home.create();
			String pKey = (String)sb.getPrimaryKey();
			System.out.println("ID: " + pKey);

			SimpleBean b = home.findByPrimaryKey(pKey);
			System.out.println("ID: " + b.getPrimaryKey());

			System.out.println("Done.");
		} catch(Exception e) {
			System.out.println(e.toString());
		}
	}
}
</pre>