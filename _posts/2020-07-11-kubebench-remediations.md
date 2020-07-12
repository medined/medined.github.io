---
layout: post
title: Remediations For kube-bench Findings
author: David Medinets
categories: kubernetes kubebench ansible
year: 2020
theme: kubernetes
---

# Table of Contents
{:.no_toc}
* unordered list
{:toc}

* * *

This article shows how I am remediating the results of `kube-bench`. It will be updated over time, hopefully.

## Master

* 1 Master Node Security Configuration
* 1.1 Master Node Configuration Files
* 1.1.1 Ensure that the API server pod specification file permissions are set to 644 or more restrictive

```
Pass.
```

* 1.1.2 Ensure that the API server pod specification file ownership is set to root:root

```
Pass.
```

* 1.1.3 Ensure that the controller manager pod specification file permissions are set to 644 or more restrictive

```
Pass.
```

* 1.1.4 Ensure that the controller manager pod specification file ownership is set to root:root

```
Pass.
```

* 1.1.5 Ensure that the scheduler pod specification file permissions are set to 644 or more restrictive

```
Pass.
```

* 1.1.6 Ensure that the scheduler pod specification file ownership is set to root:root

```
Pass.
```

* 1.1.7 Ensure that the etcd pod specification file permissions are set to 644 or more restrictive

```
Kubespray creates an "etcd
 cluster separate from the controller node. Therefore, the etcd.yaml file does not exist.
```

* 1.1.8 Ensure that the etcd pod specification file ownership is set to root:root

```
Kubespray creates an "etcd
 cluster separate from the controller node. Therefore, the etcd.yaml file does not exist.
```

* 1.1.9 Ensure that the Container Network Interface file permissions are set to 644 or more restrictive

```
    - name: 1.1.9 Ensure that the Container Network Interface file permissions are set to 644 or more restrictive
      file:
        path: /etc/cni/net.d
        mode: "644"
        recurse: yes
        state: directory
```

* 1.1.10 Ensure that the Container Network Interface file ownership is set to root:root

```
    - name: 1.1.10 Ensure that the Container Network Interface file ownership is set to root:root
      file:
        group: root
        owner: root
        path: /etc/cni/net.d
        recurse: yes
        state: directory
```

* 1.1.11 Ensure that the etcd data directory permissions are set to 700 or more restrictive

```
Kubespray creates an "etcd
 cluster separate from the controller node. Therefore, the etcd.yaml file does not exist.
```

* 1.1.12 Ensure that the etcd data directory ownership is set to etcd:etcd

```
Kubespray creates an "etcd
 cluster separate from the controller node. Therefore, the etcd.yaml file does not exist.
```

* 1.1.19 Ensure that the Kubernetes PKI directory and file ownership is set to root:root

```
    - name: 1.1.19 Ensure that the Kubernetes PKI directory and file ownership is set to root:root
      file:
        group: root
        owner: root
        path: /etc/kubernetes/pki
        recurse: yes
        state: directory
```

* 1.1.20 Ensure that the Kubernetes PKI certificate file permissions are set to 644 or more restrictive

```
    - name: 1.1.20 Ensure that the Kubernetes PKI certificate file permissions are set to 644 or more restrictive
      block:

        - name: 1.1.20 - Find files.
          find:
            paths: /etc/kubernetes/pki
            patterns: "*.crt"
          register: crt_glob

        - name: 1.1.20 - Check permissions.
          file:
            path: "{{ item.path }}"
            mode: "644"
            state: file
          with_items:
            - "{{ crt_glob.files }}"
```

* 1.1.21 Ensure that the Kubernetes PKI key file permissions are set to 600

```
    - name: 1.1.21 Ensure that the Kubernetes PKI key file permissions are set to 600
      block:

        - name: 1.1.21 - Find files.
          find:
            paths: /etc/kubernetes/pki
            patterns: "*.key"
          register: key_glob

        - name: 1.1.21 - Check permissions.
          file:
            path: "{{ item.path }}"
            mode: "600"
            state: file
          with_items:
            - "{{ key_glob.files }}"
```

* 1.2 API Server
* 1.2.1 Ensure that the --anonymous-auth argument is set to false

```
    # If a change is made, the apiserver is restarted.
    - name: 1.2.1 Ensure that the --anonymous-auth argument is set to false
      lineinfile:
        path: /etc/kubernetes/manifests/kube-apiserver.yaml
        regexp: '^    - --anonymous-auth=True'
        line: "    - --anonymous-auth=False"
```

* 1.2.2 Ensure that the --basic-auth-file argument is not set

```
Pass.
```

* 1.2.3 Ensure that the --token-auth-file parameter is not set

