---
layout: post
title: List Deleteable Security Groups
author: David Medinets
categories: aws security
year: 2017
---

I inherited an AWS VPC that grew organically. This meant about 150 security
groups with no clear way to know which ones are important. Until I learned about
the `describe_network_interfaces` function, creating a report about security
group usage seemed like too much work.

Even with knowledge of that function, this bit of python was more work than I
thought. Therefore, I'm sharing it.

```
import boto3
security_groups = find_deleteable_security_groups(boto3.client('config'), boto3.client('cloudformation'))
for security_group in security_groups:
  print(security_group)

def find_deleteable_security_groups(ec2Client, cfmClient):
  # Will hold security group information.
  security_groups = {}
  # Will hold network interface information.
  network_interfaces = {}
  # Will hold backward references of security group. For example, securiy group A
  # might have no assets but still be referenced by security group B.
  backward_references = {}

  for security_group in ec2Client.describe_security_groups()['SecurityGroups']:
      sg_id = security_group['GroupId']
      network_interfaces[sg_id] = ec2Client.describe_network_interfaces(Filters=[{'Name': 'group-id', 'Values': [ sg_id ] }])['NetworkInterfaces']
      security_group['referenced_groups'] = {}
      for permission in security_group['IpPermissions']:
          userid_group_pairs = permission['UserIdGroupPairs']
          for group in userid_group_pairs:
              group_id = group['GroupId']
              security_group['referenced_groups'][group_id] = 1
              if group_id not in backward_references:
                  backward_references[group_id] = {}
              backward_references[group_id][sg_id] = 1
      security_groups[sg_id] = security_group

  results = {}
  for sg_id in sorted(security_groups):
      network_interface = network_interfaces[sg_id]
      interface_count = len(network_interface)
      if security_groups[sg_id]['GroupName'] == 'default':
          results[sg_id] = { 'Count' : interface_count,  'Message' : 'Default' }
      else:
          printed = False
          sg = security_groups[sg_id]
          referenced_group_count = len(sg['referenced_groups'])

          backward_reference_count = 0
          if sg_id in backward_references:
              backward_reference_count = len(backward_references[sg_id])

          if interface_count > 0:
              results[sg_id] = { 'Count' : interface_count }
              printed = True
          else:
              response = ec2Client.describe_tags(Filters=[ { 'Name': 'resource-id', 'Values': [ sg_id ] }, { 'Name': 'key', 'Values': [ 'aws:cloudformation:stack-id' ] } ])
              if 'Tags' in response:
                  if len(response['Tags']) > 0:
                      components = response['Tags'][0]['Value'].split('/')
                      stack_name = components[1]
                      stack_id = cfmClient.Stack(stack_name).stack_id
                      results[sg_id] = { 'Count' : interface_count, 'StackName' : stack_name, 'StackId' : stack_id }
                      printed = True

          if printed == False and referenced_group_count > 0:
              results[sg_id] = { 'Count' : interface_count, 'References' : sg['referenced_groups'] }
              printed = True

          if printed == False and backward_reference_count > 0:
              results[sg_id] = { 'Count' : interface_count, 'ReferencedBy' : backward_references[sg_id] }
              printed = True

          if printed == False:
              results[sg_id] = { 'Count' : interface_count, 'Message' : 'Deleteable' }

  return results
```
