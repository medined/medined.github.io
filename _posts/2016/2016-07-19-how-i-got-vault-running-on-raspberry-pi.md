---
layout: post
title: How I Got Hashicorp Vault to Run on Raspberry PI
categories: hashicorp raspberry-pi picocluster vault
year: 2016
---

## Notes for Research

* https://www.katacoda.com/courses/docker-production/vault-secrets
* https://github.com/csawyerYumaed/vault-docker
* https://github.com/aerofs/gockerize
* https://github.com/CenturyLinkLabs/golang-builder
* https://gist.github.com/voxxit/dd6f95398c1bdc9f1038
* https://github.com/calavera/docker-volume-keywhiz
* https://github.com/defunctzombie/docket
* https://github.com/ehazlett/docker-volume-libsecret
* https://github.com/AngryBytes/docker-surgery
* https://www.vaultproject.io/intro/getting-started/apis.html

## References

* http://elasticcompute.io/2016/01/21/runtime-secrets-with-docker-containers/


## Content

These steps result in Hashicorp Vault running in development mode. Do NOT use in production without proper research.


All of these steps are done on a Raspberry PI.

* Connect to the RPI

```
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi1.local
```

* Create a project directory

```
mkdir -p projects/armhf-vault
```

* Connect to the project directory

```
cd projects/armhf-vault
```

* Create a Dockerfile

```
cat << EOF > Dockerfile
FROM container4armhf/armhf-alpine:3.4

MAINTAINER Christopher 'Chief' Najewicz <chief@beefdisciple.com>

ENV version=0.6.0
ENV os=linux
ENV arch=arm

ADD https://releases.hashicorp.com/vault/${version}/vault_${version}_${os}_${arch}.zip /tmp/vault.zip

RUN \
  cd /bin &&\
  unzip /tmp/vault.zip &&\
  chmod +x /bin/vault &&\
  rm /tmp/vault.zip &&\
  apk update &&\
  apk add ca-certificates

EXPOSE 8200

VOLUME /etc/vault.hcl

ENTRYPOINT ["/bin/vault", "server"]

CMD ["-config=/etc/vault.hcl"]
EOF
```

* Create a vault configuration file.

```
cat << EOF > vault.hcl
listener "tcp" {
  address = "0.0.0.0:8200"
  tls_disable = 1
}
EOF
```

* Create a script to build the image.

```
cat << EOF > build-image.sh
docker build -t medined/armhf-vault .
EOF
```

* Create a script to run the image.

```
cat << EOF > run-image.sh
docker run \
    --rm \
    --name vault-dev \
    --memory-swap -1 \
    -v "vault.hcl:/etc/vault.hcl"
    -p 8200:8200 \
    medined/armhf-vault -dev
EOF
```

* Make the scripts executable.

```
chmod +x *.sh
```

* Build the image.

```
./build-image.sh
```

* Run the image.

```
./run-image.sh
```

* Open a new terminal and connect to the RPI

```
ssh -oStrictHostKeyChecking=no -oCheckHostIP=no pirate@pi1.local
```

* Run a shell inside the vault container

```
docker exec -it vault-dev /bin/sh
```

* Export the vault http address.

```
export VAULT_ADDR=http://127.0.0.1:8200
```

* Now you can use the vault command.

```
vault status
```

* Alternatively you can use wget

```
http://127.0.0.1:8200/v1/sys/seal-status
```
