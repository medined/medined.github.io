---
layout: post
title: Using Boto3 To Create NS Hosted Zone Record Set
author: David Medinets
categories:
  - "[[python]]"
  - "[[aws]]"
  - "[[boto3]]"
  - "[[route53]]"
year: 2020
theme: aws
---
## Acknowledgements

This work is being done at the request of the Enterprise Container Working Group (ECWG) of the Office of Information and Technology (OIT - https://www.oit.va.gov/) at the Department of Veteran Affairs.

## Article

I wanted to create a qwerty.va-oit.cloud sub-domain using a set of four name servers.

```python
nameservers = ['ns-657.awsdns-18.net', 'ns-422.awsdns-52.com', 'ns-1995.awsdns-57.co.uk', 'ns-1469.awsdns-55.org']
```

I could not find example code how to do this. It's not terribly complicated but providing an example might safe someone time so I'll take a few minutes.

The first step is to add a period to the end of each nameserver to create an fully-qualified domain name (FQDN).

```python
nameservers = ['{}.'.format(nameserver) for nameserver in nameservers]
```

The example from the `boto3` documentation shows the following (which I have shortened):

```python
response = client.change_resource_record_sets(
    HostedZoneId='string',
    ChangeBatch={
        'Changes': [{
            'Action': 'UPSERT',
            'ResourceRecordSet': {
                'Name': 'string',
                'Type': 'NS',
                'TTL': 123,
                'ResourceRecords': [{'Value': 'string'},],
            }
        }]
    }
)
```

I tried various ways to get the nameserver list into that `string` associated with `value`. However, it wasn't until I realized that `ResourceRecords` actually was an array that I understood what was needed.

The following code converts from an "array of strings" to an "array of dict".

```python
nameserver_resource_records = [{'Value': nameserver} for nameserver in nameservers]
```

With the "array of dict", calling the upsert becomes trivial.

```python
response = client.change_resource_record_sets(
    HostedZoneId=domain_hosted_zone_id,
    ChangeBatch={
        'Changes': [
            {
                'Action': 'UPSERT',
                'ResourceRecordSet': {
                    'Name': sub_domain_name,
                    'Type': 'NS',
                    'TTL': 60,
                    'ResourceRecords': nameserver_resource_records
                }
            }
        ]
    }
)
```
