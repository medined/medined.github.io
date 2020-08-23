---
layout: post
title: How Can I Get More Detail About Connection Refused Message In Ingress Nginx Controller Pod?
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

Here is the full API URL. Notice that the IP address is not internet addressable. The second line removes the host name from the string. We'll add the controller information later.

```
API_URL="https://10.233.0.1:443/api/v1/endpoints?allowWatchBookmarks=true&labelSelector=OWNER%21%3DTILLER&resourceVersion=5840136&timeout=6m24s&timeoutSeconds=384&watch=true"
API_URL=$(echo $API_URL | cut -d'/' -f4-)
```

The `E8023` error does not provide much information. I know that `10.233.0.1` corresponds to my controller node. And I know that the Ingress Nginx Controller is running on my worker node.

**NOTE** This cluster has just one controller and one worker node.

* Set the namespace and service account of the pod.

```
NAMESPACE=ingress-nginx
SERVICE_ACCOUNT=ingress-nginx
```

* Get the service account's token secret's name. This will look like `ingress-nginx-token-sc7dw`.

```bash
SECRET=$(kubectl -n $NAMESPACE get serviceaccount ${SERVICE_ACCOUNT} -o json | jq -Mr '.secrets[].name | select(contains("token"))')
```

* Get the bearer token from the secret and decode it. This is a 941 character string. It looks like `eyJhbGc...48AzXRQ`.

```bash
TOKEN=$(kubectl -n $NAMESPACE get secret ${SECRET} -o json | jq -Mr '.data.token' | base64 -d)
```

* Make a local copy of the certificate authority file by the secret.

```bash
kubectl -n $NAMESPACE get secret ${SECRET} -o json | jq -Mr '.data["ca.crt"]' | base64 -d > /tmp/ca.crt
```

* Get the hostname of the API server. There are several ways to accomplish this. I choose to use the `cluster-info` command. The only tricky part to this techique is there are non-visible characters around the host name that need to be removed. That's what the `cut` and `rev` commands are doing.

```bash
APISERVER=$(kubectl cluster-info  | head -n 1 | awk '{print $6}' | cut -b16- | rev | cut -b10- | rev)
echo "[$APISERVER]"
```

* Run the API call that caused the original error.

```bash
curl https://$APISERVER:6443/$API_URL --header "Authorization: Bearer $TOKEN" --cacert /tmp/ca.crt
{"type":"ERROR","object":{..."message":"too old resource version: 5840136 (5856237)","reason":"Expired","code":410}}
```

This was not an expected outcome. Looking closer at the URL being fetched shows:

```
api/v1/endpoints
```

I think that should be

```
openapi/v1/endpoints
```

Here is the result of that change.

```
curl https://$APISERVER:6443/openapi/v1/endpoints --header "Authorization: Bearer $TOKEN" --cacert /tmp/ca.crt
{
  "paths": [
    "/apis",
    "/apis/",
    "/apis/apiextensions.k8s.io",
    "/apis/apiextensions.k8s.io/v1",
    "/apis/apiextensions.k8s.io/v1beta1",
    "/healthz",
    "/healthz/etcd",
    "/healthz/log",
    "/healthz/ping",
    "/healthz/poststarthook/crd-informer-synced",
    "/healthz/poststarthook/generic-apiserver-start-informers",
    "/healthz/poststarthook/start-apiextensions-controllers",
    "/healthz/poststarthook/start-apiextensions-informers",
    "/livez",
    "/livez/etcd",
    "/livez/log",
    "/livez/ping",
    "/livez/poststarthook/crd-informer-synced",
    "/livez/poststarthook/generic-apiserver-start-informers",
    "/livez/poststarthook/start-apiextensions-controllers",
    "/livez/poststarthook/start-apiextensions-informers",
    "/metrics",
    "/openapi/v2",
    "/readyz",
    "/readyz/etcd",
    "/readyz/log",
    "/readyz/ping",
    "/readyz/poststarthook/crd-informer-synced",
    "/readyz/poststarthook/generic-apiserver-start-informers",
    "/readyz/poststarthook/start-apiextensions-controllers",
    "/readyz/poststarthook/start-apiextensions-informers",
    "/readyz/shutdown",
    "/version"
  ]
}
```
