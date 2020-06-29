---
layout: post
title: Provision CentOS-based Kubernetes Cluster On AWS With KubeSpray
author: David Medinets
categories: centos terraform ansible kubernetes kubespray
year: 2020
theme: kubernetes
---

Let me start by saying that I will be covering no new ground. This post is only to disambiguate building a centos-based kubernetes cluster by showing the exact steps that I used.

Creating a cluster takes two steps:

* Provisioning the AWS infrastructure.
* Installing the Kubernetes software.

It should take less than 20 minutes to create a small cluster. I have just one controller node and one worker node. Kubespray also creates two basion nodes. I don't mind one bastion but I don't know why two would be helpful.

## Provisioning Infrastructure

* Connect to a base project directory. I use `/data/project` which is on a separate partition.

```bash
cd /data/project
```

* Download project

```bash
git clone https://github.com/kubernetes-sigs/kubespray.git
cd kubespray
```

* Change to the `aws` directory.

```bash
cd contrib/terraform/aws
```

* Fix old syntax. This is not strictly needed but I like to avoid warning messages when possible.
  * In `./modules/elb/variables.tf`, remove quotes around `type`.
  * In `./modules/vpc/variables.tf`, remove quotes around `type`.
  * In `./variables.tf`, remove quotes around `type`.

* In `./terraform.tfvars`, set vairables as needed. Note that the inventory file will be created a few levels up in the directory tree.

```
# Global Vars
aws_cluster_name = "flooper"

# VPC Vars
aws_vpc_cidr_block       = "10.250.192.0/18"
aws_cidr_subnets_private = [ "10.250.192.0/20", "10.250.208.0/20" ]
aws_cidr_subnets_public  = [ "10.250.224.0/20", "10.250.240.0/20" ]

# Bastion Host
aws_bastion_size = "t3.medium"

# Kubernetes Cluster
aws_kube_master_num  = 1
aws_kube_master_size = "t3.medium"

aws_etcd_num  = 1
aws_etcd_size = "t3.medium"

aws_kube_worker_num  = 1
aws_kube_worker_size = "t3.medium"

# Settings AWS ELB
aws_elb_api_port                = 6443
k8s_secure_api_port             = 6443
kube_insecure_apiserver_address = "0.0.0.0"

default_tags = {
  #  Env = "devtest"
  #  Product = "kubernetes"
}

inventory_file = "../../../inventory/hosts"
```

* In `./credentials.tfvars`, set your AWS credentials. Don't create a cluster unless you have access to a PEM file related to the `AWS_SSH_KEY_NAME` EC2 key pair.

```
AWS_ACCESS_KEY_ID = "111AXLYWH3DH2FGKSOFQ"
AWS_SECRET_ACCESS_KEY = "111dvxqDOX4RXJN7BQRZI/HD02WDW2SwV5Ck8R7F"
AWS_SSH_KEY_NAME = "keypair_name"
AWS_DEFAULT_REGION = "us-east-1"
```

* In `./variables.tf`, switch the AMI to use Centos.

```yaml
data "aws_ami" "distro" {
  owners      = ["679593333241"]
  most_recent = true

  filter {
      name   = "name"
      values = ["CentOS Linux 7 x86_64 HVM EBS *"]
  }

  filter {
      name   = "architecture"
      values = ["x86_64"]
  }

  filter {
      name   = "root-device-type"
      values = ["ebs"]
  }
}
```

* Intialize terraform.

```bash
terraform init
```

* Apply the terraform plan. This is create all of the AWS infrastructure that is needed; including creating a VPC.

```bash
terraform apply --var-file=credentials.tfvars --auto-approve
```

## Install kubernetes

* Connect to the `kubespray` directory.

```bash
cd /data/project/kubespray
```

* Export the location of the EC2 key pair PEM file.

```bash
export PKI_PRIVATE_PEM=KEYPAIR.pem
```

* Run the ansible playbook for CentOS.

```bash
ansible-playbook \
    -i ./inventory/hosts \
    ./cluster.yml \
    -e ansible_user=centos \
    -e bootstrap_os=centos \
    -b \
    --become-user=root \
    --flush-cache \
    -e ansible_ssh_private_key_file=$PKI_PRIVATE_PEM
```

* Setup `kubectl` so that is can connect to the new cluster. **THIS OVERWRITES YOUR KUBECTL CONFIG FILE!**

```bash
CONTROLLER_HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-master\]" -A 1 | tail -n 1)
CONTROLLER_IP=$(cat ./inventory/hosts | grep $CONTROLLER_HOST_NAME | grep ansible_host | cut -d'=' -f2)
LB_HOST=$(cat inventory/hosts | grep apiserver_loadbalancer_domain_name | cut -d'"' -f2)

cat <<EOF
CONTROLLER_HOST_NAME: $CONTROLLER_HOST_NAME
       CONTROLLER_IP: $CONTROLLER_IP
             LB_HOST: $LB_HOST
EOF

mkdir -p ~/.kube
ssh -F ssh-bastion.conf centos@$CONTROLLER_IP "sudo chmod 644 /etc/kubernetes/admin.conf"
scp -F ssh-bastion.conf centos@$CONTROLLER_IP:/etc/kubernetes/admin.conf ~/.kube/config
sed -i "s^server:.*^server: https://$LB_HOST:6443^" ~/.kube/config
kubectl get nodes
```

**The cluster creation is complete.**

# Destroy Cluster

```
cd contrib/terraform/aws
terraform destroy --var-file=credentials.tfvars --auto-approve
```

# Links

* https://www.youtube.com/watch?v=OEEr2EX8WYc
