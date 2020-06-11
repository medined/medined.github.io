---
layout: post
title: Using Eureka REST API For Python Service
author: David Medinets
categories: eureka rest python
year: 2020
theme: python
---

This article shows how to interact with Eureka using its REST API. I provide fully working
examples.

Eureka has a REST API that allows non-java technologies (like Python) to interact with it. We'll use `curl` to make the REST calls. Of course, the calls can be replicated in any language.

Eureka doesn't care if a service is actually running. It only cares about the information being sent to it. Therefore, this directory will use FAKE_SERVICE as its service name.

The first step is to start a Eureka server. You can do this any way you'd like. The easiest way, if you have `docker` installed is to run the following command. See https://hub.docker.com/repository/docker/medined/eureka-server if you want more information about the image.

```bash
docker run \
  --name eureka-server \
  --rm=true \
  --detach \
  --publish 8761:8761 \
  medined/eureka-server:2.3.0.RELEASE
```

## Available Endpoints

What can you do with the REST API? The list is below. This list is pulled directly from the official documentation so you don't need to get reference it.

* Register application
* De-register application
* Send heartbeat
* Query for all instances
* Query for all appID instances
* Query for a specific appID/instanceID
* Query for a specific instanceID
* Take instance out of service
* Move instance back into service (remove override)
* Update metadata
* Query for all instances under a particular vip address
* Query for all instances under a particular secure vip address

Examples about how to perform each of these functions are below.

# Service Registration

Save the script below as `service-register.sh` script. Then update the configuration information if needed.

```bash
#!/bin/bash

# $1 is used as the host name.

EUREKA_HOST="localhost"
EUREKA_PORT="8761"
EUREKA_URI="http://$EUREKA_HOST:$EUREKA_PORT"

SERVICE_NAME="FAKE-SERVICE"
SERVICE_PROTOCOL="http"
SERVICE_HOST="fake.com"
SERVICE_PORT="5000"

SERVICE_URI="$SERVICE_PROTOCOL://$SERVICE_HOST:$SERVICE_PORT"
HOME_URI="$SERVICE_URI/home"
HEALTH_URI="$SERVICE_URI/health"

# This is the URL shown in the "status" field in the
# instances section of the eureka dashboard.
#
# It's up to you to decide what the URL points to. Some
# information or status endpoint might be good.
STATUS_URI="$SERVICE_URI/health"

# This is the name displayed to the right of the status
# on the eureka dashbard. If the app (FAKE_SERVICE) is
# registered with more than one hostname, they will be
# displayed as a comma-separated list. This hostname
# is part of the heartbeat message.
#
# If you'll have more than one host per service,
# make sure they have different host names.
HOST_NAME="${1:-fake01}"

# Everyone of these parameters seem to be required. I don't know
# anything about secureVipAddress and vipAddress.
#
# dataCenterInfo must have a name of "MyOwn" or "Amazon".
#
# status can be UP, DOWN, STARTING, OUT_OF_SERVICE, UNKNOWN.
#   if the registration status is STARTING, then the service
#   will never be evicted. Also, simply sending a Heartbeat
#   does not change the status.
#
# The metadata fields can be any information you want associated
# with a service. I recommend keeping it short.
#

cat <<EOF > /tmp/json.json
{
  "instance": {
    "app": "$SERVICE_NAME",
    "dataCenterInfo": {
      "@class": "com.netflix.appinfo.MyDataCenterInfo",
      "name": "MyOwn"
    },
    "healthCheckUrl": "$HEALTH_URI",
    "homePageUrl": "$HOME_URI",
    "hostName": "$HOST_NAME",
    "ipAddr": "$SERVICE_HOST",
    "leaseInfo": {
      "evictionDurationInSecs": 90
    },
    "metadata": {
      "owner": "George Harris",
      "cost-code": "1D234R"
    },
    "port": {
      "\$": 5000,
      "@enabled": "true"
    },
    "securePort": {
      "\$": 443,
      "@enabled": "false"
    },
    "secureVipAddress": null,
    "status": "UP",
    "statusPageUrl": "$STATUS_URI",
    "vipAddress": null
  }
}
EOF


curl \
  --header "content-type: application/json" \
  --data-binary @/tmp/json.json \
  $EUREKA_URI/eureka/apps/$SERVICE_NAME
```

When registering a service, you can specify a status (UP, DOWN, STARTING,
OUT_OF_SERVICE, and UNKNOWN). If you specify, STARTING the service will not
be evicted if no heartbeat is received. Also, sending a heartbeat does not
change the status to UP.

