---
layout: post
title: Environment Variables Available In AWS Lambda Go
author: David Medinets
categories: aws lambda golang
year: 2018
theme: golang
---

The short version of this story is simple. Below is a sorted list of the
environment variables available to an AWS function written in Go. Note that
the secret information is available. I've used asterisks for their value.

```
AWS_ACCESS_KEY: ********
AWS_ACCESS_KEY_ID: ********
AWS_DEFAULT_REGION: us-east-1
AWS_LAMBDA_FUNCTION_MEMORY_SIZ: 1024
AWS_LAMBDA_FUNCTION_NAME: odol-sango-dev-logenvironment
AWS_LAMBDA_FUNCTION_VERSION: $LATEST
AWS_LAMBDA_LOG_GROUP_NAME: /aws/lambda/odol-sango-dev-logenvironment
AWS_LAMBDA_LOG_STREAM_NAME: 2018/03/07/[$LATEST]2b67007c25944c85a09e4b06ae683867
AWS_REGION: us-east-1
AWS_SECRET_ACCESS_KEY: ********
AWS_SECRET_KEY: ********
AWS_SECURITY_TOKEN: ********
AWS_SESSION_TOKEN: ********
AWS_XRAY_CONTEXT_MISSING: LOG_ERROR
AWS_XRAY_DAEMON_ADDRESS: 169.254.79.2:2000
LAMBDA_RUNTIME_DIR: /var/runtime
LAMBDA_TASK_ROOT: /var/task
LANG: en_US.UTF-8
LD_LIBRARY_PATH: /lib64:/usr/lib64:/var/runtime:/var/runtime/lib:/var/task:/var/task/lib
PATH: /usr/local/bin:/usr/bin/:/bin
0TZ: :UTC
_AWS_XRAY_DAEMON_ADDRESS: 169.254.79.2
_AWS_XRAY_DAEMON_PORT: 2000
_HANDLER: bin/logenvironment
_LAMBDA_CONSOLE_SOCKET: 35
_LAMBDA_CONTROL_SOCKET: 28
_LAMBDA_LOG_FD: 45
_LAMBDA_RUNTIME_LOAD_TIME: 4653494624711
_LAMBDA_SB_ID: 16
_LAMBDA_SERVER_PORT: 61375
_LAMBDA_SHARED_MEM_FD: 12
_X_AMZN_TRACE_ID: Parent
```

The long version is more involved. I used the Serverless framework for this short
project. After installing the Serverless framework I created a project using the
Go template as follows:

```
cd $GOPATH/src
serverless create -t aws-go-dep -p odol-sango
cd odol-sango
make
serverless deploy
serverless invoke -f hello
```

Now that the basic project is working. Create a file called functions/logenvironment/logenvironment.go:

```
package main

import (
	"fmt"
	"os"
	"sort"
	"strings"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
)

func Handler(request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	var restricted [6]string
	restricted[0] = "AWS_ACCESS_KEY"
	restricted[1] = "AWS_ACCESS_KEY_ID"
	restricted[2] = "AWS_SECRET_ACCESS_KEY"
	restricted[3] = "AWS_SECRET_KEY"
	restricted[4] = "AWS_SECURITY_TOKEN"
	restricted[5] = "AWS_SESSION_TOKEN"

	envs := make(map[string]string)
	// Make a hash map of the environment variables.
	for _, e := range os.Environ() {
		pair := strings.Split(e, "=")
		var key_is_restricted = false
		for _, a := range restricted {
			if a == pair[0] {
				key_is_restricted = true
			}
		}
		if key_is_restricted {
			envs[pair[0]] = "********"
		} else {
			envs[pair[0]] = pair[1]
		}
	}

	// Sort the keys
	keys := make([]string, 0, len(envs))
	for key := range envs {
		keys = append(keys, key)
	}
	sort.Strings(keys)

	// Print the environment variables
	for _, k := range keys {
		fmt.Printf("%03.30s: %s\n", k, envs[k])
	}

	return events.APIGatewayProxyResponse{Body: "Environment Logged\n", StatusCode: 200}, nil
}

func main() {
	lambda.Start(Handler)
}
```

Update the Makefile:

```
build:
	dep ensure
	env GOOS=linux go build -ldflags="-s -w" -o bin/hello hello/main.go
	env GOOS=linux go build -ldflags="-s -w" -o bin/world world/main.go
	env GOOS=linux go build -ldflags="-s -w" -o bin/logenvironment functions/logenvironment/logenvironment.go
```

Update the serverless.yml file:

```
logenvironment:
  handler: bin/logenvironment
  events:
    - http:
        path: logenvironment
        method: get
```

Compile and deploy:

```
make && sls deploy
```

In one bash window, run the following command to watch the Lambda log messages:

```
serverless logs -f logenvironment -t
```

In another window, call the function using something like:

```
curl https://olkj0m5hag.execute-api.us-east-1.amazonaws.com/dev/logenvironment
```

Good Luck!
