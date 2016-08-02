---
layout: post
title: ceph
---

## Resources

* http://bryanapperson.com/blog/the-definitive-guide-ceph-cluster-on-raspberry-pi/
* https://www.linkedin.com/pulse/ceph-raspberry-pi-rahul-vijayan
* http://millibit.blogspot.co.uk/2015/01/ceph-pi-adding-osd-and-more-performance.html
* http://millibit.blogspot.com/2014/12/ceph-pi-installing-ceph-on-raspberry-pi.html
* http://dachary.org/?p=1971 - Installing ceph with ceph-deploy

## Steps

Install some basic software

```
for i in `seq 1 5`; do 
  ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi$i.local "sudo apt-get install -y vim screen htop iotop btrfs-tools lsb-release gdisk"
done
```

Connect to the first PRI.

```
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi1.local
```

Create a PKI key.

```
ssh-keygen
```

Copy the PKI key to the servers to enable password-less SSH. You'll be prompted for passwords (which is 'hypriot').

```
ssh-copy-id pirate@pi1
ssh-copy-id pirate@pi2
ssh-copy-id pirate@pi3
ssh-copy-id pirate@pi4
ssh-copy-id pirate@pi5
```

Add a ceph repository to each RPI. Then update.

```
echo "deb http://download.ceph.com/debian-jewel/  jessie main" > ceph.list
for i in `seq 1 5`; do 
  scp ceph.list pirate@pi$i:.;
done
for i in `seq 1 5`; do 
  ssh pirate@pi$i "sudo mv ceph.list /etc/apt/sources.list.d/ceph.list"
done
for i in `seq 1 5`; do 
  ssh pirate@pi$i "sudo apt-get update"
done
```

Fix bug that stops ping from working.

for i in `seq 1 5`; do 
  ssh pirate@pi$i "sudo chmod u+s /bin/ping"
done

Update and install ceph

```
for i in `seq 1 5`; do 
  ssh pirate@pi$i "sudo apt-get install -y ceph ceph-common"
done
```

Install ntp

for i in `seq 1 5`; do 
  ssh pirate@pi$i "sudo apt-get install -y ntp"
done
```


Install the ceph deploy script on RPI 1.

```
sudo apt-get install -y ceph-deploy
```

Remove references to 127.0.0.1 in /etc/hosts because you are using a networked server not a stand-alone server.

mkdir ceph-config
cd ceph-config

ceph-deploy new pi1

Edit ceph.conf file
  osd_pool_default_size = 2
  public_network = 10.42.0.0/24
  osd_journal_size = 1024 # Disable in-memory logs
  debug_lockdep = 0/0
  debug_context = 0/0
  debug_crush = 0/0
  debug_buffer = 0/0
  debug_timer = 0/0
  debug_filer = 0/0
  debug_objecter = 0/0
  debug_rados = 0/0
  debug_rbd = 0/0
  debug_journaler = 0/0
  debug_objectcatcher = 0/0
  debug_client = 0/0
  debug_osd = 0/0
  debug_optracker = 0/0
  debug_objclass = 0/0
  debug_filestore = 0/0
  debug_journal = 0/0
  debug_ms = 0/0
  debug_monc = 0/0
  debug_tp = 0/0
  debug_auth = 0/0
  debug_finisher = 0/0
  debug_heartbeatmap = 0/0
  debug_perfcounter = 0/0
  debug_asok = 0/0
  debug_throttle = 0/0
  debug_mon = 0/0
  debug_paxos = 0/0
  debug_rgw = 0/0
  osd heartbeat grace = 8
 
[mon]
  mon compact on start = true
  mon osd down out subtree_limit = host
 
[osd]
  # Filesystem Optimizations
  osd mkfs type = btrfs
  osd journal size = 512
 
  # Performance tuning
  max open files = 327680
  osd op threads = 2
  filestore op threads = 2
  
  #Capacity Tuning
  osd backfill full ratio = 0.95
  mon osd nearfull ratio = 0.90
  mon osd full ratio = 0.95
 
  # Recovery tuning
  osd recovery max active = 1
  osd recovery max single start = 1
  osd max backfills = 1
  osd recovery op priority = 1
 
  # Optimize Filestore Merge and Split
  filestore merge threshold = 40
  filestore split multiple = 8
  osd_pool_default_min_size = 2


sudo journalctl -f
sudo journalctl --vacuum-size=100MB

ceph-deploy install pi1 pi2 pi3 pi4 pi5
ceph-deploy new pi1 pi2 pi3 pi4 pi5
ceph-deploy mon create-initial
ceph-deploy admin pi1 pi2 pi3 pi4 pi5
ceph-deploy mds create pi1 pi2 pi3 pi4 pi5
 
ssh ceph1 "chmod 644 /etc/ceph/ceph.client.admin.keyring"
ssh ceph2 "chmod 644 /etc/ceph/ceph.client.admin.keyring"
ssh ceph3 "chmod 644 /etc/ceph/ceph.client.admin.keyring"



root@pi1:/home/pirate/ceph-config# cat /var/log/ceph/ceph.log
2016-07-21 20:16:26.166808 unknown.0 :/0 0 :  [INF] mkfs ec70aafa-dbc9-4e6e-88d3-a975e474dab0
2016-07-21 20:16:25.991535 mon.0 10.42.0.49:6789/0 1 : cluster [INF] mon.pi1@0 won leader election with quorum 0
2016-07-21 20:16:26.121073 mon.0 10.42.0.49:6789/0 2 : cluster [INF] mon.pi1@0 won leader election with quorum 0
2016-07-21 20:16:26.140120 mon.0 10.42.0.49:6789/0 3 : cluster [INF] monmap e1: 1 mons at {pi1=10.42.0.49:6789/0}
2016-07-21 20:16:26.188543 mon.0 10.42.0.49:6789/0 4 : cluster [INF] pgmap v1: 0 pgs: ; 0 bytes data, 0 kB used, 0 kB / 0 kB avail
2016-07-21 20:16:26.286958 mon.0 10.42.0.49:6789/0 5 : cluster [INF] mdsmap e1: 0/0/0 up
2016-07-21 20:16:26.323232 mon.0 10.42.0.49:6789/0 6 : cluster [INF] osdmap e1: 0 osds: 0 up, 0 in
2016-07-21 20:16:26.365556 mon.0 10.42.0.49:6789/0 7 : cluster [INF] pgmap v2: 64 pgs: 64 creating; 0 bytes data, 0 kB used, 0 kB / 0 kB avail






vi ~/.ssh/config
```
Host pi1  
   Hostname pi1  
   User hypriot
Host pi2  
   Hostname pi2  
   User hypriot
Host pi3  
   Hostname pi3  
   User hypriot
Host pi4
   Hostname pi5  
   User hypriot
Host pi5  
   Hostname pi5  
   User hypriot
``

```