`hostname` and `instance` can be thought of as synonmous as far as Eureka
is concerned.

# Service Information

You can request information about registered services using the following URL:

```bash
curl http://localhost:9000/eureka/apps
```

Then you can narrow down to a specific service:

```bash
curl http://localhost:9000/eureka/apps/FAKE-SERVICE
```

And finally, you can filter down to a specific instance or hostname. In the URL
below, `fake01` is the host name (AKA the instance).

```bash
curl http://localhost:9000/eureka/apps/FAKE-SERVICE/fake01
<instance>
  <hostName>fake01</hostName>
  <app>FAKE-SERVICE</app>
  <ipAddr>fake.com</ipAddr>
  <status>UP</status>
  <overriddenstatus>UNKNOWN</overriddenstatus>
  <port enabled="true">5000</port>
  <securePort enabled="false">443</securePort>
  <countryId>1</countryId>
  <dataCenterInfo class="com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo">
    <name>MyOwn</name>
  </dataCenterInfo>
  <leaseInfo>
    <renewalIntervalInSecs>30</renewalIntervalInSecs>
    <durationInSecs>90</durationInSecs>
    <registrationTimestamp>1591812947850</registrationTimestamp>
    <lastRenewalTimestamp>1591813378305</lastRenewalTimestamp>
    <evictionTimestamp>0</evictionTimestamp>
    <serviceUpTimestamp>1591812947341</serviceUpTimestamp>
  </leaseInfo>
  <metadata>
    <owner>George Harris</owner>
    <cost-code>1D234R</cost-code>
  </metadata>
  <homePageUrl>http://fake.com:5000/home</homePageUrl>
  <statusPageUrl>http://fake.com:5000/health</statusPageUrl>
  <healthCheckUrl>http://fake.com:5000/health</healthCheckUrl>
  <isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>
  <lastUpdatedTimestamp>1591812947850</lastUpdatedTimestamp>
  <lastDirtyTimestamp>1591812947341</lastDirtyTimestamp>
  <actionType>ADDED</actionType>
</instance>
```

# Service Heartbeat

NOTE: Sending a heartbeat simply means that a service is not evicted. It
does not change a service's status.

The `service-heartbeat.sh` script can be used to send a heartbeat for our
fake service. It's function is quite simpe. It PUTs a request to the
following URL.

```
http://localhost:9000/eureka/apps/FAKE-SERVICE/fake01
```

Notice that no parameters are needed. This contradicts the examples on the
web but not the official documentation. I've seen examples showing that
`status` or `lastDirtyTimestamp` should be specified. They don't.

Heartbeat messages should be sent every 30 seconds.

When a service is evicted, you might see a message like the following in the
logs.

```
Evicting 1 items (expired=1, evictionLimit=1)
DS: Registry: expired lease for FAKE-SERVICE/fake01
```

# Service Deregistration

Deregistering a service is quite simple. Make a request like the following:

```bash
curl -X DELETE http://localhost:9000/eureka/apps/FAKE-SERVICE/fake01
```

# Query for a specific instanceID

Although we're not using Instance IDs, the URL will look like this:

```bash
curl http://localhost:9000/eureka/instances/fake01
```

# Service Status Change (overriddenstatus)

Using a curl command like that below, you can change the status of a service.
As far as I know, there is no security mechanism stopping a malicious actor
from arbitrarily changing status on a production system. Therefore, network
controls (like security groups) must be used to secure access.

```bash
curl -X PUT http://localhost:9000/eureka/apps/FAKE-SERVICE/fake01/status?value=DOWN
```

The REST request above sets the `overriddenstatus` field of a service. This
status must be cleared using this kind of request. Make sure to provide the
optional status. Otherwise, the status becomes UNKNOWN and the service will
be shortly evicted.

```bash
curl -X DELETE http://localhost:9000/eureka/apps/FAKE-SERVICE/fake01/status?value=UP
```

# Service Metadata Update

The request below will add or change metadata associated with a service. There
is no security stopping a bad actor from changing the wrong key, value or even
changing the wrong service.

```bash
curl -X PUT http://localhost:9000/eureka/apps/FAKE-SERVICE/fake01/metadata?color=BLUE
```

## Research Links

* https://github.com/Netflix/eureka/wiki/Eureka-REST-operations
* https://dzone.com/articles/the-mystery-of-eureka-health-monitoring
* https://blog.asarkar.org/technical/netflix-eureka/
