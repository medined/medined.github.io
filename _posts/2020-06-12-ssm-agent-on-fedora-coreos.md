---
layout: post
title: Add AWS SSM Agent on Fedora CoreOS
author: David Medinets
categories: aws fedora coros ansible
year: 2020
theme: ansible
---

This article shows how to install the AWS SSM Agent on Fedora CoreOS. My goal was to alert when 'denied' messages appear in audit logs. My first step towards this goal was to install the SSM agent to allow the server's `/var/log/audit/audit.log` file to appear in the CloudWatch Logs console.

After I did this work, I realized that I should have started with the CloudWatch agent. Ah well.

The first step is entirely in your hands. Clone https://github.com/aws/amazon-ssm-agent.git and build the binary files. Whatever directory you use will become the `ssm_binary_dir` in the ansible command.

The `ansible-playbook` command looks like this. `core` is the ssh user for Fedora CoreOS.

```bash
python3 ansible-playbook \
  --extra-vars ssm_binary_dir=/data/projects/amazon-ssm-agent/bin \
  -i inventory \
  -u core \
  playbook.harden.yml
```

Define an `inventory` file with the IP address of the remote server.

```bash
cat <<EOF > inventory
[fcos]
3.235.161.106
EOF
```

I add the PEM file to SSH to simplify my life. I learned that having PEM files in `~/.ssh` slows the connection process and can cause trouble if there are too many. That's I am using my `Downloads` directory.

```bash
ssh-add /home/medined/Downloads/ec2-key-pair.pem
```

Now here is the playbook.

```bash
cat <<EOF > playbook.aws-ssm-agent.yml
---
- hosts: fcos
  gather_facts: false

  vars:
    ansible_python_interpreter: '/usr/bin/python3'

  tasks:

  # ##########
  # # Amazon SSM Agent
  # ##########

  - name: Copy Amazon SSM Agent
    become: yes
    copy:
      src: "{{item}}"
      dest: /usr/local/bin
      mode: 755
      force: no
    with_fileglob:
      - "{{ssm_binary_dir}}/linux_amd64/*"

  - name: Make logging directory
    become: yes
    file:
      path: /var/log/amazon/ssm
      state: directory

  - name: Make config directory
    become: yes
    file:
      path: /etc/amazon/ssm
      state: directory

  - name: Copy Amazon SSM Agent JSON
    become: yes
    copy:
      src: "{{ssm_binary_dir}}/amazon-ssm-agent.json.template"
      dest: /etc/amazon/ssm/amazon-ssm-agent.json

  - name: Copy Amazon SSM Agent JSON
    become: yes
    copy:
      src: "{{ssm_binary_dir}}/seelog_unix.xml"
      dest: /etc/amazon/ssm

  - name: Create SSM service file.
    become: yes
    copy:
      dest: /etc/systemd/system/amazon-ssm-agent.service
      content: |
        [Unit]
        Description=amazon-ssm-agent
        [Service]
        Type=simple
        WorkingDirectory=/usr/local/bin
        ExecStart=/usr/local/bin/amazon-ssm-agent
        KillMode=process
        Restart=on-failure
        RestartSec=15min
        [Install]
        WantedBy=network-online.target

  - name: Enable SSM service
    become: yes
    service:
      name: amazon-ssm-agent
      enabled: yes
      state: started
EOF
```

You now have all of the pieces to deploy the AWS SSM Agent on Fedora CoreOS.
