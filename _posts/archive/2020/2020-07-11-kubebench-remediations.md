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

## Acknowledgements

This work is being done at the request of the Enterprise Container Working Group (ECWG) of the Office of Information and Technology (OIT - https://www.oit.va.gov/) at the Department of Veteran Affairs.

## Updates

2020-11-01 - I have updated some of the remediations to show how to change KubeSpray playbooks before the `cluster.yml` is used. This is a better way to handle remediations.

## Article

This article shows how I am remediating the results of `kube-bench`. It will be updated over time, hopefully.

## Findings

| Result        | Count |
| ------------- | ----: |
| PASS          | 63 |
| FAIL          | 0 |
| WARN          | 0 |
| JUSTIFICATION | 53 |
| ------------- | ----: |
| Total         | 116 |

## Recommendations From Rancher

https://rancher.com/docs/rancher/v2.x/en/security/hardening-2.4/ has some useful information.

Make these changes on all nodes.

* Create a `sysctl` settings file. I have not evaluated these settings.

```bash
cat <<EOF > /etc/sysctl.d/90-kubelet.conf
vm.overcommit_memory=1
vm.panic_on_oom=0
kernel.panic=10
kernel.panic_on_oops=1
kernel.keys.root_maxbytes=25000000
EOF
```

* Run `sysctl` to enable the settings.

```bash
sysctl -p /etc/sysctl.d/90-kubelet.conf
```

## How To run Remediation Ansible Playbook

In order to resolve some of the WARN and FAIL findings, you'll need to create an Ansible playbook. Then run it. Below is an example playbook which you can modify as needed for your cluster.

It simply display the Ansible version. Notice that it needs to setup the proxy information. And run the `kubespray_defaults` role in order to setup variables. Run this role for each node that is changed.

```yaml
cat <<EOF > cis-benchmark-remediation.yml
---
- name: Check ansible version
  import_playbook: ansible_version.yml

#
# Just a reminder that all include the bastion servers.
#

- hosts: all
  gather_facts: false
  tags: always
  tasks:
    - name: "Set up proxy environment"
      set_fact:
        proxy_env:
          http_proxy: "{{ http_proxy | default ('') }}"
          HTTP_PROXY: "{{ http_proxy | default ('') }}"
          https_proxy: "{{ https_proxy | default ('') }}"
          HTTPS_PROXY: "{{ https_proxy | default ('') }}"
          no_proxy: "{{ no_proxy | default ('') }}"
          NO_PROXY: "{{ no_proxy | default ('') }}"
      no_log: true

#
# Just the controllers
#
- hosts: kube-master
  any_errors_fatal: "{{ any_errors_fatal | default(true) }}"
  become: yes
  gather_facts: False
  roles:
    - { role: kubespray-defaults }
    tasks:
      - debug:
          var: ansible_version

#
# Just the workers
#
- hosts: kube-node
  any_errors_fatal: "{{ any_errors_fatal | default(true) }}"
  gather_facts: False
  roles:
    - { role: kubespray-defaults }
    tasks:
      - debug:
          var: ansible_version

#
# The controllers and the workers
#
- hosts: kube-master kube-node
  any_errors_fatal: "{{ any_errors_fatal | default(true) }}"
  gather_facts: False
  roles:
    - { role: kubespray-defaults }
    tasks:
      - debug:
          var: ansible_version


- hosts: etcd
  any_errors_fatal: "{{ any_errors_fatal | default(true) }}"
  become: yes
  gather_facts: False
  roles:
    - { role: kubespray-defaults }
  tasks:
    - debug:
        var: ansible_version
EOF
```

The following command runs that playbook.

```bash
ansible-playbook \
    -i ./inventory/hosts \
    ./cis-benchmark-remediation.yml \
    -e ansible_user=centos \
    -e bootstrap_os=centos \
    --become \
    --become-user=root \
    --flush-cache \
    -e ansible_ssh_private_key_file=$PKI_PRIVATE_PEM \
    | tee /tmp/kubebench-remediation-$(date "+%Y-%m-%d_%H:%M").log
```

## Changing APISERVER

When you change the apiserver manifest, that static pod will automatically restart. Make sure to wait for it to become responsive before moving to another task.

This is how to wait:

```yaml
        - name: Wait for k8s apiserver
          wait_for:
            host: "{{ kube_apiserver_access_address }}"
            port: "{{ kube_apiserver_port }}"
            timeout: 180
```

## References

* https://cloud.google.com/kubernetes-engine/docs/concepts/cis-benchmarks has some interesting things to say about why some tests are not passing.

## Remediations

### 1. Master

* 1 Master Node Security Configuration
* 1.1 Master Node Configuration Files
* 1.1.1 Ensure that the API server pod specification file permissions are set to 644 or more restrictive

```
PASS
```

* 1.1.2 Ensure that the API server pod specification file ownership is set to root:root

```
PASS
```

* 1.1.3 Ensure that the controller manager pod specification file permissions are set to 644 or more restrictive

```
PASS
```

* 1.1.4 Ensure that the controller manager pod specification file ownership is set to root:root

```
PASS
```

* 1.1.5 Ensure that the scheduler pod specification file permissions are set to 644 or more restrictive

```
PASS
```

* 1.1.6 Ensure that the scheduler pod specification file ownership is set to root:root

```
PASS
```

* 1.1.7 Ensure that the etcd pod specification file permissions are set to 644 or more restrictive

Kubespray creates an "etcd" cluster separate from the controller node. Therefore, the etcd.yaml file does not exist.

```
JUSTIFICATION for FAIL
```

* 1.1.8 Ensure that the etcd pod specification file ownership is set to root:root

Kubespray creates an "etcd" cluster separate from the controller node. Therefore, the etcd.yaml file does not exist.

```
JUSTIFICATION for FAIL
```

* 1.1.9 Ensure that the Container Network Interface file permissions are set to 644 or more restrictive

This test is a warning because the location of the CNI files can vary between kubernetes distributions. The remediation below will resolve this finding for Kubespray CentOS clusters. The `master.yaml` file can be updated to score this test if needed.

```
JUSTIFICATION for WARN (MANUAL)

    - name: 1.1.9 Ensure that the Container Network Interface file permissions are set to 644 or more restrictive
      file:
        path: /etc/cni/net.d
        mode: "644"
        recurse: yes
        state: directory
```

* 1.1.10 Ensure that the Container Network Interface file ownership is set to root:root

This test is a warning because the location of the CNI files can vary between kubernetes distributions. The remediation below will resolve this finding for Kubespray CentOS clusters. The `master.yaml` file can be updated to score this test if needed.

```
JUSTIFICATION for WARN (MANUAL)

    - name: 1.1.10 Ensure that the Container Network Interface file ownership is set to root:root
      file:
        group: root
        owner: root
        path: /etc/cni/net.d
        recurse: yes
        state: directory
```

* 1.1.11 Ensure that the etcd data directory permissions are set to 700 or more restrictive

Kubespray creates an independent `etcd` cluster from the control plane node. Therefore, the etcd.yaml file does not exist.

```
JUSTIFICATION for FAIL
```

* 1.1.12 Ensure that the etcd data directory ownership is set to etcd:etcd

Kubespray creates an independent `etcd` cluster from the control plane node. Therefore, the etcd.yaml file does not exist.

```
JUSTIFICATION for FAIL
```

* 1.1.19 Ensure that the Kubernetes PKI directory and file ownership is set to root:root

The remediation playbook resolves this issue but kube-bench marks this as a manual test.

```
JUSTIFICATION for WARN (MANUAL)

    - name: 1.1.19 Ensure that the Kubernetes PKI directory and file ownership is set to root:root
      file:
        group: root
        owner: root
        path: /etc/kubernetes/pki
        recurse: yes
        state: directory
```

* 1.1.20 Ensure that the Kubernetes PKI certificate file permissions are set to 644 or more restrictive

The remediation playbook resolves this issue but kube-bench marks this as a manual test.

```
JUSTIFICATION for WARN (MANUAL)

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

The remediation playbook resolves this issue but kube-bench marks this as a manual test.

```
JUSTIFICATION for WARN (MANUAL)

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

The `anonymous-auth` is set to true to enable health-checks from load balancers. Research on this topic shows that communication between nodes and users is fully secured by Principle of Least Privilege. However, if the health check is not a concern then this finding can be remediated with the following task. I advise accepting the default so the health check works.

```
JUSTIFICATION of FAIL (MANUAL)

    # If a change is made, the apiserver is restarted.
    - name: 1.2.1 Ensure that the --anonymous-auth argument is set to false
      lineinfile:
        path: /etc/kubernetes/manifests/kube-apiserver.yaml
        regexp: '^    - --anonymous-auth=True'
        line: "    - --anonymous-auth=False"
```

* 1.2.2 Ensure that the --basic-auth-file argument is not set

```
PASS
```

* 1.2.3 Ensure that the --token-auth-file parameter is not set

```
PASS
```

* 1.2.4 Ensure that the --kubelet-https argument is set to true

```
PASS
```

* 1.2.5 Ensure that the --kubelet-client-certificate and --kubelet-client-key arguments are set as appropriate

```
PASS
```

* 1.2.6 Ensure that the --kubelet-certificate-authority argument is set as appropriate

```
PASS

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
PASS
```

* 1.2.8 Ensure that the --authorization-mode argument includes Node

```
PASS
```

* 1.2.9 Ensure that the --authorization-mode argument includes RBAC

```
PASS
```

* 1.2.10 Ensure that the admission control plugin EventRateLimit is set

This is an alpha feature. See https://github.com/kubernetes/kubernetes/issues/62861 for commentary.

```
JUSTIFICATION of WARN
```

* 1.2.11 Ensure that the admission control plugin AlwaysAdmit is not set

```
PASS
```

* 1.2.12 Ensure that the admission control plugin AlwaysPullImages is set

```
PASS

    - name: 1.2.12 Ensure that the admission control plugin AlwaysPullImages is set
      block:
        - name: 1.2.12 check AlwaysPullImages exists.
          command: grep enable-admission-plugins kube-apiserver.yaml | grep AlwaysPullImages
          register: always_pull_images_flag
          check_mode: no
          ignore_errors: yes
          changed_when: no

        - name: 1.2.12 add AlwaysPullImages
          replace:
            path: /etc/kubernetes/manifests/kube-apiserver.yaml
            regexp: '^(.*enable-admission-plugins=.*)$'
            replace: '\1,AlwaysPullImages'
          when: always_pull_images_flag.rc != 0
```

* 1.2.13 Ensure that the admission control plugin SecurityContextDeny is set if PodSecurityPolicy is not used

The `master.yaml` only tests that the `SecurityContextDeny` admission control plugin is not being used. We are using the `PodSecurityPolicy` admission controller. The task below verifies that SecurityContextDeny is not being used.

```
JUSTIFICATION for WARN

    - name: 1.2.13 Ensure that the admission control plugin SecurityContextDeny is set if PodSecurityPolicy is not used
      shell: grep enable-admission-plugins /etc/kubernetes/manifests/kube-apiserver.yaml | grep SecurityContextDeny
      register: security_content_deny_flag
      check_mode: no
      changed_when: no
      failed_when: security_content_deny_flag.rc == 0
```

* 1.2.14 Ensure that the admission control plugin ServiceAccount is set

```
PASS
```

* 1.2.15 Ensure that the admission control plugin NamespaceLifecycle is set

```
PASS
```

* 1.2.16 Ensure that the admission control plugin PodSecurityPolicy is set

When the **Pod Security Policy** option is followed when provisioning the cluster, this plugin is enabled. However, the `apiserver` runs as a K8S static pod.

The `ps` audit test used by kubebench can't find the option even though it is enabled.

```
JUSTIFICATION FOR FAIL
```

* 1.2.17 Ensure that the admission control plugin NodeRestriction is set

```
PASS
```

* 1.2.18 Ensure that the --insecure-bind-address argument is not set

```
PASS
```

* 1.2.19 Ensure that the --insecure-port argument is set to 0

```
PASS
```

* 1.2.20 Ensure that the --secure-port argument is not set to 0

```
PASS
```

* 1.2.21 Ensure that the --profiling argument is set to false

```
PASS
```

* 1.2.22 Ensure that the --audit-log-path argument is set

```
PASS - see 3.2.1.
```

* 1.2.23 Ensure that the --audit-log-maxage argument is set to 30 or as appropriate

```
PASS - see 3.2.1.
```

* 1.2.24 Ensure that the --audit-log-maxbackup argument is set to 10 or as appropriate

```
PASS - see 3.2.1.
```

* 1.2.25 Ensure that the --audit-log-maxsize argument is set to 100 or as appropriate

```
PASS - see 3.2.1.
```

* 1.2.26 Ensure that the --request-timeout argument is set as appropriate

```
PASS
```

* 1.2.27 Ensure that the --service-account-lookup argument is set to true

```
PASS
```

* 1.2.28 Ensure that the --service-account-key-file argument is set as appropriate

```
PASS
```

* 1.2.29 Ensure that the --etcd-certfile and --etcd-keyfile arguments are set as appropriate

```
PASS
```

* 1.2.30 Ensure that the --tls-cert-file and --tls-private-key-file arguments are set as appropriate

```
PASS
```

* 1.2.31 Ensure that the --client-ca-file argument is set as appropriate

```
PASS
```

* 1.2.32 Ensure that the --etcd-cafile argument is set as appropriate

```
PASS
```

* 1.2.33 Ensure that the --encryption-provider-config argument is set as appropriate

This check will pass as long as the Encryption At Rest steps are followed when the cluster is created. Those steps are shown at https://medined.github.io/kubernetes/kubespray/encryption/ansible/add-aws-encryption-provider-to-kubespray/.

```
PASS
```

* 1.2.34 Ensure that encryption providers are appropriately configured

Kube-bench has this as a manual check. Therefore it will always fail. However, Encryption at Rest is supported at shown by https://medined.github.io/kubernetes/kubespray/encryption/ansible/add-aws-encryption-provider-to-kubespray/. There is a manual proof process which can be automated if needed.

```
JUSTIFICATION of FAIL (MANUAL)
```

* 1.2.35 Ensure that the API Server only makes use of Strong Cryptographic Ciphers

Kube-bench is checking for a more permissive list of ciphers. Its check has no flexibility. However, the Ansible playbook will restrict the set as shown below. A security expert should verify the list of cipher suites is correct.

```
JUSTIFICATION of FAIL (MANUAL)

- name: 1.2.35 Ensure that the API Server only makes use of Strong Cryptographic Ciphers
  block:
    - name: 1.2.35 check flag
      command: grep "tls-cipher-suites=" /etc/kubernetes/manifests/kube-apiserver.yaml
      register: tls_cipher_suites_flag
      check_mode: no
      ignore_errors: yes
      changed_when: no

    # This is a long shell command but I don't know any way to shorten it.
    - name: 1.2.35 add flag
      shell: sed -i 's^- kube-apiserver$^- kube-apiserver\n    - --tls-cipher-suites=TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384^' /etc/kubernetes/manifests/kube-apiserver.yaml
      args:
        warn: false
      when: tls_cipher_suites_flag.rc != 0
      notify:
        Restart kubelet
```

* 1.3 Controller Manager
* 1.3.1 Ensure that the --terminated-pod-gc-threshold argument is set as appropriate

```
PASS
```
* 1.3.2 Ensure that the --profiling argument is set to false

```
PASS
```

* 1.3.3 Ensure that the --use-service-account-credentials argument is set to true

```
PASS
```

* 1.3.4 Ensure that the --service-account-private-key-file argument is set as appropriate

```
PASS
```

* 1.3.5 Ensure that the --root-ca-file argument is set as appropriate

```
PASS
```

* 1.3.6 Ensure that the RotateKubeletServerCertificate argument is set to true

```
PASS

---
- name: Ensure that the RotateKubeletServerCertificate argument is set to true
  become: yes
  become_user: root
  lineinfile:
    path: /etc/kubernetes/manifests/kube-controller-manager.yaml
    insertafter: "- kube-controller-manager$"
    line: "    - --feature-gates=RotateKubeletServerCertificate=true"
    firstmatch: yes
    state: present
```

* 1.3.7 Ensure that the --bind-address argument is set to 127.0.0.1

```
PASS

---
- name: Ensure that the --bind-address argument is set to 127.0.0.1
  become: yes
  become_user: root
  lineinfile:
    path: /etc/kubernetes/manifests/kube-controller-manager.yaml
    regexp: "--bind-address="
    line: "    - --bind-address=127.0.0.1"
    firstmatch: yes
    state: present
```

* 1.4 Scheduler
* 1.4.1 Ensure that the --profiling argument is set to false

```
PASS

---
- name: Ensure that the RotateKubeletServerCertificate argument is set to true
  become: yes
  become_user: root
  lineinfile:
    path: /etc/kubernetes/manifests/kube-scheduler.yaml
    insertafter: "- kube-scheduler$"
    line: "    - --profiling=false"
    firstmatch: yes
    state: present
```

* 1.4.2 Ensure that the --bind-address argument is set to 127.0.0.1

```
PASS

---
- name: Ensure that the --bind-address argument is set to 127.0.0.1
  become: yes
  become_user: root
  lineinfile:
    path: /etc/kubernetes/manifests/kube-scheduler.yaml
    regexp: "--bind-address="
    line: "    - --bind-address=127.0.0.1"
    firstmatch: yes
    state: present
```

### 2. Etcd

* 2 Etcd Node Configuration
* 2 Etcd Node Configuration Files
* 2.1 Ensure that the --cert-file and --key-file arguments are set as appropriate (Scored)

The standard KubeSpray installation provides HTTPS communication between K8S servers and the ETCD servers. This can be proven by looking at `/etc/kubernetes/manifests/kube-apiserver.yaml` on the controller node. In that file, you'll see the following parameters are set.

```
- --etcd-cafile=/etc/ssl/etcd/ssl/ca.pem
- --etcd-certfile=/etc/ssl/etcd/ssl/node-ip-10-245-207-223.ec2.internal.pem
- --etcd-keyfile=/etc/ssl/etcd/ssl/node-ip-10-245-207-223.ec2.internal-key.pem
- --etcd-servers=https://10.245.207.119:2379
```

The KubeBench audit looks for an `etcd` process running on the controller node. That process does not exist so the test will always fail.

```
JUSTIFICATION FOR FAIL
```

* 2.2 Ensure that the --client-cert-auth argument is set to true (Scored)

The standard KubeSpray installation provides HTTPS communication between K8S servers and the ETCD servers. In order to prove this check should pass, review the setting with the following code. The result will be `ETCD_CLIENT_CERT_AUTH=true`.

```
HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP cat /etc/etcd.env | grep ETCD_CLIENT_CERT_AUTH
```

The KubeBench audit looks for an `etcd` process running on the controller node. That process does not exist so the test will always fail.

```
JUSTIFICATION FOR FAIL
```

2.3 Ensure that the --auto-tls argument is not set to true (Scored)

```
PASS
```

* 2.4 Ensure that the --peer-cert-file and --peer-key-file arguments are set as appropriate (Scored)

The standard KubeSpray installation provides HTTPS communication between K8S servers and the ETCD servers. In order to prove this check should pass, review the setting with the following code. The result shows that the peer parameters are set.

```bash
HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP cat /etc/etcd.env | grep ETCD_PEER
```

```
JUSTIFICATION FOR FAIL
```

* 2.5 Ensure that the --peer-client-cert-auth argument is set to true (Scored)

The standard KubeSpray installation provides HTTPS communication between K8S servers and the ETCD servers. In order to prove this check should pass, review the setting with the following code. The result shows that the peer parameters are set.

```bash
HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP cat /etc/etcd.env | grep ETCD_PEER_CLIENT_AUTH
```

```
JUSTIFICATION FOR FAIL
```

* 2.6 Ensure that the --peer-auto-tls argument is not set to true (Scored)

```
PASS
```

* 2.7 Ensure that a unique Certificate Authority is used for etcd (Not Scored)

This check will always be WARN because it is a manual test.

KubeSpray runs `etcd` as a separate set of servers. The CA of each etcd server can be checked using the following technique. Run the equivalent for each server in the etcd cluster.

```bash
JUSTIFICATION for WARN (MANUAL)

HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP grep "ETCDCTL_CA_FILE" /etc/etcd.env
```

### 3. Controlplane

* 3 Control Plane Configuration
* 3.1 Authentication and Authorization
* 3.1.1 Client certificate authentication should not be used for users (Not Scored)

There are many ways to add username-based authentication to Kubernetes. One way is to use KeyCloak as the OpenID Connect server. This technique is too complex to show here but https://medined.github.io/centos/terraform/ansible/kubernetes/kubespray/provision-centos-kubernetes-cluster-on-aws/ documents the whole process.

Each user can be provided a separate account in KeyCloak with special RBAC for their particular permission needs.

```
JUSTIFICATION for WARN

Alternative mechanisms provided by Kubernetes such as the use of OIDC should be implemented in place of client certificates.
```

* 3.2 Logging
* 3.2.1 Ensure that a minimal audit policy is created (Scored)

This section shows how to set a policy file but does not make a recommendation
regarding a default policy. An example policy is used.

The Ansible task shown below allows 1.2.22, 1.2.23, 1.2.24, and 1.2.25 to pass.

The following list of audit log files provide anecdotal proof that
both audit features and log rotation is enabled.

```
# ls -lh
total 556M
-rw-r--r--. 1 root root 200M Jul 18 14:19 audit-2020-07-18T14-19-40.621.log
-rw-r--r--. 1 root root 150M Jul 18 14:47 audit-2020-07-18T14-47-28.622.log
-rw-r--r--. 1 root root 100M Jul 18 15:35 audit-2020-07-18T15-35-00.149.log
-rw-r--r--. 1 root root 100M Jul 18 16:22 audit-2020-07-18T16-22-39.856.log
-rw-r--r--. 1 root root 5.2M Jul 18 16:25 audit.log
```

```
JUSTIFICATION for WARN (MANUAL)

- name: 3.2.1 Ensure that a minimal audit policy is created
  block:

  - name: 3.2.1 - Create a directory hold the audit policy.
    file:
      path: /etc/kubernetes/policies
      state: directory

  - name: 3.2.1 - Create policy file.
    copy:
      dest: /etc/kubernetes/policies/audit-policy.yaml
      content: |
        apiVersion: audit.k8s.io/v1beta1
        kind: Policy
        rules:
          # Do not log from kube-system accounts
          - level: None
            userGroups:
            - system:serviceaccounts:kube-system
          - level: None
            users:
            - system:apiserver
            - system:kube-scheduler
            - system:volume-scheduler
            - system:kube-controller-manager
            - system:node

          # Do not log from collector
          - level: None
            users:
            - system:serviceaccount:collectorforkubernetes:collectorforkubernetes

          # Don't log nodes communications
          - level: None
            userGroups:
            - system:nodes

          # Don't log these read-only URLs.
          - level: None
            nonResourceURLs:
            - /healthz*
            - /version
            - /swagger*

          # Log configmap and secret changes in all namespaces at the metadata level.
          - level: Metadata
            resources:
            - resources: ["secrets", "configmaps"]

          # A catch-all rule to log all other requests at the request level.
          - level: Request

  #
  # There might be a lot of changes to the api-server manifest. Each
  # change would restart the apiserver pod. It might be better to
  # stop the kubelet and restart it when we are done.
  - name: Stop kubelet.
    systemd:
      state: stopped
      name: kubelet

  - name: 3.2.1 - Check audit policy flag exists.
    shell: grep audit-policy-file /etc/kubernetes/manifests/kube-apiserver.yaml | grep audit-policy-file
    register: audit_policy_file_flag
    check_mode: no
    ignore_errors: yes
    changed_when: no

  - name: 3.2.1 - Add audit parameters.
    replace:
      path: /etc/kubernetes/manifests/kube-apiserver.yaml
      regexp: '^(.*tls-private-key-file=.*)$'
      replace: '\1\n    - --audit-policy-file=/etc/kubernetes/policies/audit-policy.yaml\n    - --audit-log-path=/var/log/apiserver/audit.log\n    - --audit-log-maxbackup=10\n    - --audit-log-maxage=30\n    - --audit-log-maxsize=100\n    - --feature-gates=AdvancedAuditing=false'
    when: audit_policy_file_flag.rc != 0

  - name: 3.2.1 - Add volume mounts to end of list.
    replace:
      path: /etc/kubernetes/manifests/kube-apiserver.yaml
      regexp: '^  hostNetwork: true$'
      replace: '    - mountPath: /etc/kubernetes/policies\n      name: policies\n      readOnly: true\n    - mountPath: /var/log/apiserver\n      name: log\n  hostNetwork: true'
    when: audit_policy_file_flag.rc != 0

  - name: 3.2.1 - Add volumes to end of list.
    replace:
      path: /etc/kubernetes/manifests/kube-apiserver.yaml
      regexp: '^status: {}$'
      replace: '  - hostPath:\n      path: /etc/kubernetes/policies\n      type: DirectoryOrCreate\n    name: policies\n  - hostPath:\n      path: /var/log/apiserver\n      type: DirectoryOrCreate\n    name: log\nstatus: {}'
    when: audit_policy_file_flag.rc != 0

  - name: Start kubelet.
    systemd:
      state: started
      name: kubelet
```

* 3.2.2 Ensure that the audit policy covers key security concerns (Not Scored)

See section 3.2.1.

```
JUSTIFICATION for WARN (MANUAL)
```

### 4. Node

* 4 Worker Node Security Configuration
* 4.1 Worker Node Configuration Files
* 4.1.1 Ensure that the kubelet service file permissions are set to 644 or more restrictive (Scored)

```
PASS
```

* 4.1.2 Ensure that the kubelet service file ownership is set to root:root (Scored)

```
PASS
```

* 4.1.3 Ensure that the proxy kubeconfig file permissions are set to 644 or more restrictive (Scored)

KubeSpray does not store its kube-proxy configuration on the node file system. Instead it is passed into the pod using a ConfigMap. If your K8S account has the propoer permissions, you can see this ConfigMap using the following command:

```
kubectl -n kube-system describe configmap kube-proxy
```

```
JUSTIFICATION FOR FAIL
```

* 4.1.4 Ensure that the proxy kubeconfig file ownership is set to root:root (Scored)

KubeSpray does not store its kube-proxy configuration on the node file system. Instead it is passed into the pod using a ConfigMap. If your K8S account has the propoer permissions, you can see this ConfigMap using the following command:

```
kubectl -n kube-system describe configmap kube-proxy
```

```
JUSTIFICATION FOR FAIL
```

* 4.1.5 Ensure that the kubelet.conf file permissions are set to 644 or more restrictive (Scored)

```
PASS
```

* 4.1.6 Ensure that the kubelet.conf file ownership is set to root:root (Scored)

```
PASS
```

* 4.1.7 Ensure that the certificate authorities file permissions are set to 644 or more restrictive (Scored)

This KubeBench check has no test associated with it. However, the Ansible snippet below is ensure the correct permission is applied to the CA file on the worker nodes.

```
JUSTIFICATION FOR WARN

    - hosts: kube-node
      any_errors_fatal: "{{ any_errors_fatal | default(true) }}"
      become: yes
      gather_facts: False
      roles:
        - { role: kubespray-defaults }
      tasks:
        - name: Include kubespray-default variables
          include_vars: roles/kubespray-defaults/defaults/main.yaml
        - name: 4.1.7 Ensure that the certificate authorities file permissions are set to 644 or more restrictive
          file:
            path: /etc/kubernetes/ssl/ca.crt
            mode: "644"
```

* 4.1.8 Ensure that the client certificate authorities file ownership is set to root:root (Scored)

```
PASS
```

* 4.1.9 Ensure that the kubelet configuration file has permissions set to 644 or more restrictive (Scored)

```
PASS
```

* 4.1.10 Ensure that the kubelet configuration file ownership is set to root:root (Scored)

```
PASS
```

* 4.2 Kubelet
* 4.2.1 Ensure that the --anonymous-auth argument is set to false (Scored)

```
PASS
```

* 4.2.2 Ensure that the --authorization-mode argument is not set to AlwaysAllow (Scored)

```
PASS
```

* 4.2.3 Ensure that the --client-ca-file argument is set as appropriate (Scored)

```
PASS
```

* 4.2.4 Ensure that the --read-only-port argument is set to 0 (Scored)

Port 10255 is a read only port that kubernetes opens for gathering diagnostics, but can potentially be used for unauthenticated access to sensitive data and so the CIS. Manual inspection shows that although the CIS check would show as a fail because kubelet was not started with the `--read-only-port 0` argument, the kubelet config file at `/etc/kubernetes/kubelet-config.yaml` is configured to disable the read-only api with the `readOnlyPort: 0` configuration and so satisfies this requirement. A manual test to confirm that the configuration file setting is being applied is to run `netstat -tulpn` on the worker nodes and confirm that the server is not listening on port 10255.

```
PASS

---
- name: enable protect kernel defaults
  hosts:  kube-node
  tasks:
  - name: lineinfile for kernel defaults setting  
    lineinfile:
      path: /etc/kubernetes/kubelet-config.yaml
      regexp: '^readOnlyPort'
      line: 'readOnlyPort: 0'
```

* 4.2.5 Ensure that the --streaming-connection-idle-timeout argument is not set to 0 (Scored)

```
PASS
```

* 4.2.6 Ensure that the --protect-kernel-defaults argument is set to true (Scored)

```
PASS
  ---
  - name: enable protect kernel defaults
    hosts:  kube-node
    tasks:
    - name: lineinfile for kernel defaults setting  
      lineinfile:
        path: /etc/kubernetes/kubelet-config.yaml
        regexp: '^protect'
        line: 'protectKernelDefaults: true'
```

* 4.2.7 Ensure that the --make-iptables-util-chains argument is set to true (Scored)

```
PASS
```

* 4.2.8 Ensure that the --hostname-override argument is not set (Not Scored)

The CIS Benchmark says "Some cloud providers may require this flag to ensure that hostname matches names issued by the cloud provider. In these environments, this recommendation should not apply." Some research could be done to verify that argument is required for AWS.

However, by manual inspection we can see that the parameter argument is the same as the hostname. Therefore, the hostname is not being overridden, just set to its proper value.

The manual test is this: On a worker node, run `hostname`. This might return `ip-10-250-207-133.ec2.internal`. View the `kubelet` command using `ps -ef | grep kubelet`. You'll see that the `--hostname-override` parameter is the same of the hostname.

```
JUSTIFICATION for WARN
```

* 4.2.9 Ensure that the --event-qps argument is set to 0 or a level which ensures appropriate event capture (Not Scored)

```
JUSTIFICATION for WARN
```

`kublet`, in our cluster, is configured in `/etc/kubernetes/kubelet-config.yaml`. The `kube-bench` tool does not test this file. It tests for command-line parameters. The --event-gps argument can be set, before the cluster is created by making the following changes:

In `roles/kubernetes/node/templates/kubelet-config.v1beta1.yaml.j2`, add the following to the end of the file.

```
{% if kubelet_event_record_qps %}
eventRecordQPS: {{ kubelet_event_record_qps }}
{% endif %}
```

In `roles/kubespray-defaults/defaults/main.yaml`, add the following to the end of the file.

```
# Sets the eventRecordQPS parameter in kubelet-config.yaml. The default value is 5 (see types.go)
# Setting it to 0 allows unlimited requests per second.
kubelet_event_record_qps: 5
```

* 4.2.10 Ensure that the --tls-cert-file and --tls-private-key-file arguments are set as appropriate (Scored)

Using command-line parameters for `kubelet` has been deprecated in favor of using a configuration file. KubeSpray uses a configuration file so this test based on `ps` will never pass.

KubeSpray uses `/etc/kubernetes/kubelet-config.yaml` to configure `kubelet`. If the tlsCertFile and tlsPrivateKeyFile are not provided, a self-signed certificate and key are generated for the public address.

```
JUSTIFICATION FOR FAIL
```

* 4.2.11 Ensure that the --rotate-certificates argument is not set to false (Scored)

```
PASS
```

* 4.2.12 Ensure that the RotateKubeletServerCertificate argument is set to true (Scored)

Update `roles/kubespray-defaults/defaults/main.yaml` to have the following:

```
kube_feature_gates:
  - RotateKubeletServerCertificate=true

PASS
```

* 4.2.13 Ensure that the Kubelet only makes use of Strong Cryptographic Ciphers (Not Scored)

Update `roles/kubernetes/master/defaults/main/main.yml` to have the following:

```
tls_cipher_suites:
  - TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
  - TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
  - TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305
  - TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
  - TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305
  - TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
  - TLS_RSA_WITH_AES_256_GCM_SHA384
  - TLS_RSA_WITH_AES_128_GCM_SHA256

PASS
```

### 5. Policies

* 5 Kubernetes Policies
* 5.1 RBAC and Service Accounts
* 5.1.1 Ensure that the cluster-admin role is only used where required (Not Scored)

Identify all clusterrolebindings to the cluster-admin role. Check if they are used and if they need this role or if they could use a role with fewer privileges.

Where possible, first bind users to a lower privileged role and then remove the clusterrolebinding to the cluster-admin role :

  kubectl delete clusterrolebinding [name]

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.1.2 Minimize access to secrets (Not Scored)

Where possible, remove get, list and watch access to secret objects in the cluster.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.1.3 Minimize wildcard use in Roles and ClusterRoles (Not Scored)

Where possible replace any use of wildcards in clusterroles and roles with specific objects or actions.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.1.4 Minimize access to create pods (Not Scored)

Where possible, remove create access to pod objects in the cluster.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.1.5 Ensure that default service accounts are not actively used. (Scored)

Create explicit service accounts wherever a Kubernetes workload requires specific access to the Kubernetes API server.

Modify the configuration of each default service account to include this value automountServiceAccountToken: false

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.1.6 Ensure that Service Account Tokens are only mounted where necessary (Not Scored)

Modify the definition of pods and service accounts which do not need to mount service account tokens to disable it.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2 Pod Security Policies
* 5.2.1 Minimize the admission of privileged containers (Not Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.privileged field is omitted or set to false.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.2 Minimize the admission of containers wishing to share the host process ID namespace (Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.hostPID field is omitted or set to false.
```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.3 Minimize the admission of containers wishing to share the host IPC namespace (Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.hostIPC field is omitted or set to false.
```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.4 Minimize the admission of containers wishing to share the host network namespace (Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.hostNetwork field is omitted or set to false.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.5 Minimize the admission of containers with allowPrivilegeEscalation (Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.allowPrivilegeEscalation field is omitted or set to false.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.6 Minimize the admission of root containers (Not Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.runAsUser.rule is set to either MustRunAsNonRoot or MustRunAs with the range of UIDs not including 0.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.7 Minimize the admission of containers with the NET_RAW capability (Not Scored)

Create a PSP as described in the Kubernetes documentation, ensuring that the .spec.requiredDropCapabilities is set to include either NET_RAW or ALL.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.8 Minimize the admission of containers with added capabilities (Not Scored)

Ensure that allowedCapabilities is not present in PSPs for the cluster unless it is set to an empty array.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.2.9 Minimize the admission of containers with capabilities assigned (Not Scored)

Review the use of capabilites in applications runnning on your cluster. Where a namespace contains applicaions which do not require any Linux capabities to operate consider adding a PSP which forbids the admission of containers which do not drop all capabilities.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.3 Network Policies and CNI
* 5.3.1 Ensure that the CNI in use supports Network Policies (Not Scored)

Use network policies to restrict traffic in the cluster. This is a manual, not scored test.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.3.2 Ensure that all Namespaces have Network Policies defined (Scored)

Follow the documentation and create NetworkPolicy objects as you need them.

```
JUSTIFICATION for WARN
```

* 5.4 Secrets Management
* 5.4.1 Prefer using secrets as files over secrets as environment variables (Not Scored)

If possible, rewrite application code to read secrets from mounted secret files, rather than from environment variables.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.4.2 Consider external secret storage (Not Scored)

Refer to the secrets management options offered by your cloud provider or a third-party secrets management solution.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.5 Extensible Admission Control
* 5.5.1 Configure Image Provenance using ImagePolicyWebhook admission controller (Not Scored)

Image Provenance can be supported using https://trow.io/.

```
JUSTIFICATION for WARN
```

* 5.6 General Policies
* 5.6.1 Create administrative boundaries between resources using namespaces (Not Scored)

Follow the documentation and create namespaces for objects in your deployment as you need them.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.6.2 Ensure that the seccomp profile is set to docker/default in your pod definitions (Not Scored)

Seccomp is an alpha feature currently. By default, all alpha features are disabled.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.6.3 Apply Security Context to Your Pods and Containers (Not Scored)

Follow the Kubernetes documentation and apply security contexts to your pods. For a suggested list of security contexts, you may refer to the CIS Security Benchmark for Docker Containers.

```
JUSTIFICATION for WARN (MANUAL)
```

* 5.6.4 The default namespace should not be used (Scored)

Ensure that namespaces are created to allow for appropriate segregation of Kubernetes resources and that all new resources are created in a specific namespace.

This test is scored, but implemented manually. There should only be one service, `kubernetes` running in the `default` namespace. You can verify this using the following command:

```bash
kubectl -n default get all
NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
service/kubernetes   ClusterIP   10.233.0.1   <none>        443/TCP   3d16h
```

```
JUSTIFICATION for WARN (MANUAL)
```
