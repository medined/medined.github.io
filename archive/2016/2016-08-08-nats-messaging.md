---
layout: post
title: NATS Messaging
categories: nats docker
year: 2016
theme: docker
---

This page consolidates information about the NATS messaging system.

## Introduction

A while back, we open-sourced our NATS messaging system. Today, we’re launching NATS.io where people can download NATS and various client libraries in one place as well as access documentation and support resources. We’re also announcing Apcera technical support for NATS and its client libraries.

So what's special about NATS?

shortest request-to-response time - Apcera’s core technology offering, Continuum, is like many other platforms, a sophisticated distributed system. This means it has different moving parts spread across physical and virtual machines that communicate and coordinate with each other. At runtime, these communications must be lightning fast, and should scale in a predictable fashion. For the shortest request-to-response time, you need multiple responders waiting and ready to jump on a request while others may be busy. Available responders race to answer the request first. All this is the job of the messaging backplane called NATS within Continuum.

6 million messages per second - NATS was designed and built as a next-generation messaging system that supports modern cloud and distributed systems. Unlike traditional enterprise messaging systems, it works more like a nervous system, with an always-on dial tone that will do whatever it takes to ensure it remains available. Originally, NATS was written in Ruby and could send ~150k messages per second. That Ruby version has since been deprecated and Apcera rewrote the server and client in the Go language. This updated Go-based server, gnatsd, can process up to 6 million messages per second, all on a commodity laptop. Just another reason why we love Go.

## Links


### Main Site

* http://nats.io

### Demo

* telnet demo.nats.io 4222

### Docker Hub

* https://hub.docker.com/_/nats

### Configuration


### Context

* http://thenewstack.io/nats-rest-alternative-provides-messaging-distributed-systems/
* https://www.apcera.com/blog/introducing-natsio
* https://docs.cloudfoundry.org/concepts/architecture/messaging-nats.html
* https://github.com/fatihcode/natsboard
* https://github.com/nats-io/gnatsd


### Community

* http://nats.io/community
* https://github.com/sohlich/nats-proxy - HTTP requests to NATS.
