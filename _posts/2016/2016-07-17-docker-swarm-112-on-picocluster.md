---
layout: post
title: Docker Swarm 1.12 on PicoCluster
date: '2016-07-17T12:40:00.003-04:00'
author: David Medinets
categories: docker raspberry-pi
year: 2016
---

This post shows the steps I followed to run Docker Swarm 1.12 on my 5-node Raspberry PI PicoCluter.


I followed the directions at https://medium.com/@bossjones/how-i-setup-a-raspberry-pi-3-cluster-using-the-new-docker-swarm-mode-in-29-minutes-aa0e4f3b1768#.ma06iyonf but tweaked them a bit.

First off, I wanted to have my cluster using eth0 to connect to my laptop and then share its WiFi connection. Using this technique means that my WiFi network name and password are not on the cluster. So the cluster should be able to plug into any laptop or server without changes. Follow instructions at https://t.co/2jRbNAOiCU to share your eth0 connection.

Use `lsblk` to umount any directories on the SD cards you'll be using.

Now flash (https://github.com/hypriot/flash) the SD cards using the flash tool from hypriot. Notice that *no* network information is provided.

I used piX naming convention so that I can easily loop over all five RPI in the PicoCluster.

```
flash --hostname pi1 --device /dev/mmcblk0 https://github.com/hypriot/image-builder-rpi/releases/download/v0.8.1/hypriotos-rpi-v0.8.1.img.zip
flash --hostname pi2 --device /dev/mmcblk0 https://github.com/hypriot/image-builder-rpi/releases/download/v0.8.1/hypriotos-rpi-v0.8.1.img.zip
flash --hostname pi3 --device /dev/mmcblk0 https://github.com/hypriot/image-builder-rpi/releases/download/v0.8.1/hypriotos-rpi-v0.8.1.img.zip
flash --hostname pi4 --device /dev/mmcblk0 https://github.com/hypriot/image-builder-rpi/releases/download/v0.8.1/hypriotos-rpi-v0.8.1.img.zip
flash --hostname pi5 --device /dev/mmcblk0 https://github.com/hypriot/image-builder-rpi/releases/download/v0.8.1/hypriotos-rpi-v0.8.1.img.zip
```

Using this function, you can find the IP addresses for the RPI.

```
function getip() { (traceroute $1 2>&1 | head -n 1 | cut -d\( -f 2 | cut -d\) -f 1) }
```

List the IP addresses.

```
for i in `seq 1 5`; do
  echo "HOST: pi$i; IP: $(getip pi$i.local)";
done
```

Remove any fingerprints for the RPI.

```
for i in `seq 1 5`; do
  ssh-keygen -R pi${i}.local 2&gt;/dev/null;
done
```

Create variable to hold `ssh` options you'll use frequently

```
export SSH_OPTIONS="-oStrictHostKeyChecking=no -oCheckHostIP=no"
```

Copy your PKI identity to the RPI.

```
for i in `seq 1 5`; do
  ssh-copy-id $SSH_OPTOINS pirate@pi${i}.local;
done
```

Download the deb file for Docker v1.12

```
curl -O https://jenkins.hypriot.com/job/armhf-docker/17/artifact/bundles/latest/build-deb/raspbian-jessie/docker-engine_1.12.0%7Erc4-0%7Ejessie_armhf.deb
```

Copy the deb file to the RPI

```
for i in `seq 1 5`; do
  scp $SSH_OPTIONS docker-engine_1.12.0%7Erc4-0%7Ejessie_armhf.deb pirate@pi$i.local:.;
done
```

Remove older Docker version from the RPI

```
for i in `seq 1 5`; do
  ssh $SSH_OPTIONS pirate@pi$i.local sudo apt-get purge -y docker-hypriot;
done
```

Install Docker

```
for i in `seq 1 5`; do
  ssh $SSH_OPTIONS pirate@pi$i.local sudo dpkg -i docker-engine_1.12.0%7Erc4-0%7Ejessie_armhf.deb;
done
```

Initialize the Swarm

```
ssh $SSH_OPTIONS pirate@pi1.local docker swarm init
```

Join slaves to Swarm - replace the join command below with the specific one displayed by the init command.


```
for i in `seq 2 5`; do
  ssh $SSH_OPTIONS pirate@pi$i.local docker swarm join --secret ceuok9jso0klube8m3ih9gcsv --ca-hash sha256:f0864eb57963e3f9cd1756e691d0b609903e3a0bb48785272ea53155809025ee 10.42.0.49:2377;
done
```

Exercise the Swarm

```
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi1.local
docker service create --name ping hypriot/rpi-alpine-scratch ping 8.8.8.8
docker service tasks ping
docker service update --replicas 10 ping
docker service tasks ping
docker service rm ping
```

Shutdown the Swarm

```
for i in `seq 1 5`; do
  ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi$i.local sudo shutdown -h +0 "Shutting down";
done
```
