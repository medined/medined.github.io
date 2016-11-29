# hyper.sh

Hyper.sh is a secure container hosting service. What makes it different from AWS (Amazon Web Services) is that you don't start servers, but start docker images directly from Docker Hub or other registries. Hyper.sh is running the containers in a new way, in which multi-tenants' containers are inherently safe to run side by side on bare metal, instead of being nested in VMs.

# Running a $1/month web server

* Create a Docker volume

```
./hyper volume create --name=tiny-web-server
```

* Create an index.html file on the volume.

  * Open a shell that accesses the Docker volume

```
./hyper run --size s1 -it --rm --volume tiny-web-server:/www centos /bin/bash
```
  * Create the file.

```
echo "Hello World" > /www/index.html
```

* Run the web server. Note that it won't be accessible to the Internet yet.

```
./hyper run --size s1 --detach --name tiny-web-server -p 80 --volume tiny-web-server:/www fnichol/uhttpd
```

* On the hyper.sh website, allocate one of your floating IP addresses.

* Also on the hyper.sh website, attach a floating IP address to the tiny-web-server container.

Visit your equivalent of http://209.177.88.159/index.html.

That page costs about $1/month to host. ::))
