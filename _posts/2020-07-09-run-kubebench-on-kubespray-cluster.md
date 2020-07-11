---
layout: post
title: yRun KubeBench on KubeSpray Cluster
author: David Medinets
categories: kubernetes kubebench kubespray
year: 2020
theme: kubernetes
---

{:.no_toc}
{:toc}

This article shows how to run the five kinds of tests that kube-bench provides.

1. Master
2. Etcd
3. Controlplane
4. Node
5. Policies

By default, only controler and node tests have scripts to run them. This article shows how to run those and the rest.

This article is a follow-up to https://medined.github.io/centos/terraform/ansible/kubernetes/kubespray/2020/06/28/provision-centos-kubernetes-cluster-on-aws.html which shows how to provision the cluster that is about to be tested.

## Setup

Make sure that you can run the following command to list the nodes in your cluster.

```bash
kubectl get nodes
```

## Download Project

```
cd /data/projects
git clone https://github.com/aquasecurity/kube-bench.git
cd kube-bench
```

## Run Tests

The following sections show how to run each kind of test and how to read the results.

### 1. Master

These tests are run on a controller node.

```bash
kubectl apply -f job-master.yaml
```

View the test results by looking at the pod's log.

```bash
POD_NAME=$(kubectl get pods -l job-name=kube-bench-master --output=jsonpath='{.items[].metadata.name}')
kubectl logs $POD_NAME
```

You can see the results from my initial controller test at https://gist.github.com/medined/455ebc08a88356beff6c7750794e971e.

### 2. Etcd

In my cluster, we have `etcd` not running inside kubernetes. It has its own cluster. However, we only need to test one server in the cluster. The following command chooses the first `etcd` server in the Ansible `inventory` file.

Run these commands from the `kubespray` directory.

```bash
ETCD_HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
ETCD_IP=$(cat ./inventory/hosts | grep $ETCD_HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$ETCD_IP \
  sudo docker run \
    --pid=host \
    --rm=true \
    aquasec/kube-bench:latest \
    --benchmark cis-1.5 run --targets etcd
```

You can redirect the SSH output to a file if needed.

### 3. Controlplane

Run a job for the controlplane tests.

```bash
kubectl apply -f - <<EOF
---
apiVersion: batch/v1
kind: Job
metadata:
  name: kube-bench-controlplane
spec:
  template:
    spec:
      hostPID: true
      nodeSelector:
        node-role.kubernetes.io/master: ""
      tolerations:
        - key: node-role.kubernetes.io/master
          operator: Exists
          effect: NoSchedule
      containers:
        - name: kube-bench
          image: aquasec/kube-bench:latest
          command: ["kube-bench", "--benchmark", "cis-1.5", "run", "--targets", "controlplane"]
          volumeMounts:
            - name: var-lib-etcd
              mountPath: /var/lib/etcd
              readOnly: true
            - name: etc-kubernetes
              mountPath: /etc/kubernetes
              readOnly: true
              # /usr/local/mount-from-host/bin is mounted to access kubectl / kubelet, for auto-detecting the Kubernetes version.
              # You can omit this mount if you specify --version as part of the command.
            - name: usr-bin
              mountPath: /usr/local/mount-from-host/bin
              readOnly: true
      restartPolicy: Never
      volumes:
        - name: var-lib-etcd
          hostPath:
            path: "/var/lib/etcd"
        - name: etc-kubernetes
          hostPath:
            path: "/etc/kubernetes"
        - name: usr-bin
          hostPath:
            path: "/usr/bin"
EOF
```

View the test results by looking at the pod's log.

```bash
POD_NAME=$(kubectl get pods -l job-name=kube-bench-controlplane --output=jsonpath='{.items[].metadata.name}')
kubectl logs $POD_NAME
```

### 4. Node

These tests are run on a worker node.

```bash
kubectl apply -f job-node.yaml
```

View the test results by looking at the pod's log.

```bash
POD_NAME=$(kubectl get pods -l job-name=kube-bench-node --output=jsonpath='{.items[].metadata.name}')
kubectl logs $POD_NAME
```

You can see the results from my initial worker test at https://gist.github.com/medined/909b9fd1cfad24578c814074553dc07a.

### 5. Policies

Run a job for the policy tests.

```bash
kubectl apply -f - <<EOF
---
apiVersion: batch/v1
kind: Job
metadata:
  name: kube-bench-policies
spec:
  template:
    spec:
      hostPID: true
      nodeSelector:
        node-role.kubernetes.io/master: ""
      tolerations:
        - key: node-role.kubernetes.io/master
          operator: Exists
          effect: NoSchedule
      containers:
        - name: kube-bench
          image: aquasec/kube-bench:latest
          command: ["kube-bench", "--benchmark", "cis-1.5", "run", "--targets", "policies"]
          volumeMounts:
            - name: var-lib-etcd
              mountPath: /var/lib/etcd
              readOnly: true
            - name: etc-kubernetes
              mountPath: /etc/kubernetes
              readOnly: true
              # /usr/local/mount-from-host/bin is mounted to access kubectl / kubelet, for auto-detecting the Kubernetes version.
              # You can omit this mount if you specify --version as part of the command.
            - name: usr-bin
              mountPath: /usr/local/mount-from-host/bin
              readOnly: true
      restartPolicy: Never
      volumes:
        - name: var-lib-etcd
          hostPath:
            path: "/var/lib/etcd"
        - name: etc-kubernetes
          hostPath:
            path: "/etc/kubernetes"
        - name: usr-bin
          hostPath:
            path: "/usr/bin"
EOF
```

View the test results by looking at the pod's log.

```bash
POD_NAME=$(kubectl get pods -l job-name=kube-bench-policies --output=jsonpath='{.items[].metadata.name}')
kubectl logs $POD_NAME
```

# SSH TO Controller

When you are trying to resolve compliance issues, you'll definitely need to SSH to your servers to review various files. Run these commands wherever you have `kubespray` installed.

```bash
cd /data/projects/kubespray

CONTROLLER_HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-master\]" -A 1 | tail -n 1)
CONTROLLER_IP=$(cat ./inventory/hosts | grep $CONTROLLER_HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$CONTROLLER_IP

WORKER_HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-worker\]" -A 1 | tail -n 1)
CONTROLLER_IP=$(cat ./inventory/hosts | grep $WORKER_HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$WORKER_IP

ETCD_HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
ETCD_IP=$(cat ./inventory/hosts | grep $ETCD_HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$ETCD_IP
```
