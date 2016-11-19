#!/bin/bash

ES_CNAME=es-app
ES_IMAGE_NAME=elasticsearch
ES_NODE_NAME="MyNode"
ES_VERSION=5.0.1
NGINX_CNAME=es-nginx
NGINX_IMAGE_NODE=nginx
NGINX_PORT=9200
NGINX_VERSION=1.11.5

VOL_ES_DATA=/root/projects/medined.github.io/projects/es/esdata
VOL_NGINX_CONF=/root/projects/medined.github.io/projects/es/nginx.conf
VOL_NGINX_PASSWORDS=/root/projects/medined.github.io/projects/es/passwords

source set-authentication.sh

# Derived 
ES_IMAGE="$ES_IMAGE_NAME:$ES_VERSION"
NGINX_IMAGE="$NGINX_IMAGE_NODE:$NGINX_VERSION"

#  es-insert-document
#  es-load-test-data
#  es-search
#  es-stack-start
#  es-stack-stop
#  getip

function es-get-stats {
  curl --silent -u $ES_USER:$ES_PASSWORD http://localhost:$NGINX_PORT/_nodes/stats/http?pretty
}

function nginx-run-detached {
  docker stop $NGINX_CNAME
  docker rm $NGINX_CNAME
  docker run \
    --detach \
    --name $NGINX_CNAME \
    --link $ES_CNAME:es \
    --publish $NGINX_PORT:80 \
    --volume $VOL_NGINX_PASSWORDS:/elasticsearch/passwords:ro \
    --volume $VOL_NGINX_CONF:/etc/nginx/nginx.conf:ro \
    $NGINX_IMAGE
}

function es-run-detached {
  docker stop $ES_CNAME
  docker rm $ES_CNAME

  docker run \
    --detach \
    --name $ES_CNAME \
    --volume $VOL_ES_DATA:/usr/share/elasticsearch/data \
    $ES_IMAGE \
    -Enode.name="$ES_NODE_NAME"
}

function docker-get-container-ip {
  CNAME=$1
  if [ -z $CNAME ]; then
    echo "Please specify container name."
  else
    docker inspect --format "{{.NetworkSettings.IPAddress}}" $CNAME
  fi
}

function util-get-jq {
  curl --silent -L -O https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64
  mv jq-linux64 jq
  chmod +x jq
  sudo mv jq /usr/local/bin
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

