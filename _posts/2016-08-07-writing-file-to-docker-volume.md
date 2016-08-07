---
layout: post
title: From BASH, how to write to a file inside a Docker Volume
categories: docker
---

I tried several different ways to create a simple text file inside a Docker Volume. The following worked:

```
docker volume create --name=mysql-configuration

docker run \
  --rm=true \
  --volume=mysql-configuration:/data \
  ubuntu \
  sh -c 'echo "[mysqld]\nmax_connections=100" > /data/mysql-configuration.cnf'

docker run \
  --rm=true \
  --volume=mysql-configuration:/data \
  ubuntu \
  cat /data/mysql-configuration.cnf
```

I hope that the Docker Volume system evolves to act like Hadoop's. It would be nice to have ``put`` and ``get`` functionality.
