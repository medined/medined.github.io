---
layout: post
title: Unable to Connect with AWS Managed Elasticseach - SOLVED
author: David Medinets
categories: aws elasticsearch curl cloudformation
year: 2017
theme: aws
---

TR;DR - You don't need to specify a port number when connecting to the AWS
managed Elasticsearch service.

That was an easy issue to resolve. While you're here, let me show you the
CloudFormation template that I am using to create a three-node AWS-managed
Elasticsearch cluster.

```
AWSTemplateFormatVersion: 2010-09-09
Description: Elasticsearch Stack

Parameters:
  pAwsAccountId:
    Type: String
    Default: 929717587211
    Description: AWS Account Id
  pSourceIP:
    Type: String
    Default: 34.201.116.224
    Description: IP of the server accessing Elasticsearch

Resources:
  rElasticsearch:
    Type: AWS::Elasticsearch::Domain
    Properties:
      AccessPolicies:
        Version: 2012-10-17
        Statement:
          -
            Effect: Allow
            Principal:
              AWS: "*"
            Action: "es:*"
            Condition:
              IpAddress:
                aws:SourceIp:
                  - !Ref pSourceIP
            Resource: !Join [ "", [ "arn:aws:es:", !Ref "AWS::Region", ":", !Ref "AWS::AccountId", ":domain/", !Ref "AWS::StackName", "/*"  ] ]
      AdvancedOptions:
        rest.action.multi.allow_explicit_index: true
        indices.fielddata.cache.size: ""
      DomainName: !Ref "AWS::StackName"
      EBSOptions:
        EBSEnabled: true
        Iops: 0
        VolumeSize: 20
        VolumeType: gp2
      ElasticsearchClusterConfig:
        InstanceCount: 3
        InstanceType: t2.medium.elasticsearch
      ElasticsearchVersion: 5.1
      Tags:
        - Key: Name
          Value: !Join [ ":", [ !Ref "AWS::StackName", elasticsearch ] ]

Outputs:
  oElasticsearchId:
    Description: the id of elasticsearch
    Value: !Ref rElasticsearch
    Export:
      Name: !Join [ ":", [ !Ref "AWS::StackName", elasticsearch-id ] ]
  oElasticsearchArn:
    Description: the arn of elasticsearch
    Value: !GetAtt rElasticsearch.DomainArn
    Export:
      Name: !Join [ ":", [ !Ref "AWS::StackName", elasticsearch-arn ] ]
  oElasticsearchEndpoint:
    Description: the endpoint of elasticsearch
    Value: !GetAtt rElasticsearch.DomainEndpoint
    Export:
      Name: !Join [ ":", [ !Ref "AWS::StackName", elasticsearch-endpoint ] ]
```

As soon as the cluster is in the active state, I can use curl (from the source
ip server) to access it.

Get the Endpoint from the Elasticseach console.

```
export ES_ENDPOINT=https://search-app-05-elasticsearch-stack-foy7lzlceajw4walf5yape6iru.us-east-1.es.amazonaws.com
curl -XGET $ES_ENDPOINT/_cat/indices?v
```
