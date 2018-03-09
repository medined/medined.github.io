---
layout: post
title: Using hyper.sh for software development
author: David Medinets
categories: docker
year: 2016
---

 Hyper.sh is a secure container hosting service. What makes it different from AWS (Amazon Web Services) is that you don't start servers, but start docker images directly from Docker Hub or other registries. Hyper.sh is running the containers in a new way, in which multi-tenants' containers are inherently safe to run side by side on bare metal, instead of being nested in VMs.

* Create a Docker volume to hold your project files.

```
./hyper volume create --name=development
```

* Create a Docker container where you can perform work. This container is about 25 cents per hour.

```
./hyper run --size m2 -it --rm --volume development:/projects centos /bin/bash
```

* Create SSH file so that GitHub knows who you are.

```
mkdir ~/.ssh
chmod 700 ~/.ssh

cat << EOF > ~/.ssh/id_rsa
-----BEGIN RSA PRIVATE KEY-----
XTz8S7HbpSj3bz6PqT5AxIGk7jnCyLvjIuO9tk3wxFdYgiCkSpHE44Wku32MLJct
...
Bh0z0XMCgYEAjCsLw+zhObeKdTuhtmzzpHu7jaI97OET7+5MwGFZbzgcdf9f37FN
dEIHo1XYuxRpqFXMNz6kwZgs8k8+uPM4C8fu4r4UHqVbdZwzM5pvhQoo+qzvePNL
TmeVsBVUQPTs6K1IO3MEPfIN4m366MselXW0tLcvPi6hOPkl5Kzqj+o=
-----END RSA PRIVATE KEY-----
EOF
chmod 600 ~/.ssh/id_isa

```

* Install git so you can pull a project from GitHub.

```
yum install -y git
git config --global user.email "david.medinets@gmail.com"
git config --global user.name "medined"
git config --global push.default simple
git clone git@github.com:medined/medined.github.io.git
```

That's - do your work - then ```git commit``` and exit the
Docker container.

PS - this entry was written inside a hyper.sh container.
