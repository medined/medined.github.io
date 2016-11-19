#!/bin/bash

docker run -d elasticsearch -Des.node.name="TestNode"

#  es-get-stats   curl http://127.0.0.1/_nodes/stats/http?pretty
#  es-insert-document
#  es-insert-document
#  es-load-test-data
#  es-run
#  es-run-detached
#  es-run-detached
#  es-search
#  es-search
#  es-stack-start

function es-run {
  echo "TBD"
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

