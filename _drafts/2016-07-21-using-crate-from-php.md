---
layout: post
---

## TODO

* data volume
* nodes recognize each other


## Research Notes

* https://github.com/jkbrzt/httpie - a curl replacement
* https://crate.io/docs/clients/rest/
* https://github.com/crate/crate-sample-apps/tree/master/php

## Steps

ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi1.local
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi2.local
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi3.local
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi4.local
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi5.local



docker run --rm -it ubuntu
apt update
apt upgrade -y
apt install -y git
git clone https://github.com/crate/crate-sample-apps.git


docker service create \
  --mode global \
  --mount target=/data,source=crate_db,type=bind,writable=true \
  --name crate \
  --update-delay 10s \
  --publish 4200:4200 \
  --publish 4300:4300 \
  --update-parallelism 1 \
  hypriot/rpi-crate:0.54.1

docker service create \
  --name whoami \
  --mode global \
  --mount target=/data,source=crate_db,type=bind \
  -p 80:8000 \
  hypriot/rpi-whoami

docker run --rm -it -P -e NGINX_NO_UPDATE= ckulka/rpi-nginx

docker service create \
  --name nginx \
  --mode global \
  --mount target=/data,source=crate_db,type=bind \
  --publish 80:80 \
  --env NGINX_NO_UPDATE= \
  ckulka/rpi-nginx


        "Mounts": [
            {
                "Name": "crate_db",
                "Source": "/var/lib/docker/volumes/crate_db/_data",
                "Destination": "/data",
                "Driver": "local",
                "Mode": "z",
                "RW": true,
                "Propagation": "rprivate"
            }
        ],



docker volume create --name crate_db
docker run -d -p 4200:4200 -p 4300:4300 crate

docker service create \
  --mode global \
  --mount target=/data,source=crate_db,type=bind,writable=true \
  --name crate \
  --update-delay 10s \
  --publish 4200:4200 \
  --publish 4300:4300 \
  --update-parallelism 1 \
  hypriot/rpi-crate:0.54.1

docker service tasks crate
firefox http://$(getip pi1.local):4200/admin

http://10.42.0.49:4200/admin

http://10.42.0.32:4200/admin

export HOSTS="pi1:4300,pi2:4300,pi3:4300,pi4:4300,pi5:4300"
export HOST="pi1"

docker service create \
  --mode global \
  --name crate \
  --update-delay 10s \
  --publish 4200:4200 \
  --publish 4300:4300 \
  --update-parallelism 1 \
  hypriot/rpi-crate:0.54.1 \
    -Des.cluster.name=crate-cluster \
    -Des.node.name=crate1 \
    -Des.transport.publish_port=4300 \
    -Des.network.publish_host="$HOST" \
    -Des.multicast.enabled=false \
    -Des.discovery.zen.ping.unicast.hosts="$HOSTS" \
    -Des.discovery.zen.minimum_master_nodes=2

