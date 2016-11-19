#!/bin/bash

# http://ewanvalentine.io/writing-and-running-go-apis-in-docker/
# https://blog.codeship.com/building-minimal-docker-containers-for-go-applications/
# https://blog.docker.com/2016/09/docker-golang/
# https://www.iron.io/the-easiest-way-to-develop-with-go%E2%80%8A-%E2%80%8Aintroducing-a-docker-based-go-tool/
# https://medium.com/@kelseyhightower/optimizing-docker-images-for-static-binaries-b5696e26eb07#.74p4i2fkw
# https://www.socketloop.com/tutorials/golang-convert-csv-data-to-json-format-and-save-to-file

GO_IMAGE_NAME=golang
GO_VERSION=1.7.3
GO_IMAGE="$GO_IMAGE_NAME:$GO_VERSION"

function go {
  docker run \
    --rm \
    --volume $(pwd)/app:/go \
    $GO_IMAGE \
    go $@
}

function b {
  go build read-csv.go
}

function r {
  app/read-csv
}

function e {
  vi app/read-csv.go
}

function go-one-line-pull-and-run-project {
  docker run \
    --rm \
    $GO_IMAGE \
    sh -c "go get github.com/golang/example/hello/... && exec hello"
}

function go-get-binary-from-build-container {
  rm -rf /tmp/go-binaries
  mkdir -p /tmp/go-binaries
  docker run \
    -v /tmp/go-binaries:/go/bin \
    $GO_IMAGE \
    go get github.com/golang/example/hello/...
  echo "The build binaries are in /tmp/go-binaries:"
  ls -lh /tmp/go-binaries
}

function go-install-binary-to-path {
  docker run \
    -v /usr/local/bin:/go/bin \
    $GO_IMAGE \
    go get github.com/golang/example/hello/...
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