```
Pass.
```

* 1.2.4 Ensure that the --kubelet-https argument is set to true

```
Pass.
```

* 1.2.5 Ensure that the --kubelet-client-certificate and --kubelet-client-key arguments are set as appropriate

```
Pass.
```

* 1.2.6 Ensure that the --kubelet-certificate-authority argument is set as appropriate

The code below does not have the current certificate authority file but it does show how to resolve this issue when the correct file is identified.

```
    - name: 1.2.6 Ensure that the --kubelet-certificate-authority argument is set as appropriate
      block:
        - name: 1.2.6 check kubelet-certificate-authority flag
          command: grep --silent "kubelet-certificate-authority=" /etc/kubernetes/manifests/kube-apiserver.yaml
          register: kca_flag
          check_mode: no
          ignore_errors: yes
          changed_when: no

        - name: 1.2.6 add kubelet-certificate-authority flag
          replace:
            path: /etc/kubernetes/manifests/kube-apiserver.yaml
            regexp: '^(.*kubelet-client-certificate=.*)$'
            replace: '    - --kubelet-certificate-authority=/etc/kubernetes/ssl/ca.crt\n\1'
          when: kca_flag.rc != 0
```

* 1.2.7 Ensure that the --authorization-mode argument is not set to AlwaysAllow

```
Pass.
```

* 1.2.8 Ensure that the --authorization-mode argument includes Node

```
Pass.
```

* 1.2.9 Ensure that the --authorization-mode argument includes RBAC

```
Pass.
```

* 1.2.10 Ensure that the admission control plugin EventRateLimit is set

```
WARN
```

* 1.2.11 Ensure that the admission control plugin AlwaysAdmit is not set

```
Pass.
```

* 1.2.12 Ensure that the admission control plugin AlwaysPullImages is set

```
WARN
```

* *1.2.13 Ensure that the admission control plugin SecurityContextDeny is set if PodSecurityPolicy is not used

```
WARN
```

* 1.2.14 Ensure that the admission control plugin ServiceAccount is set

```
Pass.
```

* 1.2.15 Ensure that the admission control plugin NamespaceLifecycle is set

```
Pass.
```

* 1.2.16 Ensure that the admission control plugin PodSecurityPolicy is set

```
FAIL
```

* 1.2.17 Ensure that the admission control plugin NodeRestriction is set

```
Pass.
```

* 1.2.18 Ensure that the --insecure-bind-address argument is not set

```
Pass.
```

* 1.2.19 Ensure that the --insecure-port argument is set to 0

```
Pass.
```

* 1.2.20 Ensure that the --secure-port argument is not set to 0

```
Pass.
```

* 1.2.21 Ensure that the --profiling argument is set to false

```
Pass.
```

* 1.2.22 Ensure that the --audit-log-path argument is set

```
FAIL
```

* 1.2.23 Ensure that the --audit-log-maxage argument is set to 30 or as appropriate

```
FAIL
```

* 1.2.24 Ensure that the --audit-log-maxbackup argument is set to 10 or as appropriate

```
FAIL
```

* 1.2.25 Ensure that the --audit-log-maxsize argument is set to 100 or as appropriate

```
FAIL
```

* 1.2.26 Ensure that the --request-timeout argument is set as appropriate

```
Pass.
```

* 1.2.27 Ensure that the --service-account-lookup argument is set to true

```
Pass.
```

* 1.2.28 Ensure that the --service-account-key-file argument is set as appropriate

```
Pass.
```

* 1.2.29 Ensure that the --etcd-certfile and --etcd-keyfile arguments are set as appropriate

```
Pass.
```

* 1.2.30 Ensure that the --tls-cert-file and --tls-private-key-file arguments are set as appropriate

```
Pass.
```

* 1.2.31 Ensure that the --client-ca-file argument is set as appropriate

```
Pass.
```

* 1.2.32 Ensure that the --etcd-cafile argument is set as appropriate

```
Pass.
```

* 1.2.33 Ensure that the --encryption-provider-config argument is set as appropriate

```
FAIL
```

* 1.2.34 Ensure that encryption providers are appropriately configured

```
FAIL
```

* 1.2.35 Ensure that the API Server only makes use of Strong Cryptographic Ciphers

```
FAIL
```

* 1.3 Controller Manager
* 1.3.1 Ensure that the --terminated-pod-gc-threshold argument is set as appropriate

```
Pass.
```
* 1.3.2 Ensure that the --profiling argument is set to false

```
Pass.
```

* 1.3.3 Ensure that the --use-service-account-credentials argument is set to true

```
Pass.
```

