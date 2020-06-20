---
layout: post
title: Running OPENSCAP (OSCAP) on Fedora CoreOS
author: David Medinets
categories: aws fedora coreos ansible openscap oscap
year: 2020
theme: fedora-coreos
---

I am exploring how to create a Fedora CoreOS server that can pass as many security checks as possible. However, I am not a security guru. Make sure to vet anything you read here with your own experts.

This work is being done at the request of the Enterprise Container Working Group (ECWG) of the Office of Information and Technology (OIT - https://www.oit.va.gov/) at the Department of Veteran Affairs.

## Goal

Run `oscap` on a Fedora CoreOS server.

## Definitions

[**Fedora CoreOS**](https://getfedora.org/coreos) is an automatically-updating, minimal operating system for running containerized workloads securely and at scale. However, we'll probably be replacing virtual servers instead of updating them.

[**OpenSCAP**](https://www.open-scap.org) is an ecosystem providing multiple tools to assist administrators and auditors with assessment, measurement, and enforcement of security baselines. SCAP or Security Content Automation Protocol is a U.S. standard maintained by National Institute of Standards and Technology (NIST).

[**rpm-ostree**](https://rpm-ostree.readthedocs.io/en/latest/) - rpm-ostree is a hybrid image/package system. It uses libOSTree as a base image format, and accepts RPM on both the client and server side, sharing code with the dnf project

[**XCCDF**](https://csrc.nist.gov/projects/security-content-automation-protocol/scap-specifications/xccdf) - XCCDF is a specification language for writing security checklists, benchmarks, and related kinds of documents. An XCCDF document represents a structured collection of security configuration rules for some set of target systems.

## Installation

The first step is to install the software.

```bash
sudo rpm-ostree install openscap-scanner scap-security-guide zip
sudo reboot
```

Of note is that the installation provides the following files.

```bash
$ ls -c1  /usr/share/xml/scap/ssg/content/*fedora*.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-cpe-dictionary.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-cpe-oval.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-ds-1.2.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-ds.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-ocil.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-oval.xml
/usr/share/xml/scap/ssg/content/ssg-fedora-xccdf.xml
```

I don't yet know how they are used, but the XCCDF file looks promising. We can find more information. The profiles are relevant, I think.

```bash
$ oscap info /usr/share/xml/scap/ssg/content/ssg-fedora-xccdf.xml
Document type: XCCDF Checklist
Checklist version: 1.1
Imported: 1970-01-01T00:00:00
Status: draft
Generated: 2020-06-03
Resolved: true
Profiles:
	Title: OSPP - Protection Profile for General Purpose Operating Systems
		Id: ospp
	Title: PCI-DSS v3 Control Baseline for Fedora
		Id: pci-dss
	Title: Standard System Security Profile for Fedora
		Id: standard
Referenced check files:
	ssg-fedora-oval.xml
		system: http://oval.mitre.org/XMLSchema/oval-definitions-5
	ssg-fedora-ocil.xml
		system: http://scap.nist.gov/schema/ocil/2
```

## Run Evaluation

Let's use the `standard` profile to run an evaluation. I change the ownership of the result files so that I can `scp` them to my local workstation.

```bash
sudo oscap xccdf eval \
    --profile standard \
    --fetch-remote-resources \
    --report xccdf-report.html \
    --results ssg-fedora-xccdf-results.xml \
    /usr/share/xml/scap/ssg/content/ssg-fedora-xccdf.xml

sudo chown core:core xccdf-report.html ssg-fedora-xccdf-results.xml
```

Open the HTML file in a web broswer to view the results.

## Generate Fixes

`oscap` has a wonderful feature to suggest fixes. The following command does this. It does not need to be run as `root`. If you prefer a different fix-type, use `--help` to learn how.

```bash
oscap xccdf generate fix \
    --fix-type ansible \
    --output playbook-xccdf-fixes.yml \
    --profile standard \
    ssg-fedora-xccdf-results.xml
```

Right off the bat, I needed to add to the playbook.

```yaml
    become: yes

    vars:   
        ansible_python_interpreter: '/usr/bin/python3'
```

These changes allowed the playbook to get started but it wasn't long before the first task failure. That's tale for another time.

## Generate Guide

```bash
oscap xccdf generate guide \
    --profile standard \
    --output xccdf-guide.html \
    ssg-fedora-xccdf-results.xml
```
