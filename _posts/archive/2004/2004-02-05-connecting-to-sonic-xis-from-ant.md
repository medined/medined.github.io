---
layout: post
title: Connecting to Sonic XIS from Ant Custom Task Using Java
date: '2004-02-05T13:33:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
- "[[ant]]"
modified_time: '2004-02-05T13:42:03.936-05:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-107600600804289312
blogger_orig_url: http://affy.blogspot.com/2004/02/connecting-to-sonic-xis-from-ant.md
year: 2004
theme: java
---

<p>Making the connection from my desktop computer to the remote XIS server required three steps. Determining the steps
	required the help of the Sonic support team and took a non-trivial amount of time so let me document them here.</p>


<ol>
	<li>Set the <b><code>com.exln.dxe.iorport</code></b> property in the
		<code>[XIS_HOME]/config/xlnserver.properties</code> file. Mine is set to 1052. Note that actual port number that
		you pick is unimportant as long as it is between <b><code>com.exln.dxe.minorbport</code></b> (1051 by default)
		and <b><code>com.exln.dxe.maxorbport</code></b> (1055 by default).</li>
	<li>In the same file, set the <b><code>com.exln.dxe.host</code></b> property to the fully qualified host name or the
		IP address of the XIS server or cluster.</li>
	<li>Use the following code as a template for your own connections in your client code. Make sure that the IORPORT
		number that you use is the same as that in the xlnserver.properties file.
		<pre>
		Properties p = new Properties();
		p.setProperty("com.exln.dxe.adminhost", [IP_ADDRESS_OF_XIS_SERVER]);
		p.setProperty("com.exln.dxe.adminport", "1050");
		p.setProperty("com.exln.dxe.iorport", "[IORPORT_NUMBER");
		try {
			this.session = XlnClientSessionFactory.getSession(p);
			if (this.session == null) {
				LogConfiguration.message("UNABLE_TO_GET_CLIENT_XIS_SESSION");
			}
		} catch (Exception e) {
			LogConfiguration.message(e, "UNABLE_TO_GET_CLIENT_XIS_SESSION");
		}
</pre>
	</li>
</ol>

Good Luck!