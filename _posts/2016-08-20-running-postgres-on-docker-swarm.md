---
layout: post
title: Running PostgreSQL on Docker Swarm 1.12
author: David Medinets
categories: docker postgres
---

For this example, I am using a five node Docker Swarm cluster running on VirtualBox. At this start, I assume the swarm is running and that the swarm master is called pi1. 

NOTE: The name 'pi1' has no relevance. It can be anything. Just change the references to your name.

* See the swarm running.

```
eval $(docker-machine env pi1)
docker node ls
```

* Start the postgres service.

```
eval $(docker-machine env pi1)
docker service create \
  --env POSTGRES_DB=ckan \
  --env POSTGRES_USER=ckan \
  --env POSTGRES_PASSWORD=ckan \
  --publish 5432:5432 \
  --name postgres \
  postgres:9.5
```

* Run a command-line client from outside of the swarm.

```
eval $(docker-machine env --unset)
docker run \
  --add-host postgres:$(docker-machine ip pi1) \
  --env PGPASSWORD=ckan \
  -it \
  --rm \
  postgres:9.5 \
  psql -h postgres -U ckan
```

* Use the client.

Do whatever is needed at the postgres command-line and then type '\q' to exit.

* Stop the service.

```
eval $(docker-machine env pi1)
docker service rm postgres
eval $(docker-machine env --unset)
```
