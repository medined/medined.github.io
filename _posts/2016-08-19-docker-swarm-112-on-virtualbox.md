---
layout: post
title: Docker Swarm 1.12 on VirtualBox
author: David Medinets
categories: docker
---

This post shows the steps I followed to run Docker Swarm 1.12 on VirtualBox.

## Start the cluster

```
docker-machine create --driver virtualbox pi1
docker-machine create --driver virtualbox pi2
docker-machine create --driver virtualbox pi3
docker-machine create --driver virtualbox pi4
docker-machine create --driver virtualbox pi5
```

## List the IP Addresses

```
for i in `seq 1 5`; do 
  echo "HOST: pi$i IP: $(docker-machine ip pi$i)"; 
done
```

## Start Swarm Master

```
export SWARM_ID=$(docker-machine ssh pi1 docker swarm --advertise-addr=$(docker-machine ip pi1) init)
```

## Start Agents

```
export SWARM_JOIN_CMD=$(docker-machine ssh pi1 docker swarm join-token worker | tail -n 4 | head -n 3 | tr --delete '\\')

for i in `seq 2 5`; do 
  docker-machine ssh pi$i $SWARM_JOIN_CMD
done
```

# L#ist swarm nodes

```
eval $(docker-machine env pi1)
docker node ls
```

## Exercise the swarm

```
eval $(docker-machine env pi1)
docker service create --name ping alpine ping 8.8.8.8
docker service ps ping
docker service update --replicas 10 ping
docker service rm ping
```

## Shutdown the cluster

```
eval $(docker-machine env --unset)
docker-machine env --unset
for i in `seq 0 5`; do 
  docker-machine rm -y pi$i;
done
```
