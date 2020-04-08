---
layout: post
title: Having Healthy Instances In Typhoon's Target Groups
author: David Medinets
categories: kubernetes typhoon podman docker healthz
year: 2020
theme: aws
---

I've been asked to help the Enterprise Design Patterns team at the Department of Veteran's Affairs (https://www.oit.va.gov/library/recurring/edp/index.cfm) to research different ways to provision Kubernetes clusters.

Naturally, they would like to use Fedora CoreOS (FCOS - https://getfedora.org/coreos/) in order to have immutable nodes and kernel isolaton.

To that end, I looked at Typhoon (https://typhoon.psdn.io/) which distributes upstream Kubernetes, architectural conventions, and cluster addons, much like a GNU/Linux distribution provides the Linux kernel and userspace components.

Typhoon supports FCOS using terraform (https://www.terraform.io/) as the configuration tool.

Following the procedure that I documented at https://github.com/medined/silver-argint/blob/typhoon/installers/typhoon/docs/01-create-cluster.md, the cluster started.

NOTE: The document just linked is in a typhoon branch and may be merged into master when you read this. If the link does not work, check the master branch.

However, I noticed that the tempest-workers-http and tempest-workers-https Target Groups did not have healthy instances. The health check **did not complete** successfully. Load balancers do not forward requests to unhealthy instances.

*NOTE: The instances in the tempest-controllers target group do report as healthy and I don't know why. Take my commentary as just a workaround that works for me. I don't know what a production-ready response would be.*

After research, I noticied (and understood) the question at https://docs.fedoraproject.org/en-US/fedora-coreos/faq/#_can_i_run_containers_via_docker_and_podman_at_the_same_time.

The relevant words are "Running containers via docker and podman at the same time can cause issues and unexpected behavior. We highly recommend against trying to use them both at the same time."

Then I looked at the aws/fedora-coreos/kubernetes/workers/fcc/worker.yaml file. I'll show the relevant section:

```
    - name: docker.service
      enabled: true
```

I also did not see any service to respond to the health check. For example, something that responds to <host>:10254/healthz.

*Remember to take everything I say here as 'it works on my computer' sort of advice. My knowledge is just an inch or two deep.*

I created a workaround:

* Cloned Typhoon.
* Changed the docker.service unit to be:
```
    - name: docker.service
      mask: true
```
* Added a healthz.service:. Notice that I am using an image from Docker Hub that I created. You should, for security, create your own image from https://github.com/medined/simple-nodejs. In fact, you should be using a golang image so it is smaller. I just did not want to take the time.
```
    - name: healthz.service
      enabled: true
      contents: |
        [Unit]
        Description=A healthz unit!
        After=network-online.target
        Wants=network-online.target
        [Service]
        Type=forking
        KillMode=none
        Restart=on-failure
        RemainAfterExit=yes
        ExecStartPre=podman pull medined/simple-nodejs:0.0.2
        ExecStart=podman run -d --name healthz-server -p 10254:10254 medined/simple-nodejs:0.0.2
        ExecStop=podman stop -t 10 healthz-server
        ExecStopPost=podman rm healthz-server
        [Install]
        WantedBy=multi-user.target
```
* Changed my tempest.tf file to use my local Typhoon source. The source must be a relative path. Your's will probably be different from mine.
```
module "tempest" {
  #source = "git::https://github.com/poseidon/typhoon//aws/fedora-coreos/kubernetes?ref=v1.18.0"
  source = "../../../typhoon/aws/fedora-coreos/kubernetes"
  ...
}
* Ran `terraform init`
* Ran `terraform apply`

After a few minutes, your cluster should be working with healthly nodes in the worker target groups.
