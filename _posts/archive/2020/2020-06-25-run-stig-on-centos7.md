---
layout: post
title: Running MindPoint Group RHEL7 STIG On Centos
author: David Medinets
categories: centos terraform ansible stig
year: 2020
theme: stig
---

This is going to be a long post. We'll start from scratch and develop the ability to run the MindPoint Group RHEL7 STIG on Centos 7. If you want to go farther, there is also discussion of Lynis. Just running the STIG playbook results in the server having a Lynis hardening index of 73. It can be better.

This work is being done at the request of the Enterprise Container Working Group (ECWG) of the Office of Information and Technology (OIT - <https://www.oit.va.gov/> at the Department of Veteran Affairs.

## Overview

* Provision a server.
* Initial setup so Ansible will run.
* Run Ansible playbook to be STIG compliant.

## Provision

Terraform is used to provision an EC2 server. All configuration is done in `variables.tf`.

* Create a `provision` directory.

* Create `variables.tf`. Make sure to update using your own values.

```yaml
variable "aws_profile" {
  default = "ic1"
}

variable "aws_region" {
  default = "us-east-1"
}

variable "instance_type" {
  default = "t3.medium"
}

variable "pki_private_key" {
  default = "KEYPAIR.pem"
}

variable "pki_public_key" {
  default = "KEYPAIR.pub"
}

variable "ssh_cidr_block" {
  default = "0.0.0.0/0"
}

# The ssh_user variable is used by both Terraform and Ansible.
variable "ssh_user" {
  default = "centos"
}

variable "subnet_id" {
  default = "subnet-02c78f939d58e2320"
}

variable "vpc_id" {
  default = "vpc-04bdc9b68b19472c3"
}

# Ansible Variables

variable "ansible_python_interpreter" {
  default = "/bin/python3"
}

variable "banner_text_file" {
  default = "file-banner-text.txt"
}

variable "password_max_days" {
  default = "90"
}

variable "password_min_days" {
  default = "1"
}

variable "sha_crypt_max_rounds" {
  default = "10000"
}

variable "sha_crypt_min_rounds" {
  default = "5000"
}
```

* Create `tr_ansible_vars_file.yml.tpl`. This template is used to generate `tr_ansible_vars_file.yml` which is read by the Ansible playbook.

```properties
ansible_python_interpreter: ${ansible_python_interpreter}
centos_user_password: ${centos_user_password}
```

* Create `data-sources.tf`.

```yaml
#
# Find the latest CentOS AMI.
#
data "aws_ami" "centos" {
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

#
# The password will be created as a resource in another file. After the
# terraform plan is applied, the clear text password is in
# tf_ansible_vars_file.yml. Be careful not to check that file into
# a code repository.
#
data "template_file" "tf_ansible_vars_file" {
    template = "${file("./tr_ansible_vars_file.yml.tpl")}"
    vars = {
        ansible_python_interpreter = var.ansible_python_interpreter
        centos_user_password = random_password.centos_user_password.result
    }
}
```

* Create `security-groups.tf`

```yaml

#
# Ingress
#

resource "aws_security_group" "centos_allow_ssh" {
  name        = "centos_allow_ssh"
  description = "Allow SSH"
  vpc_id      = var.vpc_id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [ var.ssh_cidr_block ]
  }

  tags = {
    Name = "centos_allow_ssh"
  }
}

#
# Egress
#
resource "aws_security_group" "centos_allow_any_outbound" {
  name        = "centos_allow_any_outbound"
  description = "Centos Allow Any Outbound"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "centos_allow_any_outbound"
  }
}
```

* Create `infrastructure-as-code.tf`. Here is the heart of the Terraform file. This file creates the EC2 instance. Note that an EIP is allocated so that the instance can be stopped and restarted without a chance in IP address. An `inventory` file is created for Ansible to use. The EC2 name will have a timestamp so that you can create more than one instance. **However, if you do provision more than one server you lose the ability to destroy using terraform.**

```yaml
# When a profile is specified, tf will try to use
# ~/.aws/credentials.

provider "aws" {
  region  = var.aws_region
  profile = var.aws_profile
  version = "~> 2.66"
}

resource "random_password" "centos_user_password" {
  length = 16
  special = true
  override_special = "_%@"
}

resource "aws_key_pair" "centos" {
  public_key = file(var.pki_public_key)
}

resource "aws_instance" "centos" {
  ami           = data.aws_ami.centos.id
  associate_public_ip_address = "true"
  instance_type = var.instance_type
  key_name      = aws_key_pair.centos.key_name
  subnet_id     = var.subnet_id
  vpc_security_group_ids = [
    aws_security_group.centos_allow_ssh.id,
    aws_security_group.centos_allow_any_outbound.id
  ]
  tags = { Name = "centos-${formatdate("YYYYMMDDhhmmss", timestamp())}" }
}

resource "local_file" "tf_ansible_vars_file" {
  content = data.template_file.tf_ansible_vars_file.rendered
  filename = "${path.module}/tf_ansible_vars_file.yml"
}

resource "aws_eip" "centos" {
  instance = aws_instance.centos.id
  vpc      = true
  tags = { Name = "centos-${formatdate("YYYYMMDDhhmmss", timestamp())}" }
  connection {
    type        = "ssh"
    user        = var.ssh_user
    private_key = file(var.pki_private_key)
    host        = self.public_ip
  }
  provisioner "remote-exec" {
    inline = [
      "sudo yum install -y python3"
    ]  
  }
  provisioner "local-exec" {
    command = "ansible-playbook -u ${var.ssh_user} -i '${self.public_ip},' --private-key ${var.pki_private_key} playbook.setup.yml"
    environment = {
      ANSIBLE_HOST_KEY_CHECKING = "False"
    }
    #    command = "./run-setup-playbook.sh ${var.ssh_user} ${self.public_ip},' ${var.pki_private_key}"
  }
}

#
# We need to export the EIP ip address, not the instance's.
#
resource "local_file" "inventory" {
  content = "[all]\n${aws_eip.centos.public_ip}\n"
  filename = "${path.module}/inventory"
}
```

* Create the last file needed for provisioning, the `playbook.setup.yml` for Ansible.

```yaml
---
- hosts: all
  gather_facts: false
  become: yes

  vars_files:
    - tf_ansible_vars_file.yml

  tasks:

    - name: upgrade all packages
      yum:
        name: '*'
        state: latest
      vars:
        ansible_python_interpreter: /usr/bin/python

    #
    # The python3-dnf package is not being found. So I am using yum
    # instead of dnf.
    #
    - name: install packages with python2
      yum:
        name:
          - epel-release
        state: latest
        update_cache: yes
      vars:
        ansible_python_interpreter: /usr/bin/python

    - name: install python selinux bindings so that copy task will work.
      yum:
        name:
          - libselinux-python3
        state: latest
        update_cache: yes
      vars:
        ansible_python_interpreter: /usr/bin/python

    - name: Create Lynis Yum repository file.
      copy:
        dest: /etc/yum.repos.d/lynis.repo
        content: |
          [lynis]
          name=CISOfy Software - Lynis package
          baseurl=https://packages.cisofy.com/community/lynis/rpm/
          enabled=1
          gpgkey=https://packages.cisofy.com/keys/cisofy-software-rpms-public.key
          gpgcheck=1
          priority=2
        mode: "644"

    - name: install packages with python2
      yum:
        name:
          - aide
          - ca-certificates
          - curl
          - fail2ban
          - lynis
          - nss
          - openssl
          - python2-jmespath
          - usbguard
        state: latest
        update_cache: yes
      vars:
        ansible_python_interpreter: /usr/bin/python

    - name: set password for 'centos' user.
      user:
        name: centos
        password: "{{ centos_user_password | password_hash('sha512') }}"

    - name: delete no password sudo configuration
      file:
        path: /etc/sudoers.d/90-cloud-init-users
        state: absent
```

* Let Terraform provision the server.

```bash
teraform init
terraform plan
terraform apply --auto-approve
```

* When the apply is done, you'll be able to SSH into the server. You don't need to perform this step. It's just for general knowlege in case you do.

```bash
ssh-add KEYPAIR.pem
IP_ADDRESS=$(cat inventory | tail -n 1)
ssh centos@$IP_ADDRESS
```

* Make the current directory available to future steps.

```bash
export PROVISION_DIR=$(pwd)
```

## Run RHEL7 STIG

* Make sure to have jmespath installed on your local workstation.

```bash
pip install jmespath
```

* Download the STIG project.

```bash
cd /data/projects
git clone https://github.com/MindPointGroup/RHEL7-STIG.git
```

* Connect to the STIG directory.

```bash
cd /data/projects/RHEL7-STIG
```

* Create a playbook to run the STIG.

```bash
cat <<EOF > playbook.stig.yml
---
- name: Apply STIG
  hosts: all
  become: yes
  roles:
    - role: "{{ playbook_dir }}"
EOF
```

* Create a script to run the STIG playbook.

```bash
cat <<EOF > run-stig-playbook.sh
#!/bin/bash

# Copy the inventory file generated by Terraform.
cp $PROVISION_DIR/inventory inventory

# Get the SUDO password from the generated Ansible variable file.
ANSIBLE_SUDO_PASS=\$(cat \$PROVISION_DIR/tf_ansible_vars_file.yml | grep centos_user_password | awk '{print \$2}')

python3 \
  \$(which ansible-playbook) \
	--extra-vars "ansible_sudo_pass=\$ANSIBLE_SUDO_PASS" \
  -i inventory \
  -u centos \
  playbook.stig.yml
EOF

chmod +x run-stig-playbook.sh
```

* Run the STIG playbook.

```bash
./run-stig-playbook.sh | tee stig.out
```

## STIG Results

The results are too long to include directly. See <https://gist.github.com/medined/52d814466fa11d4a633561011c29ccf1>.

## Lynis Results

The results are too long to include directly. See <https://gist.github.com/medined/1d15a0c5b599fed8fc2515bcd0c212ad>.

## Improving Lynis Hardening Index

* Create a script to run the Lynis playbook.

```bash
cat <<EOF > run-lynis-playbook.sh
#!/bin/bash

# Copy the inventory file generated by Terraform.
cp $PROVISION_DIR/inventory inventory

# Get the SUDO password from the generated Ansible variable file.
ANSIBLE_SUDO_PASS=\$(cat \$PROVISION_DIR/tf_ansible_vars_file.yml | grep centos_user_password | awk '{print \$2}')

python3 \
  \$(which ansible-playbook) \
	--extra-vars "ansible_sudo_pass=\$ANSIBLE_SUDO_PASS" \
	--extra-vars "ssh_user=centos" \
  -i inventory \
  -u centos \
  playbook.lynis.yml
EOF

chmod +x run-lynis-playbook.sh
```

* Create `playbook.lynis.yml`

```yaml
---
- hosts: all
  gather_facts: false
  become: yes

  handlers:

    - name: restart sshd
      service: name=sshd state=restarted
      listen: restart sshd

    - name: Unconditionally reboot the machine
      reboot:
      listen: reboot system

  tasks:

    # ....###....##.....##.########.##.....##
    # ...##.##...##.....##....##....##.....##
    # ..##...##..##.....##....##....##.....##
    # .##.....##.##.....##....##....#########
    # .#########.##.....##....##....##.....##
    # .##.....##.##.....##....##....##.....##
    # .##.....##..#######.....##....##.....##


    - name: AUTH-9230 password hashing rounds - min
      lineinfile:
        path: /etc/login.defs
        state: present
        regexp: "^SHA_CRYPT_MIN_ROUNDS"
        line: "SHA_CRYPT_MIN_ROUNDS 6000"
      tags:
        - AUTH-9230

    - name: AUTH-9230 password hashing rounds - max
      lineinfile:
        path: /etc/login.defs
        state: present
        regexp: "^SHA_CRYPT_MAX_ROUNDS"
        line: "SHA_CRYPT_MAX_ROUNDS 10000"
      tags:
        - AUTH-9230

    - name: AUTH-9328 - Default umask values
      lineinfile:
        path: /etc/login.defs
        state: present
        regexp: "^UMASK"
        line: "UMASK 027"
      tags:
        - AUTH-9328

    - name: AUTH-9328 - Default umask values in /etc/login.defs
      copy:
        dest: /etc/profile.d/umask.sh
        content: |
          # By default, we want umask to get set. This sets it for login shell
          # Current threshold for system reserved uid/gids is 200
          # You could check uidgid reservation validity in
          # /usr/share/doc/setup-*/uidgid file
          if [ $UID -gt 199 ] && [ "`id -gn`" = "`id -un`" ]; then
              umask 007
          else
              umask 027
          fi
        mode: "644"
      tags:
        - AUTH-9328

    # NIST recommends setting the daemon umask to 027
    # (REHL5: http://nvd.nist.gov/scap/content/stylesheet/scap-rhel5-document.htm).
    #
    - name: AUTH-9328 - does /etc/init.d/functions exist?
      stat:
        path: /etc/init.d/functions
      register: auth9328

    - name: AUTH-9328 - Default umask values in /etc/init.d/functions
      lineinfile:
        path: /etc/init.d/functions
        state: present
        regexp: "^umask 022"
        line: "umask 027"
      when: auth9328.stat.exists
      tags:
        - AUTH-9328

    - name: AUTH-9408 (Logging of failed login attempts)
      lineinfile:
        path: /etc/login.defs
        state: present
        regexp: "^FAILLOG_ENAB"
        line: "FAILLOG_ENAB yes"
      tags:
        - AUTH-9328

    - name: Ensure delay after failed login
      lineinfile:
        path: /etc/login.defs
        state: present
        regexp: "^FAIL_DELAY"
        line: "FAIL_DELAY 4"
      tags:
        - "https://www.lisenet.com/2017/centos-7-server-hardening-guide/"


    # .########.####.##.......########
    # .##........##..##.......##......
    # .##........##..##.......##......
    # .######....##..##.......######..
    # .##........##..##.......##......
    # .##........##..##.......##......
    # .##.......####.########.########

    - name: FILE-6344 proc mount - hidepid
      block:
        - name: FILE-6344 proc mount - hidepid
          lineinfile:
            path: /etc/fstab
            state: present
            regexp: "^#?proc /proc"
            line: proc /proc proc rw,nosuid,nodev,noexec,relatime,hidepid=2 0 0
          tags:
            - FILE-6344

        #
        # Since /proc is using hidepid, the polkitd can not see
        # /proc unless we fix its access.
        #
        # The next three steps fixes "GDBus.Error:org.freedesktop.PolicyKit1.Error.Failed  - Cannot determine user of subject"
        #
        - name: FILE-6344 proc mount - create group
          group:
            name: monitor
            state: present

        - name: FILE-6344 proc mount - add monitor to group polkitd
          user:
            name: polkitd
            groups: monitor
            append: yes

        - name: FILE-6344 proc mount - get group id
          shell: getent group monitor | cut -d':' -f3
          register: monitor_group_register

        - debug:
            var: monitor_group_register.stdout

        - name: FILE-6344 proc mount - hidepid
          lineinfile:
            path: /etc/fstab
            state: present
            regexp: "^#?proc /proc"
            line: "proc /proc proc rw,nosuid,nodev,noexec,relatime,hidepid=2,gid={{ monitor_group_register.stdout }} 0 0"
          tags:
            - FILE-6344

    - name: FILE-6374 mount /dev/shm noexec
      lineinfile:
        path: /etc/fstab
        state: present
        regexp: "^#?tmpfs /dev/shm"
        line: tmpfs /dev/shm /tmpfs rw,seclabel,nosuid,noexec,nodev,size=2G 0 0
      tags:
        - FILE-6374

    - name: FILE-6374 mount /dev noexec
      lineinfile:
        path: /etc/fstab
        state: present
        regexp: "^#?devtmpfs /dev"
        line: devtmpfs /dev devtmpfs rw,seclabel,nosuid,noexec,size=2G,nr_inodes=471366,mode=755 0 0
      tags:
        - FILE-6374

    - name: FILE-6374 mount /tmp noexec
      lineinfile:
        path: /etc/fstab
        state: present
        regexp: "^#?tmpfs /tmp"
        line: tmpfs /tmp tmpfs rw,seclabel,nosuid,noexec,nodev,size=2G 0 0
      tags:
        - FILE-6374

    #
    # Some pages on the Internet suggested to use "blacklist <filesystem>"
    # instead of the "/bin/true" approach. Empirical testing shows that
    # the approach below works. At least as far as Lynis is concerned.
    #
    - name: FILE-6430 (Disable mounting of some filesystems)
      copy:
        dest: /etc/modprobe.d/lynis-filesystem-blacklist.conf
        content: |
          install cramfs /bin/true
          install squashfs /bin/true
          install udf /bin/true
      tags:
        - FILE-6430
        - CCE-80137-3


    # .##.....##....###....########..########..########.##....##
    # .##.....##...##.##...##.....##.##.....##.##.......###...##
    # .##.....##..##...##..##.....##.##.....##.##.......####..##
    # .#########.##.....##.########..##.....##.######...##.##.##
    # .##.....##.#########.##...##...##.....##.##.......##..####
    # .##.....##.##.....##.##....##..##.....##.##.......##...###
    # .##.....##.##.....##.##.....##.########..########.##....##

    - name: HRDN-7220 (Check if one or more compilers are installed)
      file:
        path: /usr/bin/as
        state: absent
      tags:
        - HRDN-7220


    # .##....##.########.########..##....##.########.##......
    # .##...##..##.......##.....##.###...##.##.......##......
    # .##..##...##.......##.....##.####..##.##.......##......
    # .#####....######...########..##.##.##.######...##......
    # .##..##...##.......##...##...##..####.##.......##......
    # .##...##..##.......##....##..##...###.##.......##......
    # .##....##.########.##.....##.##....##.########.########

    - name: KRNL-5820 - Core dump - ProcessSizeMax
      lineinfile:
        path: /etc/systemd/coredump.conf
        state: present
        regexp: "^#?ProcessSizeMax"
        line: "ProcessSizeMax=0"
      notify: restart sshd
      tags:
        - KRNL-5820

    - name: KRNL-5820 - Core dump - storage
      lineinfile:
        path: /etc/systemd/coredump.conf
        state: present
        regexp: "^#?Storage"
        line: "Storage=none"
      notify: restart sshd
      tags:
        - KRNL-5820

    - name: KRNL-5820 - Core dump - profile
      copy:
        dest: /etc/profile.d/KRNL-5820.sh
        content: |
          ulimit -c 0
        mode: 644
      notify: restart sshd
      tags:
        - KRNL-5820

    - name: KRNL-5820 - Core dump - limits
      copy:
        dest: /etc/security/limits.d/KRNL-5820.conf
        content: |
          #<domain> <type> <item> <value>
          *         hard   core   0
        mode: 644
      notify: restart sshd
      tags:
        - KRNL-5820

    #
    # net.ipv6.conf.default.accept_redirects and net.ipv4.conf.all.forwarding are not being set.
    #
    - name: KRNL-6000 (Check sysctl key pairs in scan profile)
      copy:
        dest: /etc/sysctl.d/90-lynis.conf
        content: |
          kernel.dmesg_restrict=1
          kernel.kptr_restrict=2
          kernel.sysrq=0
          kernel.yama.ptrace_scope=1
          net.ipv4.conf.all.accept_redirects=0
          net.ipv4.conf.all.forwarding=0
          net.ipv4.conf.all.log_martians=1
          net.ipv4.conf.all.rp_filter=1
          net.ipv4.conf.all.send_redirects=0
          net.ipv4.conf.default.accept_redirects=0
          net.ipv4.conf.default.log_martians=1
          net.ipv6.conf.all.accept_redirects=0
          net.ipv6.conf.default.accept_redirects=0
      notify: reboot system
      tags:
        KRNL-6000

    # ..######..##.....##.########.##.......##......
    # .##....##.##.....##.##.......##.......##......
    # .##.......##.....##.##.......##.......##......
    # ..######..#########.######...##.......##......
    # .......##.##.....##.##.......##.......##......
    # .##....##.##.....##.##.......##.......##......
    # ..######..##.....##.########.########.########

    - name: SHLL-6230 umask check - /etc/bashrc 002
      lineinfile:
        path: /etc/bashrc
        state: present
        regexp: "^       umask 002"
        line: "       umask 027"
      tags:
        - SHLL-6230

    - name: SHLL-6230 umask check - /etc/bashrc 022
      lineinfile:
        path: /etc/bashrc
        state: present
        regexp: "^       umask 022"
        line: "       umask 027"
      tags:
        - SHLL-6230

    - name: SHLL-6230 umask check - /etc/csh.cshrc 002
      lineinfile:
        path: /etc/csh.cshrc
        state: present
        regexp: "^    umask 002"
        line: "    umask 027"
      tags:
        - SHLL-6230

    - name: SHLL-6230 umask check - /etc/csh.cshrc 022
      lineinfile:
        path: /etc/csh.cshrc
        state: present
        regexp: "^    umask 022"
        line: "    umask 027"
      tags:
        - SHLL-6230

    - name: SHLL-6230 umask check - /etc/profile 002
      lineinfile:
        path: /etc/profile
        state: present
        regexp: "^    umask 002"
        line: "    umask 027"
      tags:
        - SHLL-6230

    - name: SHLL-6230 umask check - /etc/profile 022
      lineinfile:
        path: /etc/profile
        state: present
        regexp: "^    umask 022"
        line: "    umask 027"
      tags:
        - SHLL-6230

    # ..######..##....##.####.########.....########.########..######..########..######.
    # .##....##.##...##...##..##.....##.......##....##.......##....##....##....##....##
    # .##.......##..##....##..##.....##.......##....##.......##..........##....##......
    # ..######..#####.....##..########........##....######....######.....##.....######.
    # .......##.##..##....##..##..............##....##.............##....##..........##
    # .##....##.##...##...##..##..............##....##.......##....##....##....##....##
    # ..######..##....##.####.##..............##....########..######.....##.....######.

    - name: Copy default lynis profile
      copy:
        src: /etc/lynis/default.prf
        dest: /etc/lynis/custom.prf
        remote_src: true

    #
    # Centos does not have a /var/account directory. However,
    # we do load the audit package which tracks user actions.
    #
    - name: Skip ACCT-9622 (Check for available Linux accounting information)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=ACCT-9622"
        line: "skip-test=ACCT-9622"
      tags:
        ACCT-9622

    - name: Skip AUTH-9229 (Check password hashing methods)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=AUTH-9229"
        line: "skip-test=AUTH-9229"
      tags:
        AUTH-9229

    #
    # Changing how and where directories are mounted is beyond the scope of this
    # project. Ideally /tmp, /home, and /var should be on separate drives.
    #
    - name: Skip FILE-6310 (Checking /tmp, /home and /var directory)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=FILE-6310"
        line: "skip-test=FILE-6310"
      tags:
        FILE-6310

    # IPTABLES are beyond the scope of this project. I believe include
    # defense in depth. However,
    #
    # 1. Firewall rules are very application-specific.
    # 2. EC2 instances use security groups.
    #
    - name: Skip FIRE-4508 (Check used policies of iptables chains)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=FIRE-4508"
        line: "skip-test=FIRE-4508"
      tags:
        FIRE-4508

    #
    # malware scans are too environment specific for a generic
    # project like this to resolve.
    #
    - name: Skip HRDN-7230 (Check for malware scanner)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=HRDN-7230"
        line: "skip-test=HRDN-7230"
      tags:
        HRDN-7230

    # Checking for external logging is beyond the scope of this
    # project. There are simply too many ways to enable this
    # feature.
    #
    - name: Skip LOGG-2154 (Checking syslog configuration file)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=LOGG-2154"
        line: "skip-test=LOGG-2154"
      tags:
        LOGG-2154

    # Checking for anti-virus software is beyond the scope of this
    # project.
    #
    - name: Skip MALW-3280 (Check if anti-virus tool is installed)
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=MALW-3280"
        line: "skip-test=MALW-3280"
      tags:
        MALW-3280

    - name: Skip PKGS-7420 because servers will be terminated, not updated.
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=PKGS-7420"
        line: "skip-test=PKGS-7420"
      tags:
        - PKGS-7420

    #
    # SSH-7408 checks to see if the server runs SSH on something other
    # than 22 (the default port).
    #
    # Changing the port is a bit complex in an automated provision.
    #  - switch to terraform to generate custom security group.
    #  - connect via 22:
    #      - change the port number in /etc/ssh/sshd_config.
    #      - semanage port -a -t ssh_port_t -p tcp 15762
    #      - sudo systemctl restart sshd
    #  - change ansible and other scripts to use the new port number.
    #
    # All of that work is possible but should not be done on a whim.
    #
    - name: Skip SSH-7408 SSH non-default port
      lineinfile:
        path: /etc/lynis/custom.prf
        state: present
        regexp: "^skip-test=SSH-7408:Port"
        line: "skip-test=SSH-7408:Port"
      tags:
        - SSH-7408


    # ..######...######..##.....##
    # .##....##.##....##.##.....##
    # .##.......##.......##.....##
    # ..######...######..#########
    # .......##.......##.##.....##
    # .##....##.##....##.##.....##
    # ..######...######..##.....##

    - name: SSH-7408 - hardening SSH configuration - AllowAgentForwarding
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?AllowAgentForwarding"
        line: "AllowAgentForwarding no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - AllowTcpForwarding
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?AllowTcpForwarding"
        line: "AllowTcpForwarding no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - ClientAliveCountMax
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?ClientAliveCountMax"
        line: "ClientAliveCountMax 2"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - ClientAliveInterval
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?ClientAliveInterval"
        line: "ClientAliveInterval 300"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - Compression
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?Compression"
        line: "Compression no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - INFO
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?LogLevel"
        line: "LogLevel VERBOSE"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - MaxAuthTries
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?MaxAuthTries"
        line: "MaxAuthTries 3"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - MaxSessions
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?MaxSessions"
        line: "MaxSessions 2"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - PermitRootLogin
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?PermitRootLogin"
        line: "PermitRootLogin no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    # - name: SSH-7408 - hardening SSH configuration - Port
    #   lineinfile:
    #     path: /etc/ssh/sshd_config
    #     state: present
    #     regexp: "^#?Port"
    #     line: "Port {{ ssh_port }}"
    #     validate: sshd -tf %s
    #   notify: restart sshd
    #   tags:
    #     - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - TCPKeepAlive
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?TCPKeepAlive"
        line: "TCPKeepAlive no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - UseDNS
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?UseDNS"
        line: "UseDNS no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7408 - hardening SSH configuration - X11Forwarding
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?X11Forwarding"
        line: "X11Forwarding no"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7408

    - name: SSH-7440 (Check OpenSSH option AllowUsers and AllowGroups)
      lineinfile:
        path: /etc/ssh/sshd_config
        state: present
        regexp: "^#?AllowUsers"
        line: "AllowUsers {{ ssh_user }}"
        validate: sshd -tf %s
      notify: restart sshd
      tags:
        - SSH-7440


    # ..######..########..#######..########.....###.....######...########
    # .##....##....##....##.....##.##.....##...##.##...##....##..##......
    # .##..........##....##.....##.##.....##..##...##..##........##......
    # ..######.....##....##.....##.########..##.....##.##...####.######..
    # .......##....##....##.....##.##...##...#########.##....##..##......
    # .##....##....##....##.....##.##....##..##.....##.##....##..##......
    # ..######.....##.....#######..##.....##.##.....##..######...########

    - name: STRG-1846 - Check if firewire storage is disabled
      copy:
        dest: /etc/modprobe.d/firewire.conf
        content: |
          blacklist firewire-core
      tags:
        - STRG-1846


    # .########..#######...#######..##......
    # ....##....##.....##.##.....##.##......
    # ....##....##.....##.##.....##.##......
    # ....##....##.....##.##.....##.##......
    # ....##....##.....##.##.....##.##......
    # ....##....##.....##.##.....##.##......
    # ....##.....#######...#######..########

    - name: TOOL-5104 Fail2ban - create jail
      copy:
        dest: /etc/fail2ban/jail.local
        content: |
          [DEFAULT]
          bantime  = 1800
          findtime  = 300
          maxretry = 3
          banaction = iptables-multiport
          backend = systemd

          [sshd]
          enabled = true
      tags:
        - TOOL-5104

    - name: TOOL-5104 Fail2ban - start and enable
      systemd:
        daemon_reload: yes
        enabled: yes
        masked: no
        name: fail2ban
        state: started


    # .##.....##..######..########.
    # .##.....##.##....##.##.....##
    # .##.....##.##.......##.....##
    # .##.....##..######..########.
    # .##.....##.......##.##.....##
    # .##.....##.##....##.##.....##
    # ..#######...######..########.

    - name: USB-1000 (Check if USB storage is disabled)
      copy:
        dest: /etc/modprobe.d/lynis-usb-storage-blacklist.conf
        content: |
          install usb-storage /bin/true
      tags:
        - USB-1000

    - name: USB-3000 (Check for presence of USBGuard)
      lineinfile:
        path: /etc/usbguard/usbguard-daemon.conf
        state: present
        regexp: "^PresentControllerPolicy="
        line: "PresentControllerPolicy=apply-policy"
      tags:
        - USB-3000


    # .########..########.########...#######...#######..########
    # .##.....##.##.......##.....##.##.....##.##.....##....##...
    # .##.....##.##.......##.....##.##.....##.##.....##....##...
    # .########..######...########..##.....##.##.....##....##...
    # .##...##...##.......##.....##.##.....##.##.....##....##...
    # .##....##..##.......##.....##.##.....##.##.....##....##...
    # .##.....##.########.########...#######...#######.....##...

    #
    # Lots of changes were made. Let's reboot to make sure
    # everything takes effect.
    #

    - name: Unconditionally reboot the machine
      reboot:
```

* Run the Ansible playbook.

```bash
./run-stig-playbook.sh
```

The log file is `/var/log/lynis.log`. It should show a hardening index of 100. Please review the skipped tests. And the suggestions.