* 1.3.4 Ensure that the --service-account-private-key-file argument is set as appropriate

```
Pass.
```

* 1.3.5 Ensure that the --root-ca-file argument is set as appropriate

```
Pass.
```

* 1.3.6 Ensure that the RotateKubeletServerCertificate argument is set to true

```
FAIL
```

* 1.3.7 Ensure that the --bind-address argument is set to 127.0.0.1

```
FAIL
```

* 1.4 Scheduler
* 1.4.1 Ensure that the --profiling argument is set to false

```
FAIL
```

* 1.4.2 Ensure that the --bind-address argument is set to 127.0.0.1

```
FAIL
```

## Etcd

* 2 Etcd Node Configuration
* 2 Etcd Node Configuration Files
* 2.1 Ensure that the --cert-file and --key-file arguments are set as appropriate (Scored)

```
FAIL

Follow the etcd service documentation and configure TLS encryption Then, edit the etcd pod specification file /etc/kubernetes/manifests/etcd.yaml on the master node and set the below parameters.

  --cert-file=</path/to/ca-file>
  --key-file=</path/to/key-file>
```

* 2.2 Ensure that the --client-cert-auth argument is set to true (Scored)

```
FAIL

Edit the etcd pod specification file /etc/kubernetes/manifests/etcd.yaml on the master node and set the below parameter.

  --client-cert-auth="true"
```

2.3 Ensure that the --auto-tls argument is not set to true (Scored)

```
Pass
```

* 2.4 Ensure that the --peer-cert-file and --peer-key-file arguments are set as appropriate (Scored)

```
FAIL

Follow the etcd service documentation and configure peer TLS encryption as appropriate for your etcd cluster. Then, edit the etcd pod specification file /etc/kubernetes/manifests/etcd.yaml on the
master node and set the below parameters.

  --peer-client-file=</path/to/peer-cert-file>
  --peer-key-file=</path/to/peer-key-file>
```

* 2.5 Ensure that the --peer-client-cert-auth argument is set to true (Scored)

```
FAIL

Edit the etcd pod specification file /etc/kubernetes/manifests/etcd.yaml on the master node and set the below parameter.

  --peer-client-cert-auth=true
```

* 2.6 Ensure that the --peer-auto-tls argument is not set to true (Scored)

```
Pass
```

* 2.7 Ensure that a unique Certificate Authority is used for etcd (Not Scored)

```
WARN

[Manual test] Follow the etcd documentation and create a dedicated certificate authority setup for the etcd service. Then, edit the etcd pod specification file /etc/kubernetes/manifests/etcd.yaml on the
master node and set the below parameter.

  --trusted-ca-file=</path/to/ca-file>
```

## Controlplane

* 3 Control Plane Configuration
* 3.1 Authentication and Authorization
* 3.1.1 Client certificate authentication should not be used for users (Not Scored)

```
WARN 

Alternative mechanisms provided by Kubernetes such as the use of OIDC should be implemented in place of client certificates.
```

* 3.2 Logging
* 3.2.1 Ensure that a minimal audit policy is created (Scored)

```
WARN 

Create an audit policy file for your cluster.
```

* 3.2.2 Ensure that the audit policy covers key security concerns (Not Scored)

```
WARN 

Consider modification of the audit policy in use on the cluster to include these items, at a minimum.
```

## Node

* 4 Worker Node Security Configuration
* 4.1 Worker Node Configuration Files
* 4.1.1 Ensure that the kubelet service file permissions are set to 644 or more restrictive (Scored)

```
Pass
```

* 4.1.2 Ensure that the kubelet service file ownership is set to root:root (Scored)

```
Pass
```

* 4.1.3 Ensure that the proxy kubeconfig file permissions are set to 644 or more restrictive (Scored)

```
FAIL

Run the below command (based on the file location on your system) on the each worker node. For example,

  chmod 644 /etc/kubernetes/proxy.conf
```

* 4.1.4 Ensure that the proxy kubeconfig file ownership is set to root:root (Scored)

```
FAIL

Run the below command (based on the file location on your system) on the each worker node. For example, 

  chown root:root /etc/kubernetes/proxy.conf
```

* 4.1.5 Ensure that the kubelet.conf file permissions are set to 644 or more restrictive (Scored)

```
Pass
```

* 4.1.6 Ensure that the kubelet.conf file ownership is set to root:root (Scored)

```
Pass
```

* 4.1.7 Ensure that the certificate authorities file permissions are set to 644 or more restrictive (Scored)

```
WARN

audit test did not run: There are no tests
```

* 4.1.8 Ensure that the client certificate authorities file ownership is set to root:root (Scored)

```
Pass
```

