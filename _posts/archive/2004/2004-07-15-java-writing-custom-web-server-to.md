---
layout: post
title: Java - Writing a Custom Web Server to Interpolate Velocity Templates.
date: '2004-07-15T00:18:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-05-03T08:39:20.373-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-108986732341567548
blogger_orig_url: http://affy.blogspot.com/2004/07/java-writing-custom-web-server-to.md
year: 2004
theme: java
---

<blockquote>This is the first entry of several that will chronicle my writing a self-contained web application designed
    to attach keywords to photo (or images). I did search with Google, but I saw no open-source applications that let
    you search through an image database. I'll be using my own embedded http server, Lucene, Prevayler, and Velocity.
</blockquote>


<p>Quite frankly, I don't like to deal with the configuration and deployment issues associated with Tomcat and JBoss. So
    I've modified the SingleFileHTTPServer written by Elliot Harrold to serve interpolated Velocity templates.</p>
<p>In order to get the MugTrapServer class to work, you'll need a config\mugtrap.properties file:</p>
<pre>
# The Velocity Properties
file.resource.loader.path=./src/velocity
file.resource.loader.cache=false
file.resource.loader.modificationCheckInterval=120
runtime.log.logsystem=org.apache.velocity.runtime.log.SimpleLog4JLogSystem
runtime.log.logsystem.log4j.category=org.wwre.velocity
input.encoding=UTF-8
output.encoding=UTF-8
</pre>
<p>You'll also need a starting Velocity template called src\velocity\index.vm:
<pre>
&lt;html&gt;
  &lt;head/&gt;
  &lt;body&gt;
  	&lt;br&gt;
  	Statistics ===========================================&lt;br&gt;
	Request: ${request}&lt;br&gt;
    Pages Served: ${pagesServed}&lt;br&gt;
	HTTP Server Uptime: ${upTime}  ms.&lt;br&gt;
	Created At: ${creationDate}&lt;br&gt;
    = Documentation ===========================================&lt;br&gt;
    &lt;a href="/stop">/stop&lt;/a> - ends the splitter process.&lt;br&gt;
  &lt;/body&gt;
&lt;/html&gt;
</pre>
</p>
<p>And finally, the source code:</p>
<pre>
package com.codebits.mugtrap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class MugTrapServer extends Thread {

    private static final String PROP_LOG4J_PROPERTY_FILE = "com.codebits.mugtrap.log4j.property.file";

    private static final String PROPERTY_FILE = "property.file";

    public static void main(String[] args) throws Exception {
        System.setProperty("property.file", "config/mugtrap.properties");

        MugTrapServer mts = new MugTrapServer();
        mts.initialize();

        Thread t = new VelocityServer();
        t.start();
    }

    private byte[] header;

    private int pagesServed = 1;

    private VelocityContext velocityContext = new VelocityContext();

    public MugTrapServer() throws UnsupportedEncodingException {
        StopWatch.add("httpServer");
        String header = "HTTP/1.0 200 OK\r\n" + "Server: WWRE HTTP Server 1.0\r\n" + "Content-type: text/HTML\r\n\r\n";
        this.header = header.getBytes("ASCII");
    }

    public void addObjectToContext(final String key, final Object object) {
        this.velocityContext.put(key, object);
    }

    public void initialize() {
        final String mugTrapPropertyFile = System.getProperty(PROPERTY_FILE);

        // read from the properties file.
        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(mugTrapPropertyFile));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        try {
            Velocity.init(properties);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void interpolateTemplate(final StringWriter sw, final String templateName) {
        try {
            Velocity.mergeTemplate(templateName, "UTF-8", this.velocityContext, sw);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void run() {
        boolean endProcess = false;

        try {
            ServerSocket server = new ServerSocket(9876);
            server.setSoTimeout(1000);

            while (!endProcess) {
                Socket connection = null;
                int bufferSize = 1000;
                StringBuffer request = new StringBuffer(bufferSize);

                try {
                    connection = server.accept();
                    OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    int bufferIndex = 0;
                    while (bufferIndex++ < bufferSize) {
                        int c = in.read();
                        if (c == '\r' || c == '\n' || c == -1)
                            break;
                        request.append((char) c);
                        // If this is HTTP/1.0 or later send a MIME header

                    }
                    StringBuffer buffer = new StringBuffer();

                    if (request.toString().indexOf("HTTP/") != -1) {
                        out.write(this.header);
                    }

                    DecimalFormat myFormat = (DecimalFormat) DecimalFormat.getInstance();
                    myFormat.setDecimalSeparatorAlwaysShown(true);

                    // Sample request: GET /index.vm HTTP/1.1
                    StringTokenizer st = new StringTokenizer(request.toString(), " ");
                    if (st.countTokens() != 3) {
                        throw new RuntimeException("Expected 3 tokens but found " + st.countTokens() + " in " + request.toString());
                    }

                    String httpCommand = st.nextToken();
                    String template = st.nextToken().substring(1); // ignore leading slash.
                    // I don't care about the third token.

                    if (template.toUpperCase().equals("STOP")) {
                        buffer.append("MugTrap server stopped.");
                        endProcess = true;
                    } else {
                        if (template.length() == 0 || template.endsWith(".vm")) {
                            template = "index.vm";
                        }
                        addObjectToContext("request", request.toString());
                        addObjectToContext("pagesServed", myFormat.format(pagesServed));
                        addObjectToContext("upTime", StopWatch.elapsedTime("httpServer"));
                        addObjectToContext("creationDate", new Date());

                        StringWriter sw = new StringWriter();
                        interpolateTemplate(sw, template);
                        buffer.append(sw.getBuffer());
                        sw.close();
                    }

                    out.write(buffer.toString().getBytes());
                    out.flush();
                    out.close();
                    pagesServed++;

                } catch (InterruptedIOException e) {
                    // ignore this error. when the access() times out, then
                    // a new accept() is started. The timeout lets calling
                    // processes easily stop the server.
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } // end while(!endProcess)
        } // end try
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    } // end run
}
</pre>
</p>