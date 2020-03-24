---
layout: post
title: OpenShift (with Docker) on Centos 7 inside Virtualbox
author: David Medinets
categories: OpenShift Centos Docker Virtualbox
year: 2018
theme: Docker
---

These steps assume that you are using Windows with Virtualbox already installed. It creates an everything version of Centos which uses a GNOME desktop.

# Install Centos 7 inside Virtualbox

* Download the ISO file for Everything Centos 7 from http://isoredirect.centos.org/centos/7/isos/x86_64/CentOS-7-x86_64-Everything-1804.iso. This ISO is 8.8GB so the download will take a few minutes.
* Open VirtualBox
* Click New
* Use ‘centos everything’ for the Name. Use ‘Red Hat (64-bit)’ for the Version. Click Next
* Use 2048MB or more for the memory size. Click Next
* Click Create to create the virtual hard disk
* Make sure that VDI is selected. Click Next
* Make sure that Dynamically allocated is selected. Click Next
* Use at least 50GB for the hard disk size. Click Create
* Click Start to power on the virtual machine
* Select the downloaded ISO file as the start-up disk. Click Start
* Select Install CentOS 7. Press Enter
* Leave English as the selected language. Click Continue
* Click Software Selection. Select “GNOME Desktop” in left-hand column. Select “GNOME Applications”, “Development Tools”, and “System Administration Tools” in the right-hand column. Click Done
* Click Installation Destination. Click Done
* Click Network & Host Name. Click the on/off button. Click Done
* Click Security Policy. Click the on/off button. Click Done
* Click Begin Installation
* Click Root Password. Enter password as the password. Click Done
* Click User Creation. Enter 'frog' as the full name. Select ‘Make this user administrator’. Unselect ‘Require a password to use this account’. Click Done
* Click Reboot
* Click License Registration. Select ‘I accept the license agreement’. Click Done
* Click Finish Configuration
* Login as ‘frog. You won’t be asked for a password.

## Install Guest Additions

* With Centos running, select Devices > Insert Guest Additions CD image from the Virtualbox menu
* Click Run
* Press Enter to close the window.
* Notice that the mouse pointer is no longer 'captured' by the Virtualbox window.
* Right-click the Vbox_GAs_5.2.8 icon on the desktop. Select Eject.
* Select Devices > Shared Clipboard from the Virtualbox menu. Then select bidirectional.
* Install Terminator
* In a terminal window, type the following commands.

```bash
sudo yum update –y
sudo yum install –y epel-release
sudo yum install –y terminator
exit
```

* Press the Windows key. Then type 'terminator'.  Press Enter
* You now have a much, much better terminal.

## Install Docker

* Open a terminal window.
* Type the following:

```bash
sudo yum install docker –y
sudo systemctl start docker
sudo systemctl enable docker
sudo groupadd docker
sudo chown root:docker /var/run/docker.sock
sudo usermod –aG docker frog
```

* Log out and back in to pick up the new group information.
* Press Windows Key. Type terminal. Press Enter.
* Enter 'docker info'
* Enter the following command to run the hello-world application inside a docker container.

```bash
docker run --rm hello-world
```

* Docker is now installed.

## Installing OpenShift

* Open a terminal window.
* OpenShift uses an insecure Docker registry which Docker needs to know about.

```bash
echo "{ \"insecure-registries\" : [ \"172.30.0.0/16\" ] }" | sudo tee /etc/docker/daemon.json > /dev/null
sudo systemctl daemon-reload
sudo systemctl restart docker
```

* Download and install OpenShift

```bash
wget https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-linux-64bit.tar.gz
tar xvfz openshift-origin-client-tools-v3.9.0-191fece-linux-64bit.tar.gz
sudo mv openshift-origin-client-tools-v3.9.0-191fece-linux-64bit/oc /usr/local/bin
rm openshift-origin-client-tools-v3.9.0-191fece-linux-64bit.tar.gz
rm –rf openshift-origin-client-tools-v3.9.0-191fece-linux-64bit
oc version
```

* Launch OpenShift

```bash
oc cluster up
```

* Take note of the URL and other information.
* OpenShift is now running.