* 4.1.9 Ensure that the kubelet configuration file has permissions set to 644 or more restrictive (Scored)

```
Pass
```

* 4.1.10 Ensure that the kubelet configuration file ownership is set to root:root (Scored)

```
Pass
```

* 4.2 Kubelet
* 4.2.1 Ensure that the --anonymous-auth argument is set to false (Scored)

```
Pass
```

* 4.2.2 Ensure that the --authorization-mode argument is not set to AlwaysAllow (Scored)

```
Pass
```

* 4.2.3 Ensure that the --client-ca-file argument is set as appropriate (Scored)

```
Pass
```

* 4.2.4 Ensure that the --read-only-port argument is set to 0 (Scored)

```
FAIL

If using a Kubelet config file, edit the file to set readOnlyPort to 0.

If using command line arguments, edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and set the below parameter in KUBELET_SYSTEM_PODS_ARGS variable.

--read-only-port=0

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

* 4.2.5 Ensure that the --streaming-connection-idle-timeout argument is not set to 0 (Scored)

```
Pass
```

* 4.2.6 Ensure that the --protect-kernel-defaults argument is set to true (Scored)

```
FAIL

If using a Kubelet config file, edit the file to set protectKernelDefaults: true.

If using command line arguments, edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and set the below parameter in KUBELET_SYSTEM_PODS_ARGS variable.

  --protect-kernel-defaults=true

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

* 4.2.7 Ensure that the --make-iptables-util-chains argument is set to true (Scored)

```
Pass
```

* 4.2.8 Ensure that the --hostname-override argument is not set (Not Scored)

```
WARN

Edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and remove the --hostname-override argument from the
KUBELET_SYSTEM_PODS_ARGS variable.

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

* 4.2.9 Ensure that the --event-qps argument is set to 0 or a level which ensures appropriate event capture (Not Scored)

```
WARN

If using a Kubelet config file, edit the file to set eventRecordQPS: to an appropriate level.

If using command line arguments, edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and set the below parameter in KUBELET_SYSTEM_PODS_ARGS variable.

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

* 4.2.10 Ensure that the --tls-cert-file and --tls-private-key-file arguments are set as appropriate (Scored)

```
FAIL

If using a Kubelet config file, edit the file to set tlsCertFile to the location of the certificate file to use to identify this Kubelet, and tlsPrivateKeyFile to the location of the corresponding private key file.

If using command line arguments, edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and set the below parameters in KUBELET_CERTIFICATE_ARGS variable.

  --tls-cert-file=<path/to/tls-certificate-file>
  --tls-private-key-file=<path/to/tls-key-file>

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

* 4.2.11 Ensure that the --rotate-certificates argument is not set to false (Scored)

```
Pass
```

* 4.2.12 Ensure that the RotateKubeletServerCertificate argument is set to true (Scored)

```
FAIL

Edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and set the below parameter in KUBELET_CERTIFICATE_ARGS variable.

  --feature-gates=RotateKubeletServerCertificate=true

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

* 4.2.13 Ensure that the Kubelet only makes use of Strong Cryptographic Ciphers (Not Scored)

```
WARN

If using a Kubelet config file, edit the file to set TLSCipherSuites: to

  TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_128_GCM_SHA256

or to a subset of these values.

If using executable arguments, edit the kubelet service file /etc/systemd/system/kubelet.service on each worker node and set the --tls-cipher-suites parameter as follows, or to a subset of these values.

  --tls-cipher-suites=TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_128_GCM_SHA256

Based on your system, restart the kubelet service. For example:

  systemctl daemon-reload
  systemctl restart kubelet.service
```

## Policies

* 5 Kubernetes Policies
* 5.1 RBAC and Service Accounts
* 5.1.1 Ensure that the cluster-admin role is only used where required (Not Scored)

```
WARN

Identify all clusterrolebindings to the cluster-admin role. Check if they are used and if they need this role or if they could use a role with fewer privileges.

Where possible, first bind users to a lower privileged role and then remove the clusterrolebinding to the cluster-admin role :

  kubectl delete clusterrolebinding [name]
```

* 5.1.2 Minimize access to secrets (Not Scored)

```
WARN

Where possible, remove get, list and watch access to secret objects in the cluster.
```

* 5.1.3 Minimize wildcard use in Roles and ClusterRoles (Not Scored)

```
WARN

Where possible replace any use of wildcards in clusterroles and roles with specific objects or actions.
```

* 5.1.4 Minimize access to create pods (Not Scored)

```
WARN

Where possible, remove create access to pod objects in the cluster.
```

* 5.1.5 Ensure that default service accounts are not actively used. (Scored)

