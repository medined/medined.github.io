---
layout: post
title: Fetching Stock Symbols
author: David Medinets
categories:
  - "[[investing]]"
year: 2021
theme: investing
---

## Goal

To get a list of stock symbols for the NYSE and NASDAQ markets.

## Introduction

I've been writing python programs to help plan my investment trades. The basis of everything is to know what stocks are available. The code below gets that information in a Pandas dataframe from an FTP site.

## Code

This is the `NYSEMarket.py` file.

```python
import pandas as pd

# See https://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs#nasdaq for information.

class NYSEMarket:

    url = "ftp://ftp.nasdaqtrader.com/symboldirectory/otherlisted.txt"

    original_columns = [
        'ACT Symbol',
        'Security Name',
        'Exchange',
        'CQS Symbol',
        'ETF',
        'Round Lot Size',
        'Test Issue',
        'NASDAQ Symbol'
    ]

    # These columns are ignorable after they are used for filtering.
    ignorable_columns = [
        'CQS Symbol',
        'ETF',
        'Exchange',
        'NASDAQ Symbol',
        'Round Lot Size',
        'Test Issue',
    ]

    def __init__(self):
        self.df = pd.read_csv(self.url, delimiter='|')
        self.df = self.df[self.df['Test Issue'] == 'N']
        self.df = self.df[self.df['ETF'] == 'N']
        self.df.drop(self.ignorable_columns, axis=1, inplace=True)
        self.df.rename(columns={'ACT Symbol':'Symbol'}, inplace=True)
        self.df = self.df[:-1]
```

This is the `NASDAQMarket.py` file.

```
import pandas as pd

# See https://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs#nasdaq for information.

class NASDAQMarket:

    url = "ftp://ftp.nasdaqtrader.com/symboldirectory/nasdaqlisted.txt"

    original_columns = [
       'Symbol',
       'Security Name',
       'Market Category',
       'Test Issue',
       'Financial Status',
       'Round Lot Size',
       'ETF',
       'NextShares',
    ]

    # These columns are ignorable after they are used for filtering.
    ignorable_columns = [
       'ETF',
       'Financial Status',
       'Market Category',
       'NextShares',
       'Round Lot Size',
       'Test Issue',
    ]

    def __init__(self):
        self.df = pd.read_csv(self.url, delimiter='|')
        self.df = self.df[self.df['Test Issue'] == 'N']
        self.df = self.df[self.df['ETF'] == 'N']
        self.df.drop(self.ignorable_columns, axis=1, inplace=True)
        self.df = self.df[:-1] # drop the last row.
```

This is how I use those classes:

```
#!/usr/bin/env python

from NASDAQMarket import NASDAQMarket
from NYSEMarket import NYSEMarket
import pandas as pd

nasdaq_market = NASDAQMarket()
nyse_market = NYSEMarket()

df = pd.concat([nasdaq_market.df, nyse_market.df])
df.rename(columns={'Symbol': 'symbol'}, inplace=True)
df.rename(columns={'Security Name': 'company_name'}, inplace=True)
df = df[~df.symbol.str.contains('\.')]
df = df[~df.symbol.str.contains('\$')]
df = df[~df.company_name.str.endswith('- Rights')]
df = df[~df.company_name.str.endswith('- Subunit')]
df = df[~df.company_name.str.endswith('- Subunits')]
df = df[~df.company_name.str.endswith('- Trust Preferred Securities')]
df = df[~df.company_name.str.endswith('- Unit')]
df = df[~df.company_name.str.endswith('- Units')]
df = df[~df.company_name.str.endswith('- Warrant')]
df = df[~df.company_name.str.endswith('- Warrants')]
df.sort_values(by=['symbol'], inplace=True)
print(df.head())
```

The output looks like this:

```
symbol                                       company_name
0      A            Agilent Technologies, Inc. Common Stock
1     AA                    Alcoa Corporation Common Stock
4    AAC  Ares Acquisition Corporation Class A Ordinary ...
0   AACG  ATA Creativity Global - American Depositary Sh...
1   AACQ     Artius Acquisition Inc. - Class A Common Stock
```
