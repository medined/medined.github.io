---
layout: post
title: Generate Obsidan Page From Browser Bookmarks
author: David Medinets
categories: LLM, Python
year: 2024
theme: LLM
---

## Goal

To gain a searchable set of pages that represent the 
bookmarks that I have collected over the years.

## Introduction

I had over 100 bookmarks and had no idea what most of them
were. I haven't looked at them, ever. But I  do use Obsidian 
to organize research topics and write. I thought it would be
useful if I could turn those unused bookmarks into searchable page.

Each page would include at least a summary and categories.

This script was created for my own use. Therefore there is not a lot of documentation. If you need help with anything, 
please create an issue.

The script ignores URLS that contain: ['docs.google', 'reddit', 'slack', 'twitter']. Each one would require a different
approach and I did not need them. If you do, create an issue to discuss your needs or create a pull request with your 
approach.

# GitHub Link

https://github.com/medined/bookish-fortnight - see example Obsidan pages here.

## Ollama

I wanted a process that ran locally so that no cost was incurred. In order to enable this, I used `Ollama` and the `solar` LLM. I tried other large language models but `solar`
seemed to produce the best results.

You need to have Ollama installed locally as well as runnning inside the container. The Ollama inside the container is a server responding to the code's API calls.

## Docker

I used a container to run Ollama. Since I already 
had docker installed, this seemed like the best approach.

## Process

See the readme in the project.

