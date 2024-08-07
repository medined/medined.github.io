---
layout: post
title: Provision CentOS-based Kubernetes Cluster On AWS With KubeSpray
author: David Medinets
categories: centos terraform ansible kubernetes kubespray
year: 2020
theme: kubernetes
---

# Table of Contents
{:.no_toc}
* unordered list
{:toc}

* * *

**NOTE: The steps outlined below are outdated. However, they might still be useful when learning about KubeSpray. **

Let me start by saying that I will be covering no new ground. This post is only to disambiguate building a centos-based kubernetes cluster by showing the exact steps that I used.

Creating a cluster takes two steps:

* Provisioning the AWS infrastructure.
* Installing the Kubernetes software.

It should take less than 20 minutes to create a small cluster. I have just one controller node and one worker node. Kubespray also creates two basion nodes. I don't mind one bastion but I don't know why two would be helpful.

Watch for configuration options:

* **Auditing**
* **Encryption At Rest**
* **Kubebench**
* **Pod Security Policy**

## Acknowledgements

This work is being done at the request of the Enterprise Container Working Group (ECWG) of the Office of Information and Technology (OIT - https://www.oit.va.gov/) at the Department of Veteran Affairs.

## Provisioning Infrastructure

* Install Helm. Eventually you'll want to use this tool so let's get it installed now.

```
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
helm repo add stable https://kubernetes-charts.storage.googleapis.com
helm repo update
helm search repo stable
```

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

* **Auditing** - In order to turn auditing on, edit the `roles/kubernetes/master/defaults/main/main.yml` file so the values in the file match those below:

```
kubernetes_audit: true
audit_log_maxbackups: 10
```

* In order to have an always pull policy for the pods created by KubeSpray, edit `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below:

```
k8s_image_pull_policy: Always
```

* I prefer to use Octant (https://octant.dev/) or Lens (https://k8slens.dev/) instead of the Kubernetes Dashboard. Therefore, I don't enable it. Edit `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below:

```
dashboard_enabled: false
```

* Turn on the metrics servers by editting `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below. In the cluster, the "kubectl top nodes" command will be supported.

```
metrics_server_enabled: true
```

* Have KubeSpray make a copy of kubeconfig in {{ inventory_dir }}/artifacts. This means you don't need to `scp` the generated `admin.conf` file from the controller yourself. Edit `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below. After the playbook is complete, copy `inventory/sample/artifacts/admin.conf` to `~/.kube/config`. Remember that this will overwrite any existing kubeconfig file so be careful with this copy!

```
kubeconfig_localhost: true
```

* Let KubeSpray install `helm` by `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below.

```
helm_enabled: true
```

* Let KubeSpray install `registry` by `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below.

```
registry_enabled: true
```

* Let KubeSpray install `cert-manager` by `roles/kubespray-defaults/defaults/main.yaml` so the values in the file match those below.

```
cert_manager_enabled: true
```

* Change to the `aws` directory.

```bash
cd contrib/terraform/aws
```

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
time terraform apply --var-file=credentials.tfvars --auto-approve
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
time ansible-playbook \
  -vvvvv \
  -i ./inventory/hosts \
  ./cluster.yml \
  -e ansible_user=centos \
  -e cloud_provider=aws \
  -e bootstrap_os=centos \
  --become \
  --become-user=root \
  --flush-cache \
  -e ansible_ssh_private_key_file=$PKI_PRIVATE_PEM \
  | tee /tmp/kubespray-cluster-$(date "+%Y-%m-%d_%H:%M").log
```

NOTE: In `/tmp`, you'll see Ansible Fact files named after the hostname. For example, `/tmp/ip-10-250-192-82.ec2.internal`.

NOTE: -e podsecuritypolicy_enabled=true -e kube_apiserver_enable_admission_plugins=AlwaysPullImages - I tried these parameter options but they did not work for me. I hopefully I will discover that I've done something wrong and can use them in the future. In the meantime, this entry has an alternative to enable Pod Security Policy.

NOTE: If you see a failure with the message `target uses selinux but python bindings (libselinux-python) aren't installed.`, then install the `selinux` python page on the computer running Ansible. The command should be something like `python2 -m pip install selinux`. I also run the command for `python3`.

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

* **Pod Security Policy** - If you need this, run the following command which provides a basic set of policies. Learning about pod security policies is a big topic. We won't cover it here other than to say that before enabling the PodSecurityPolicy admission controller, pod security policies need to be in place so that pods in the kube-system namespace can start. That's what the following command does, it provides a bare minimum set of policies needed to start the apiserver pod. The restricted clusterrole as *zero* rules. If you want normal users to perform commands, you'll need to explicitly create rules. Here is summary of the `restricted` PSP.
  * Enable read-only root filesystem
  * Enable security profiles
  * Prevent host network access
  * Prevent privileged mode
  * Prevent root privileges
  * Whitelist read-only host path
  * Whitelist volume types

Each time a Gist is changed, the URL for it changes as well. It's important to get the latest version. Visit https://gist.github.com/medined/73cfb72c240a413eaf499392fe4026cf, then click on 'Raw'. Make sure the URL is the same as the one shown below.

```bash
kubectl apply -f https://gist.githubusercontent.com/medined/73cfb72c240a413eaf499392fe4026cf/raw/a24a6c9da7d1b19195a0b0ac777e9032a3bc8ec3/rbac-for-pod-security-policies.yaml
```

This command creates these resources:

```
podsecuritypolicy.policy/privileged unchanged
podsecuritypolicy.policy/restricted configured
clusterrole.rbac.authorization.k8s.io/psp:privileged unchanged
clusterrole.rbac.authorization.k8s.io/psp:restricted unchanged
clusterrolebinding.rbac.authorization.k8s.io/default:restricted unchanged
rolebinding.rbac.authorization.k8s.io/default:privileged unchanged
```

* **Pod Security Policy** - If you need this, find the public IP of the controller node, then SSH to it. `sudo` to be the `root` user. Now edit `/etc/kubernetes/manifests/kube-apiserver.yaml` by adding `PodSecurityPolicy` to the admission plugin list. As soon as you save the file, the `apiserver` pod will be restarted. This will cause connection errors because the api server stops responding. This is normal. Wait a few minutes and the pod will restart and start responds to requests. Check the command using `octant` or another technique. If you don't see the admission controllers in the command, resave the file to restart the pod.

```
--enable-admission-plugins=NodeRestriction,PodSecurityPolicy
```

* **Encryption at Rest** - If you need this, visit https://medined.github.io/kubernetes/kubespray/encryption/ansible/add-aws-encryption-provider-to-kubespray/ to complete the cluster creation process.

**The cluster creation is complete.**

## Ingress Controller

See https://docs.nginx.com/nginx-ingress-controller/overview/ for more information.

By default, pods of Kubernetes services are not accessible from the external network, but only by other pods within the Kubernetes cluster. Kubernetes has a built‑in configuration for HTTP load balancing, called Ingress, that defines rules for external connectivity to Kubernetes services. Users who need to provide external access to their Kubernetes services create an Ingress resource that defines rules, including the URI path, backing service name, and other information. **The Ingress controller can then automatically program a front‑end load balancer to enable Ingress configuration.** The NGINX Ingress Controller for Kubernetes is what enables Kubernetes to configure NGINX and NGINX Plus for load balancing Kubernetes services.

* Download the official manifest file.

```bash
curl -o ingress-nginx-controller-0.34.1.yaml https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.34.1/deploy/static/provider/aws/deploy.yaml
```

* **Pod Security Policy** - If you need this, add the following to all ClusterRole and Role resources in the downloaded yaml file. This change lets the service accounts use the `privileged` pod security policy.

```
  - apiGroups:      [policy]
    resources:      [podsecuritypolicies]
    resourceNames:  [privileged]
    verbs:          [use]
```

* **Pod Security Policy** - If you need this, add the following to the end of the file in the downloaded yaml file. **I don't recall why this was needed.**

```
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  labels:
    helm.sh/chart: ingress-nginx-2.11.1
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.34.1
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx
  namespace: ingress-nginx
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: ingress-nginx
subjects:
  - kind: ServiceAccount
    name: ingress-nginx
    namespace: default
```

* Create the resources.

```bash
kubectl apply -f ingress-nginx-controller-0.34.1.yaml
```

This command creates these resources:

```
namespace/ingress-nginx created
serviceaccount/ingress-nginx created
configmap/ingress-nginx-controller created
clusterrole.rbac.authorization.k8s.io/ingress-nginx created
clusterrolebinding.rbac.authorization.k8s.io/ingress-nginx created
role.rbac.authorization.k8s.io/ingress-nginx created
rolebinding.rbac.authorization.k8s.io/ingress-nginx created
service/ingress-nginx-controller-admission created
service/ingress-nginx-controller created
deployment.apps/ingress-nginx-controller created
validatingwebhookconfiguration.admissionregistration.k8s.io/ingress-nginx-admission created
clusterrole.rbac.authorization.k8s.io/ingress-nginx-admission created
clusterrolebinding.rbac.authorization.k8s.io/ingress-nginx-admission created
job.batch/ingress-nginx-admission-create created
job.batch/ingress-nginx-admission-patch created
role.rbac.authorization.k8s.io/ingress-nginx-admission created
rolebinding.rbac.authorization.k8s.io/ingress-nginx-admission created
serviceaccount/ingress-nginx-admission created
```

* Verify installation

```bash
kubectl get pods -n ingress-nginx \
  -l app.kubernetes.io/name=ingress-nginx --watch
```

## Deploy HTTP Application

In order to make this section work, you'll need to mess around with DNS.

* Find your Ingress Nginx Controller load balancer domain. The answer will look something like `aaXXXXf67c55949d8b622XXXX862dce0-bce30cd38eXXXX95.elb.us-east-1.amazonaws.com`.

```bash
kubectl -n ingress-nginx get service ingress-nginx-controller
```

NOTE: If the `EXTERNAL-IP` stays in the `pending` state, verify that you set `-e cloud_provider=aws` when the cluster was created.

* Create a vanity domain for the service being created in this section. This domain needs to point to the load balancer found in the previous step. I use Route 53 but you can use any DNS service. Please make sure that your can correctly resolve the domain using `dig`.

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
  replicas: 1
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

* Check the service is running. You should see the `text-responder` service in the list. The external IP should be `<none>`.

```bash
kubectl --namespace text-responder get service
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

```bash
curl http://$TEXT_RESPONDER_HOST
```

## Delete Certificate Manager

```
kubectl delete namespace cert-manager

kubectl delete crd certificaterequests.cert-manager.io certificates.cert-manager.io  challenges.acme.cert-manager.io clusterissuers.cert-manager.io issuers.cert-manager.io orders.acme.cert-manager.io

kubectl delete clusterrole cert-manager-cainjector cert-manager-controller-certificates cert-manager-controller-challenges cert-manager-controller-clusterissuers cert-manager-controller-ingress-shim cert-manager-controller-issuers cert-manager-controller-orders cert-manager-edit cert-manager-role cert-manager-view

kubectl delete clusterrolebinding cert-manager-cainjector cert-manager-controller-certificates cert-manager-controller-challenges cert-manager-controller-clusterissuers cert-manager-controller-ingress-shim cert-manager-controller-issuers cert-manager-controller-orders

kubectl -n kube-system delete role cert-manager-cainjector:leaderelection cert-manager:leaderelection

kubectl -n kube-system delete rolebinding cert-manager-cainjector:leaderelection cert-manager:leaderelection

kubectl delete MutatingWebhookConfiguration cert-manager-webhook

kubectl delete ValidatingWebhookConfiguration cert-manager-webhook
```

## Deploy Certificate Manager

* Install certificate manager. Check the chart versions at https://hub.helm.sh/charts/jetstack/cert-manager to find the latest version number.

* Apply the custom resource definitions.

```bash
kubectl apply --validate=false -f https://github.com/jetstack/cert-manager/releases/download/v1.0.0-beta.0/cert-manager.crds.yaml
```

* In this installation process, do not use `global.podSecurityPolicy.enabled=true` because it will set `apparmor` annotations on three pod security polices which get created. The nodes do not support AppArmor. This will result in blocked pods.

* Add the `jetstack` repository.

```bash
helm repo add jetstack https://charts.jetstack.io
```

* Get a local copy of the manifest needed for `cert-manager`.

```bash
helm template cert-manager jetstack/cert-manager --namespace cert-manager > cert-manager.yaml
```

* **For PodSecurityPolicy** If you need this, insert the following at the top of the `cert-manager.yaml` file.

```
---
apiVersion: v1
kind: Namespace
metadata:
  name: cert-manager
  labels:
    name: cert-manager
```

* **Pod Security Policy** - If you need this, the `cert-manager` roles have no permission to use Pod Security Policies. Add the following to every role and ClusterRole in the `cert-manager.yaml` file. This is definitly overkill, but I don't have time to experiment to get more granular.

```
  - apiGroups:      [policy]
    resources:      [podsecuritypolicies]
    resourceNames:  [restricted]
    verbs:          [use]
```

* Apply the `cert-manager` manifest.

```
kubectl apply -f cert-manager.yaml
```

* Check that the pods started.

```bash
kubectl get pods --namespace cert-manager
```

* Create an issuer to test the webhook works okay.

```bash
kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: cert-manager-test
---
apiVersion: cert-manager.io/v1alpha2
kind: Issuer
metadata:
  name: test-selfsigned
  namespace: cert-manager-test
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1alpha2
kind: Certificate
metadata:
  name: selfsigned-cert
  namespace: cert-manager-test
spec:
  dnsNames:
    - example.com
  secretName: selfsigned-cert-tls
  issuerRef:
    name: test-selfsigned
EOF
```

* Check the new certificate. You should see "Certificate issued successfully".

```bash
kubectl --namespace cert-manager-test describe certificate
```

* Cleanup the test resources.

```bash
kubectl delete namespace cert-manager-test
```

* Create Let's Encrypt ClusterIssuer for staging and production environments. The main difference is the ACME server URL. I use the term `staging` because that is what Let's Encrypt uses.

>Change the email address.

```bash
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1alpha2
kind: ClusterIssuer
metadata:
  name: letsencrypt-staging
spec:
  acme:
    email: david.medinets@gmail.com
    server: https://acme-staging-v02.api.letsencrypt.org/directory
    privateKeySecretRef:
      name: letsencrypt-staging-secret
    solvers:
    - http01:
        ingress:
          class: nginx
---
apiVersion: cert-manager.io/v1alpha2
kind: ClusterIssuer
metadata:
  name: letsencrypt-production
spec:
  acme:
    email: david.medinets@gmail.com
    server: https://acme-v02.api.letsencrypt.org/directory
    privateKeySecretRef:
      name: letsencrypt-production-secret
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

* Check on the status of the development issuer. The entries should be ready.

```bash
kubectl get clusterissuer
```

* Add annotation to text-responder ingress. This uses the staging Let's Encrypt to avoid being rate limited while testing.

```bash
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: text-responder-ingress
  namespace: text-responder
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-staging
spec:
  tls:
  - hosts:
    - text-responder.david.va-oit.cloud
    secretName: text-responder-tls
  rules:
  - host: text-responder.david.va-oit.cloud
    http:
      paths:
      - backend:
          serviceName: text-responder
          servicePort: 80
        path: "/"
EOF
```

* Review the certificate that cert-manager has created. You're looking for `The certificate has been successfully issued` in the message section. It may take a minute or two. If the certificate hasn't been issue after five minutes, go looking for problems. Start in the logs of the pods in the `nginx-ingress` namespace.

```bash
kubectl --namespace text-responder describe certificate text-responder-tls
```

* Review the secret that is being created by cert-manager.

```bash
kubectl --namespace text-responder describe secret text-responder-tls
```

* Add annotation to text-responder ingress.

```bash
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: text-responder-ingress
  namespace: text-responder
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-production
spec:
  tls:
  - hosts:
    - $TEXT_RESPONDER_HOST
    secretName: text-responder-tls
  rules:
  - host: $TEXT_RESPONDER_HOST
    http:
      paths:
      - backend:
          serviceName: text-responder
          servicePort: 80
        path: "/"
EOF
```

* Delete secret to get new certificate.

```bash
kubectl --namespace text-responder delete secret text-responder-tls
```

* You'll see the certificate is re-issued.

```bash
kubectl --namespace text-responder describe certificate text-responder-tls
```

* Wait a few minutes for the certificate to be issues and the pods to settle.

```bash
kubectl --namespace text-responder describe secret text-responder-tls
```

* At this point, an HTTPS request should work.

```bash
curl https://$TEXT_RESPONDER_HOST
```

* An HTTP request will work as long as you follow the redirect.

```bash
curl -L http://$TEXT_RESPONDER_HOST
```

## Install KeyCloak

**Note that this KeyCloak as no backup and uses ephemeral drives. Any users and groups will be lost if the pods is restarted. I think.**

**Once you have KeyCloak integrated into the cluster, you (as the admin) will need to use `--context='admin'` and ``--context='medined'` to select which user to authenticate as.**

See https://medined.github.io/kubernetes/keycloak/oidc-connect/oidc/add_oidc_to_kubernetes/

## Install Istio

* Download Istio.

```bash
curl -L https://istio.io/downloadIstio | sh -
```

* Put the download directory in your PATH.

```bash
export PATH="$PATH:/data/projects/ic1/kubespray/istio-1.7.1/bin"
```

* Connect to the installation directory.

```bash
cd istio-1.7.1
```

* Run the precheck.

```bash
istioctl x precheck
```

* Install Istio with the demo configuration profile.

```bash
istioctl install --set profile=demo
```

* Create a namespace for testing Istio.

```bash
kubectl create namespace playground
```

* Enable Istio in the playground namespace.

```bash
kubectl label namespace playground istio-injection=enabled
```

* Deploy the sample application.

```bash
kubectl --namespace playground apply -f samples/bookinfo/platform/kube/bookinfo.yaml
```

* Check the pods and services. Keep checking the pods until they are ready.

```bash
kubectl --namespace playground get services
kubectl --namespace playground get pods
```

* Verify the application is running and serving HTML pages. If the application is working correctly, the response will be `<title>Simple Bookstore App</title>`.

```bash
kubectl --namespace playground exec "$(kubectl --namespace playground get pod -l app=ratings -o jsonpath='{.items[0].metadata.name}')" -c ratings -- curl -s productpage:9080/productpage | grep -o "<title>.*</title>"
```

* Open the application to outside traffic by associating the application to the istio gateway.

```bash
kubectl --namespace playground apply -f samples/bookinfo/networking/bookinfo-gateway.yaml
```

* Check the configuration for errors.

```bash
istioctl --namespace playground analyze
```

* Get connection information.

```
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')
export SECURE_INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="https")].port}')
```

* Set the gateway URL.

```bash
export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT
```

* Visit the product page in your browser.

```bash
xdg-open http://$GATEWAY_URL/productpage
```

* Install the Kiali dashboard, along with Prometheus, Grafana, and Jaeger.

```bash
kubectl apply -f samples/addons
while ! kubectl wait --for=condition=available --timeout=600s deployment/kiali -n istio-system; do sleep 1; done
```

* Visit the Kiali dashboard.

```bash
istioctl dashboard kiali
```

## Install Custom Docker Registry

TBD

## Install Jenkins

TBD

## Install Krew

TBD

## Install Octant

TBD

# Destroy Cluster

```
cd contrib/terraform/aws
terraform destroy --var-file=credentials.tfvars --auto-approve
```

# Links

* https://www.youtube.com/watch?v=OEEr2EX8WYc