```
WARN

Create explicit service accounts wherever a Kubernetes workload requires specific access to the Kubernetes API server.

Modify the configuration of each default service account to include this value automountServiceAccountToken: false
```

* 5.1.6 Ensure that Service Account Tokens are only mounted where necessary (Not Scored)

```
WARN

Modify the definition of pods and service accounts which do not need to mount service account tokens to disable it.
```

* 5.2 Pod Security Policies
* 5.2.1 Minimize the admission of privileged containers (Not Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.privileged field is omitted or set to false.
```

* 5.2.2 Minimize the admission of containers wishing to share the host process ID namespace (Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.hostPID field is omitted or set to false.
```

* 5.2.3 Minimize the admission of containers wishing to share the host IPC namespace (Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.hostIPC field is omitted or set to false.
```

* 5.2.4 Minimize the admission of containers wishing to share the host network namespace (Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.hostNetwork field is omitted or set to false.
```

* 5.2.5 Minimize the admission of containers with allowPrivilegeEscalation (Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.allowPrivilegeEscalation field is omitted or set to false.
```

* 5.2.6 Minimize the admission of root containers (Not Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.runAsUser.rule is set to either MustRunAsNonRoot or MustRunAs with the range of UIDs not including 0.
```

* 5.2.7 Minimize the admission of containers with the NET_RAW capability (Not Scored)

```
WARN

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.requiredDropCapabilities is set to include either NET_RAW or ALL.
```

* 5.2.8 Minimize the admission of containers with added capabilities (Not Scored)

```
WARN

Ensure that allowedCapabilities is not present in PSPs for the cluster unless it is set to an empty array.
```

* 5.2.9 Minimize the admission of containers with capabilities assigned (Not Scored)

```
WARN

Review the use of capabilites in applications runnning on your cluster. Where a namespace contains applicaions which do not require any Linux capabities to operate consider adding a PSP which forbids the admission of containers which do not drop all capabilities.
```

* 5.3 Network Policies and CNI
* 5.3.1 Ensure that the CNI in use supports Network Policies (Not Scored)

```
WARN

If the CNI plugin in use does not support network policies, consideration should be given to making use of a different plugin, or finding an alternate mechanism for restricting traffic in the Kubernetes cluster.
```

* 5.3.2 Ensure that all Namespaces have Network Policies defined (Scored)

```
WARN

Follow the documentation and create NetworkPolicy objects as you need them.
```

* 5.4 Secrets Management
* 5.4.1 Prefer using secrets as files over secrets as environment variables (Not Scored)

```
WARN

If possible, rewrite application code to read secrets from mounted secret files, rather than from environment variables.
```

* 5.4.2 Consider external secret storage (Not Scored)

```
WARN

Refer to the secrets management options offered by your cloud provider or a third-party secrets management solution.
```

* 5.5 Extensible Admission Control
* 5.5.1 Configure Image Provenance using ImagePolicyWebhook admission controller (Not Scored)

```
WARN

Follow the Kubernetes documentation and setup image provenance.
```

* 5.6 General Policies
* 5.6.1 Create administrative boundaries between resources using namespaces (Not Scored)

```
WARN

Follow the documentation and create namespaces for objects in your deployment as you need them.
```

* 5.6.2 Ensure that the seccomp profile is set to docker/default in your pod definitions (Not Scored)

```
WARN

Seccomp is an alpha feature currently. By default, all alpha features are disabled. So, you would need to enable alpha features in the apiserver by passing "--feature- gates=AllAlpha=true" argument.

Edit the /etc/kubernetes/apiserver file on the master node and set the KUBE_API_ARGS parameter to "--feature-gates=AllAlpha=true"

KUBE_API_ARGS="--feature-gates=AllAlpha=true"

Based on your system, restart the kube-apiserver service. For example:

systemctl restart kube-apiserver.service

Use annotations to enable the docker/default seccomp profile in your pod definitions. An example is as below:

apiVersion: v1
kind: Pod
metadata:
  name: trustworthy-pod
  annotations:
    seccomp.security.alpha.kubernetes.io/pod: docker/default
spec:
  containers:
    - name: trustworthy-container
      image: sotrustworthy:latest
```

* 5.6.3 Apply Security Context to Your Pods and Containers (Not Scored)

```
WARN

Follow the Kubernetes documentation and apply security contexts to your pods. For a suggested list of security contexts, you may refer to the CIS Security Benchmark for Docker Containers.
```

* 5.6.4 The default namespace should not be used (Scored)

```
WARN

Ensure that namespaces are created to allow for appropriate segregation of Kubernetes resources and that all new resources are created in a specific namespace.
```
