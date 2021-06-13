---
layout: post
title: Use Kubernetes init container to Git Clone and provide mounted directory
author: David Medinets
categories: kubernetes git
year: 2020
theme: kubernetes
---

## Acknowledgements

I took this idea from the TGI Kubernetes 39 video (https://www.youtube.com/watch?time_continue=3432&v=xYMA-S75_9U)

## Process

Inside your pod manifest, define an init container like this:

```yaml
template:
  spec:
    initContainers:
    - name: download-theme
      image: alpine-git
      command:
      - git
      - clone
      - https://github.com/soggiest/heptio-dex.git
      - /theme
      volumeMounts:
      - name: theme
        mountPath: /theme/
```

Some work needs to be done to make this work but the idea is sound.
