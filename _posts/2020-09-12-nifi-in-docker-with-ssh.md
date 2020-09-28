---
layout: post
title: Apache NiFi in a Container With X.509 Certificate Authentication
author: David Medinets
categories: nifi docker security
year: 2020
theme: nifi
---

## Goal

To run Apache NiFi inside a Docker container supporting HTTPS using an X.509 certificate for authentication.

## Introduction

This information is on various web sites. I'm not treading any new ground. But I am going to show the whole process in one place which I have not quite seen before.

## Procedure

### Certificate Generation

Following information from https://mintopsblog.com/2017/11/01/apache-nifi-configuration/, this section generates the certficates needed for SSL.

* Start a throw-away container. You'll run the NiFi toolkit from this container.

```bash
docker run --rm --name toolkit -d apache/nifi
```

* The tls-toolkit.sh script will be used to create the required self-signed certificate, keystore, truststore and pre-configured nifi.properties. It also creates a client certificate and password. If you are running these commands more than once, make sure you are not overwritting important files in the `conf` directory. You have been warned!

```bash
docker exec \
  -ti toolkit \
  /opt/nifi/nifi-toolkit-current/bin/tls-toolkit.sh \
    standalone \
    -n 'nifi1.bluejay.local' \
    -C 'CN=admin, OU=NIFI'

# Copy the public certificate of the Certificate Authority.
docker cp toolkit:/opt/nifi/nifi-current/nifi-cert.pem        conf
# Copy the Base64-encoded private key of the Certificate Authority in PKCS #1 PEM format.
docker cp toolkit:/opt/nifi/nifi-current/nifi-key.key         conf

docker cp toolkit:/opt/nifi/nifi-current/nifi1.bluejay.local  conf
docker cp toolkit:/opt/nifi/nifi-current/nifi2.bluejay.local  conf
docker cp toolkit:/opt/nifi/nifi-current/nifi3.bluejay.local  conf

docker cp toolkit:/opt/nifi/nifi-current/CN=admin_OU=NIFI.p12      conf
docker cp toolkit:/opt/nifi/nifi-current/CN=admin_OU=NIFI.password conf

docker stop toolkit
```

NOTE: Use `nifi[1-3].bluejay.local` if you need to generate certificates for multiple NiFi nodes.

NOTE: Spaces matter in the `CN=admin, OU-NIFI` identity. They are not needed but be consistent.

* Add the client certificate to your browser by importing the `.p12` file.

* Update your `/etc/hosts` file with the following line so you can use the hostname instead of an IP address.

```
127.0.0.1 nifi1.bluejay.local
```

### Apache NiFi

* Create the NiFi container. The newly created certificates are made available as /opt/certs.

```bash
docker run -d \
  -e AUTH=tls \
  -e KEYSTORE_PATH=/opt/certs/keystore.jks \
  -e KEYSTORE_TYPE=JKS \
  -e KEYSTORE_PASSWORD=$(grep keystorePasswd conf/nifi1.bluejay.local/nifi.properties | cut -d'=' -f2) \
  -e TRUSTSTORE_PATH=/opt/certs/truststore.jks \
  -e TRUSTSTORE_PASSWORD=$(grep truststorePasswd conf/nifi1.bluejay.local/nifi.properties | cut -d'=' -f2) \
  -e TRUSTSTORE_TYPE=JKS \
  -e INITIAL_ADMIN_IDENTITY="CN=admin, OU=NIFI" \
  -e NIFI_WEB_PROXY_CONTEXT_PATH=/nifi \
  -e NIFI_WEB_PROXY_HOST=nifi1.bluejay.local \
  --hostname nifi1.bluejay.local \
  --name nifi \
  -p 8443:8443 \
  -v $(pwd)/conf/nifi1.bluejay.local:/opt/certs:ro \
  -v /data/projects/nifi-shared:/opt/nifi/nifi-current/ls-target \
  apache/nifi
```

* Watch until the container is ready. You are looking for the `NiFi has started. The UI is available` message.

```bash
docker logs -f nifi
```

* Visit the NiFi page.

```bash
xdg-open https://nifi1.bluejay.local:8443/nifi
```

* Logs inside the container are located at `/opt/nifi/nifi-current/logs`. The files are:
  * nifi-app.log
  * nifi-bootstrap.log - contains the start command and parameters.
  * nifi-user.log
