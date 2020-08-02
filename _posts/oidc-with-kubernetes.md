# OIDC With Kubernetes

## CIS Benchmark

3.1.1 Client certificate authentication should not be used for users (Not Scored)

Alternative mechanisms provided by Kubernetes such as the use of OIDC should be
implemented in place of client certificates.

## Goal

To install an OIDC which can be used instead of tokens for kubectl authentication and authorization.

1. Install KeyCloak.

 https://www.keycloak.org/getting-started/getting-started-kube

2. Install Gangway?

## Facts

`apiserver` uses `/etc/kubernetes/ssl/ca.crt` as the client CA file.

## Glossary

* **OIDC** - OpenID Connect extends the OAuth 2.0 protocol to include user identity built into the resulting tokens which the resource server will verify and consume.
* **OAuth 2.0** - a popular method for authorizing applications to a resource server, using some identity provider such as a social media website or other account holding platform.

## Research

`openssl req -new -key jbeda.pem -out jbeda-csr.pem -subj "/CN=jbeda/O=app1/O=app2"` creates a CSR for `jbeda` belonging to two groups.

### kube-oidc-proxy

We use cert-manager, another Jetstack open-source project that enables automatic provisioning and renewal of TLS certificates in Kubernetes. This will be used to back kube-oidc-proxy as well as other dependencies in the cluster.

Dex is a server to enable access to OIDC identity providers and is deployable to Kubernetes. The server enables multiple ‘connectors’ that expose OIDC issuers such as GitHub, Linkedin or even simple Username and Passwords. Along with Dex is gangway, a Heptio project which is a web server that facilitates the OAuth browser flow via Dex and provides a convenient kubeconfig to be downloaded once authenticated. When deploying into multi-cloud, the Dex deployment will be shared between clusters, however care should be taken to ensure that the tokens issued by different clusters are not using the same audience so the validity of tokens is only scoped to a single cluster.

Finally, kube-oidc-proxy is deployed along with configuration to accept identity issued by Dex.

## References

* Kubernetes Documentation
  * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
  * https://kubernetes.io/docs/reference/access-authn-authz/certificate-signing-requests/
  * https://kubernetes.io/docs/concepts/cluster-administration/certificates/
* OIDC
  * kube-oidc-proxy - a reverse proxy server to authenticate users using OIDC to Kubernetes API servers where OIDC authentication is not available (i.e. managed Kubernetes providers such as GKE, EKS, etc).
    * https://blog.jetstack.io/blog/kube-oidc-proxy/
    * https://github.com/jetstack/kube-oidc-proxy
  * Auth0
    * https://www.kubeflow.org/docs/aws/authentication-oidc/
  * KeyCloak
    * https://itnext.io/protect-kubernetes-dashboard-with-openid-connect-104b9e75e39c
* https://beyondthekube.com/identity-management-for-on-prem-clusters/
* https://beyondthekube.com/identity-management-part-3-setting-up-oidc-authentication/
* https://www.youtube.com/watch?v=xYMA-S75_9U
* https://medium.com/@mrbobbytables/kubernetes-day-2-operations-authn-authz-with-oidc-and-a-little-help-from-keycloak-de4ea1bdbbe


## Research

* https://itnext.io/protect-kubernetes-dashboard-with-openid-connect-104b9e75e39c
* http://www.devoperandi.com/kubernetes-authentication-openid-connect/
* https://github.com/OpenUnison/openunison-k8s-login-oidc
* https://medium.com/preply-engineering/k8s-auth-a81f59d4dff6
* https://v1-16.docs.kubernetes.io/docs/reference/command-line-tools-reference/kube-apiserver/
* https://aws.amazon.com/blogs/opensource/kubernetes-ingress-aws-alb-ingress-controller/
* https://aws.amazon.com/blogs/opensource/consistent-oidc-authentication-across-multiple-eks-clusters-using-kube-oidc-proxy/
* https://medium.com/@mrbobbytables/kubernetes-day-2-operations-authn-authz-with-oidc-and-a-little-help-from-keycloak-de4ea1bdbbe


sudo yum install -y git
git clone https://github.com/neuvector/kubernetes-cis-benchmark.git
cd kubernetes-cis-benchmark
./master 1.5.1

* https://github.com/dev-sec/cis-kubernetes-benchmark
* https://sonobuoy.io/
* https://github.com/dev-sec/linux-baseline
* https://github.com/dev-sec/ssh-baseline
* https://github.com/dev-sec/cis-dil-benchmark
* https://github.com/aquasecurity/kube-hunter

* https://httpbin.org/
* https://github.com/negz/kubehook
* https://github.com/dexidp/dex


------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------


# Deploy Service With HTTP

## Prerequisites

* Set the namespace and host for the service to be deployed later.

```bash
export TEXT_RESPONDER_HOST="text-responder.david.va-oit.cloud"
```

* Create a namespace.

```bash
kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
    name: text-responder
    labels:
        name: text-responder
EOF
```

* Set the `kubectl` context so that `text-responder` is the current namespace. Undo this action by using `default` as the namespace.

```bash
kubectl config set-context --current --namespace=text-responder
```

## Deploy text-responder Application

The service being deployed just returns "silverargint" as a text response. Its simplicity makes it a great for this kind of preliminary exploration.

* Deploy a small web server that returns a text message.

```bash
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: text-responder
  namespace: text-responder
spec:
  selector:
    matchLabels:
      app: text-responder
  replicas: 2
  template:
    metadata:
      labels:
        app: text-responder
    spec:
      containers:
      - name: text-responder
        image: hashicorp/http-echo
        args:
        - "-text=silverargint"
        ports:
        - containerPort: 5678
---
apiVersion: v1
kind: Service
metadata:
  name: text-responder
  namespace: text-responder
spec:
  ports:
  - port: 80
    targetPort: 5678
  selector:
    app: text-responder
EOF
```

* The following resources now exist. Your pods and replicaset will be differenty named.

```
deployment.apps/text-responder
replicaset.apps/text-responder-bcdd88f9d
pod/text-responder-bcdd88f9d-d7w9h
pod/text-responder-bcdd88f9d-vjxff
service/text-responder
```

* Check the service is running. You should see the `text-responder` service in the list.

```bash
kubectl --namespace text-responder get service
```

* Get the load balancer hostname assigned to your ingress service.

```bash
export CLUSTER_NAME="tempest"

K8S_HOSTNAME=$(aws elbv2 describe-load-balancers --region us-east-1 --query "LoadBalancers[?LoadBalancerName==\`$CLUSTER_NAME-nlb\`].DNSName" --output text)
echo $K8S_HOSTNAME
```

* Create a vanity URL for the echo service. The URL will be $TEXT_RESPONDER_HOST that was defined above. Make it an A ALIAS record that points to the $K8S_HOSTNAME found above.

* Test the hostname.

```bash
dig $TEXT_RESPONDER_HOST
```

* Curl should get the default 404 response. The HTTPS request should fail because the local issuer certificate can't be found.

```bash
curl http://$TEXT_RESPONDER_HOST
```

```bash
curl https://$TEXT_RESPONDER_HOST
```

* Route traffic directed at the `text-responder` subdomain within the cluster.

```bash
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: text-responder-ingress
  namespace: text-responder
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
  - host: $TEXT_RESPONDER_HOST
    http:
      paths:
      - backend:
          serviceName: text-responder
          servicePort: 80
EOF
```

* Call the service. It should return `silverargint`.

```
curl http://$TEXT_RESPONDER_HOST
```
