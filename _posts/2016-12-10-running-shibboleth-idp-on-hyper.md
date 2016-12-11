---
layout: post
title: Running Shibboleth IDP on hyper.sh
author: David Medinets
categories: shibboleth hyper
---

Hyper.sh is a secure container hosting service. In this entry, I use a container to run the Shibboleth Identity Provider at hyper.sh

First start a centos container where you will run Shibboleth IDP. The command below run interactively.

```
./hyper run --expose 8080 --publish 80:8080 --size m2 -it --rm centos /bin/bash
```

On the hyper.sh page, attach a floating ip address to this container.

Now install some software packages using yum.


```
yum install -y tomcat tomcat-webapps tomcat-admin-webapps tomcat-docs-webapp tomcat-javadoc
```

Define some environment variables that are needed by tomcat.

```
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.111-1.b15.el7_2.x86_64/jre
export CATALINA_HOME=/var/lib/tomcat
export JAVA_OPTS="-Xms512"
```

Add the following two lines to /etc/tomcat/tomcat-users.xml

```
<role rolename="manager-gui"/>
<user username="admin" password="admin" roles="manager-gui"/>
```

Start tomcat just to make sure that everything is working so far.

```
/usr/libexec/tomcat/server start
```

Visit http://<ipaddr>:8080/ to see the tomcat welcome page.

Visit http://<ipaddr>:8080/manager/ to see the tomcat manager page.

Press ^C to stop tomcat.

Install and configure Shibboleth IDP.

```
curl -O https://shibboleth.net/downloads/identity-provider/latest/shibboleth-identity-provider-3.3.0.tar.gz
tar xfz shibboleth-identity-provider-3.3.0.tar.gz
rm shibboleth-identity-provider-3.3.0.tar.gz
cd shibboleth-identity-provider-3.3.0
bin/install.sh
cd /opt/shibboleth-idp/
```

Edit conf/access-control.xml to add '0.0.0.0/32' to the list of allowed ranges.

Change the ownership of files.

```
chown -R tomcat *
```

Create the IDP web application.

```
cat << EOF > /etc/tomcat/Catalina/localhost/idp.xml
<Context docBase="/opt/shibboleth-idp/war/idp.war" privileged="true" antiResourceLocking="false" swallowOutput="true"/>
EOF
```

Start tomcat and visit the /manager page. You could now see the /idp application.

```
/usr/libexec/tomcat/server start
```

Pless ^C to  stop tomcat.

Now install the JSTL jar file.

```
cd /opt/shibboleth-idp/edit-webapp/WEB-INF/lib
curl -O https://build.shibboleth.net/nexus/service/local/repositories/thirdparty/content/javax/servlet/jstl/1.2/jstl-1.2.jar
chown tomcat jstl-1.2.jar
```

Rebuild the war file.

```
cd ../../..
bin/build.sh
```

Start tomcat and visit the IDP page at http://<ipaddr>/idp. You can see the status page at http://<ipaddr>/idp/status. And more information at http://<ipaddr>/idp/shibboleth.

```
/usr/libexec/tomcat/server start
```
