#!/bin/bash

S3CMD_IMAGE_NAME=s3cmd
S3CMD_VERSION=latest
S3CMD_IMAGE="$S3CMD_IMAGE_NAME:$S3CMD_VERSION"
S3_CNAME=es-s3

function s3cmd-build-image {
  docker build -t $S3CMD_IMAGE .  
}

function s3cmd {
  docker run \
    -it \
    --link $S3_CNAME:s3 \
    --rm=true \
    --volume $(pwd)/s3cfg:/root/.s3cfg \
    --entrypoint /bin/bash \
    $S3CMD_IMAGE
}

function s3cmdx {
  docker run \
    --link $S3_CNAME:s3 \
    --rm=true \
    --volume $(pwd)/s3cfg:/root/.s3cfg \
    $S3CMD_IMAGE \
    $@
}

function reload {
  source bash-functions.sh
}

function bedit {
  vi bash-functions.sh
  reload
}

alias gs="git status"
echo "RELOADED"

