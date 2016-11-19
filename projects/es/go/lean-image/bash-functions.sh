#!/bin/bash

# https://blog.docker.com/2016/09/docker-golang/

GO_IMAGE_NAME=golang
GO_VERSION=1.7.3
GO_IMAGE="$GO_IMAGE_NAME:$GO_VERSION"

function go-build-image {
  # create binary inside container
  docker run \
    --rm \
    --volume $(pwd):/go/bin \
    $GO_IMAGE \
    go get github.com/golang/example/hello/...

  # build container
  docker build -t hello .
}

function go-run {
  docker run hello
}

function reload {
  source bash-functions.sh
}

function bedit {
  vi bash-functions.sh
  reload
}

alias gs="git stash"
echo "RELOADED"

