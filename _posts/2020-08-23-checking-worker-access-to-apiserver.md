---
layout: post
title: How Can I Get More Detail About Connection Refused Message In  Ingress Nginx Controller Pod?
author: David Medinets
categories: kubernetes
year: 2020
theme: kubernetes
---

I am seeing the following message in my Ingress Nginx Controller logs. The message has been elided so it is easier to read.

```
E0823 .../reflector.go:125: Failed to watch *v1.Endpoints: Get "<API URL>":
  dial tcp 10.233.0.1:443: connect: connection refused
```

Here is the full API URL. Store it for future use.

```
API_URL="https://10.233.0.1:443/api/v1/endpoints?allowWatchBookmarks=true&labelSelector=OWNER%21%3DTILLER&resourceVersion=5840136&timeout=6m24s&timeoutSeconds=384&watch=true"
```

The `E8023` error does not provide much information. I know that `10.233.0.1` corresponds to my controller node. And I know that the Ingress Nginx Controller is running on my worker node.

**NOTE** This cluster has just one controller and one worker node.

In order to find out the exact error, you'll need to make the HTTPS fetch yourself. However, the fetch needs to be made from the worker node to duplicate the network request. SSH makes this possible.

Since I'm using `kubespray` I need to use the `./inventory/hosts` file to find the relevant IP addresses and use the `bastion.conf` file when making an SSH.

First find the IP address that SSH needs:

```
CONTROLLER=$(cat ./inventory/hosts | grep "\[kube-master\]" -A 1 | tail -n 1)
CONTROLLER_IP=$(cat ./inventory/hosts | grep $CONTROLLER | grep ansible_host | cut -d'=' -f2)

HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-node\]" -A 1 | tail -n 1)
WORKER_IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
```

Now make the SSH call. Use `-vvv` to display debug messages. Use the CA certificate that Kubernetes is using.

```
ssh -F ssh-bastion.conf centos@$WORKER_IP \
  curl -vvv --cacert /etc/kubernetes/ssl/ca.crt $API_URL
```

Finally, the underlying error is seen:

```
endpoints is forbidden: User "system:anonymous" cannot list resource "endpoints" in API group "" at the cluster scope
```
