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

## Acknowledgements

This work is being done at the request of the Enterprise Container Working Group (ECWG) of the Office of Information and Technology (OIT - https://www.oit.va.gov/) at the Department of Veteran Affairs.

## Provisioning Infrastructure

* Let's define some values ahead of need. The cluster name allows you to have multiple clusters in the same AWS account. Each will have its down VPC. And you'll need to have a different kubespray install for each cluster. I haven't tried to support multiple clusters.

```bash
export CLUSTER_NAME="flooper"
export IMAGE_NAME=medined/aws-encryption-provider
```

* **Encryption at Rest** - If you need this, then create a Docker image of the `aws-encryption-provider`. I was not able to find an official version of this image so I created my own copy.

```bash
git clone https://github.com/kubernetes-sigs/aws-encryption-provider.git
cd aws-encryption-provider
docker build -t $IMAGE_NAME .
docker login
docker push $IMAGE_NAME
```

* **Encryption at Rest** - If you need this, then create a KMS key. Remember the key id using a file. I don't know a better way of tracking this information. I read that using an alias causes a problem. The tags are not displayed on the console. You can source this file to have the environment variables handy. I added AWS_DEFAULT_REGION and other values so you can find them later.

```
KEY_ID=$(aws kms create-key --tags TagKey=purpose,TagValue=k8s-encryption --query KeyMetadata.KeyId --output text)

export KEY_ARN=$(aws kms describe-key --key-id $KEY_ID --query KeyMetadata.Arn --output text)

cat <<EOF > $HOME/$CLUSTER_NAME-encryption-provider-kms-key.env
AWS_DEFAULT_REGION=us-east-1
CLUSTER_NAME=$CLUSTER_NAME
IMAGE_NAME=$IMAGE_NAME
KEY_ID=$KEY_ID
KEY_ARN=$KEY_ARN
EOF
```

* Connect to a base project directory. I use `/data/project` which is on a separate partition.

```bash
cd /data/project
```

* Download kubespray. These command will remove all python packages before installing the ones needed for kubespray. This is done to prevent incompatibilites.

```bash
git clone https://github.com/kubernetes-sigs/kubespray.git
cd kubespray
pip freeze | xargs pip uninstall -y
pip install -r requirements.txt
```

* **Encryption at Rest** - If you need this, then update `contrib/terraform/aws/modules/iam/main.tf` after making a copy. I think that only "kms:ListKeys", "kms:TagResource", "kms:Encrypt", "kms:DescribeKey", and "kms:CreateKey" are needed but just in case I allow all actions. Add the following to the file.

```
,{
  "Effect": "Allow",
  "Action": "kms:*",
  "Resource": ["*"]
}
```

* Change to the `aws` directory.

```bash
cd contrib/terraform/aws
```

* Fix old syntax. This is not strictly needed but I like to avoid warning messages when possible.
  * In `./modules/elb/variables.tf`, remove quotes around `type`.
  * In `./modules/vpc/variables.tf`, remove quotes around `type`.
  * In `./variables.tf`, remove quotes around `type`.

* In `./terraform.tfvars`, set variables as needed. Note that the inventory file will be created a few levels up in the directory tree.

```
cat <<EOF > terraform.tfvars
# Global Vars
aws_cluster_name = "$CLUSTER_NAME"

# VPC Vars
aws_vpc_cidr_block       = "10.250.192.0/18"
aws_cidr_subnets_private = ["10.250.192.0/20", "10.250.208.0/20"]
aws_cidr_subnets_public  = ["10.250.224.0/20", "10.250.240.0/20"]

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
EOF
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

* Apply the terraform plan. This is create all of the AWS infrastructure that is needed; including creating a VPC. Note that I remove two files that might have been created by a previous apply.

```bash
rm ../../../inventory/hosts ../../../ssh-bastion.conf
terraform apply --var-file=credentials.tfvars --auto-approve
```

## Install kubernetes

* Connect to the `kubespray` directory.

```bash
cd /data/projects/kubespray
```

* Export the location of the EC2 key pair PEM file.

```bash
export PKI_PRIVATE_PEM=KEYPAIR.pem
```

* Run the ansible playbook for CentOS.

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

NOTE: In `/tmp`, you'll see Ansible Fact files named after the hostname. For example, `/tmp/ip-10-250-192-82.ec2.internal`.

NOTE: Setting `-e cloud_provider=aws` does not work.
-e podsecuritypolicy_enabled=true \
-e kube_apiserver_enable_admission_plugins=AlwaysPullImages \

NOTE: If you see a failuer with the message `target uses selinux but python bindings (libselinux-python) aren't installed.`, then install the `selinux` python page on the computer running Ansible. The command should be something like `python2 -m pip install selinux`. I also run the command for `python3`.

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

# Get the controller's SSH fingerprint.
ssh-keygen -R $CONTROLLER_IP > /dev/null 2>&1
ssh-keyscan -H $CONTROLLER_IP >> ~/.ssh/known_hosts 2>/dev/null

mkdir -p ~/.kube
ssh -F ssh-bastion.conf centos@$CONTROLLER_IP "sudo chmod 644 /etc/kubernetes/admin.conf"
scp -F ssh-bastion.conf centos@$CONTROLLER_IP:/etc/kubernetes/admin.conf ~/.kube/config
sed -i "s^server:.*^server: https://$LB_HOST:6443^" ~/.kube/config
kubectl get nodes
```

* Create script allowing SSH to controller, worker, and etcd servers.

```bash
cat <<EOF > ssh-to-controller.sh
HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-master\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP
EOF

cat <<EOF > ssh-to-worker.sh
HOST_NAME=$(cat ./inventory/hosts | grep "\[kube-node\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP
EOF

cat <<EOF > ssh-to-etcd.sh
HOST_NAME=$(cat ./inventory/hosts | grep "\[etcd\]" -A 1 | tail -n 1)
IP=$(cat ./inventory/hosts | grep $HOST_NAME | grep ansible_host | cut -d'=' -f2)
ssh -F ssh-bastion.conf centos@$IP
EOF
```

* **Encryption at Rest** - If you need this, visit https://medined.github.io/kubernetes/kubespray/encryption/ansible/add-aws-encryption-provider-to-kubespray/ to complete the cluster creation process.

**The cluster creation is complete.**

# Destroy Cluster

```
cd contrib/terraform/aws
terraform destroy --var-file=credentials.tfvars --auto-approve
```

# Links

* https://www.youtube.com/watch?v=OEEr2EX8WYc
