---
layout: post
title: How To Add AWS Encryption Provider To KubeSpray-based Kubernetes Cluster
author: David Medinets
categories: kubernetes kubespray encryption ansible
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

## Antecedents

https://medined.github.io/centos/terraform/ansible/kubernetes/kubespray/provision-centos-kubernetes-cluster-on-aws/ shows the entire process of provisioning a Kubernetes cluster.

## Overview

This article shows show I added the AWS Encryption Provider (https://github.com/kubernetes-sigs/aws-encryption-provider) to my KubeSpray-based Kubernetes cluster.

This change is to support the CIS Benchmark `1.2.34 Ensure that encryption providers are appropriately configured` element.

NOTE: I did try to use the `kube_encrypt_secret_data` option that KubeSpray references in its source code but it did not support the AWS Encryption Provider.

## The Process

* If you want to follow these steps yourself, start just before running `terraform apply` to build the infrastructure.

* Create a Docker image of the `aws-encryption-provider`. I was not able to find an official version of this image so I created my own copy.

```bash
export IMAGE_NAME=medined/aws-encryption-provider

git clone https://github.com/kubernetes-sigs/aws-encryption-provider.git
cd aws-encryption-provider
docker build -t $IMAGE_NAME .
docker login
docker push $IMAGE_NAME
```

* Create a KMS key. Remember the key id using a file. I don't know a better way of tracking this information. I read that using an alias causes a problem. The tags are not displayed on the console. You can source this file to have the environment variables handy. I added AWS_DEFAULT_REGION and other values so they are handy later.

```
KEY_ID=$(aws kms create-key --tags TagKey=purpose,TagValue=k8s-encryption --query KeyMetadata.KeyId --output text)

export KEY_ARN=$(aws kms describe-key --key-id $KEY_ID --query KeyMetadata.Arn --output text)
export CLUSTER_NAME="flooper"

cat <<EOF > $HOME/$CLUSTER_NAME-encryption-provider-kms-key.env
AWS_DEFAULT_REGION=us-east-1
CLUSTER_NAME=$CLUSTER_NAME
IMAGE_NAME=$IMAGE_NAME
KEY_ID=$KEY_ID
KEY_ARN=$KEY_ARN
EOF
```

* Connect to the `kubespray` directory.

* Update `contrib/terraform/aws/modules/iam/main.tf` after making a copy. I think that only "kms:ListKeys", "kms:TagResource", "kms:Encrypt", "kms:DescribeKey", and "kms:CreateKey" are needed but just in case I allow all actions. Add the following to the file.

```
,{
  "Effect": "Allow",
  "Action": "kms:*",
  "Resource": ["*"]
}
```

* Run `terraform apply` as needed to build your infrastructure. Make sure to modify the command below to fit your situation.

```
cd contrib/terraform/aws
rm ../../../inventory/hosts ../../../ssh-bastion.conf
time terraform apply --var-file=credentials.tfvars --auto-approve
```

* Run the Ansible playbook. I send the output to a log file in the /tmp directory. You can find that file easily using `ls -ltr /tmp/kubespray*.log` after the playbook is complete.

```bash
ansible-playbook \
    -vvvvv \
    -i ./inventory/hosts \
    ./cluster.yml \
    -e ansible_user=centos \
    -e bootstrap_os=centos \
    --become \
    --become-user=root \
    --flush-cache \
    -e ansible_ssh_private_key_file=$PKI_PRIVATE_PEM \
    | tee /tmp/kubespray-cluster-$(date "+%Y-%m-%d_%H:%M").log
```

* Get ready to SSH into the controller. If you have more than one controller, these steps need to be repeated for each one. I have not tried this process with multiple controllers so I don't know what happens when one controller is updated and another is not. The SSH process makes it connection through a proxy. And you'll need to accept the remote server's fingerprint manually upon the first SSH.

```bash
CONTROLLER_HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-master\]" -A 1 | tail -n 1)
CONTROLLER_IP=$(cat ./inventory/hosts | grep $CONTROLLER_HOST_NAME | grep ansible_host | cut -d'=' -f2)
LB_HOST=$(cat inventory/hosts | grep apiserver_loadbalancer_domain_name | cut -d'"' -f2)

mkdir -p ~/.kube
ssh -F ssh-bastion.conf centos@$CONTROLLER_IP "sudo chmod 644 /etc/kubernetes/admin.conf"
scp -F ssh-bastion.conf centos@$CONTROLLER_IP:/etc/kubernetes/admin.conf ~/.kube/config
sed -i "s^server:.*^server: https://$LB_HOST:6443^" ~/.kube/config
kubectl get nodes
```

* Once you can list the nodes, SSH to the controller.

```bash
ssh -F ssh-bastion.conf centos@$CONTROLLER_IP
```

* Switch to super-user.

```bash
sudo su -
```

* Change to the kubernetes directory.

```bash
cd /etc/kubernetes
```

* Create the `secrets_encryption.yaml` file.

```bash
cat <<EOF > ssl/secrets_encryption.yaml
apiVersion: apiserver.config.k8s.io/v1
kind: EncryptionConfiguration
resources:
  - resources:
      - secrets
    providers:
    - kms:
        name: aws-encryption-provider
        endpoint: unix:///var/run/kmsplugin/socket.sock
        cachesize: 1000
        timeout: 3s
    - identity: {}
EOF
```

* Create the `aws-encryption-provider` static pod. A few seconds after the file is created, you'll be able to run `docker ps` in order to see the container running. Using `docker logs` will show you the logs and, hopefully, reassure you that it is running correctly.

**NOTE: You'll need to fill in the <<VARIABLE>> places. These are the IMAGE_NAME, KEY_ARN, and AWS_REGION**

```
cat <<EOF > manifests/aws-encryption-provider.yaml
apiVersion: v1
kind: Pod
metadata:
  labels:
    component: encryption-provider
    tier: control-plane
  name: aws-encryption-provider
  namespace: kube-system
spec:
  containers:
  - image: <<IMAGE_NAME>>
    name: aws-encryption-provider
    command:
    - /aws-encryption-provider
    - --key=<<KEY-ARN>>
    - --region=<<AWS_REGION>>
    - --listen=/var/run/kmsplugin/socket.sock
    ports:
    - containerPort: 8080
      protocol: TCP
    livenessProbe:
      httpGet:
        path: /healthz
        port: 8080
    volumeMounts:
    - mountPath: /var/run/kmsplugin
      name: var-run-kmsplugin
  volumes:
  - name: var-run-kmsplugin
    hostPath:
      path: /var/run/kmsplugin
      type: DirectoryOrCreate
EOF
```

* Perform a sanity check. Make sure that `kubectl -n kube-system get pods` still works.

* Now comes the last step. Update `/etc/kubernetes/manifests/kube-apiserver.yaml` with the following. You'll need to make changes to three parts of the file. After saving, the apiserver container will restart. In about 20 seconds, view the logs.

```
    - --encryption-provider-config=/etc/kubernetes/ssl/secrets_encryption.yaml

    volumeMounts:
    - mountPath: /var/run/kmsplugin
      name: var-run-kmsplugin

  volumes:
  - name: var-run-kmsplugin
    hostPath:
      path: /var/run/kmsplugin
      type: DirectoryOrCreate
```

* Perform another sanity check. Make sure that `kubectl -n kube-system get pods` still works.

## Proof

* Create a secret

```bash
kubectl create secret generic secret1 -n default --from-literal=mykey=mydata
```

* Connect to the kubespray directory.

```bash
cd /data/projects/kubespray
```

* SSH to the etcd server.

```bash
HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP
```

* Run the following command. You'll see something that look garbled but towards the beginning there should be "cryption-provider".

```bash
sudo ETCDCTL_API=3 \
  ETCDCTL_ENDPOINTS=https://`hostname`:2379 \
  ETCDCTL_CERT=/etc/ssl/etcd/ssl/admin-`hostname`.pem \
  ETCDCTL_KEY=/etc/ssl/etcd/ssl/admin-`hostname`-key.pem \
  ETCDCTL_CACERT=/etc/ssl/etcd/ssl/ca.pem \
  /usr/local/bin/etcdctl get /registry/secrets/default/secret1
```

## References

* https://kubernetes.io/docs/tasks/administer-cluster/encrypt-data/
* https://kubernetes.io/docs/tasks/administer-cluster/kms-provider/
* https://github.com/kubernetes-sigs/aws-encryption-provider
