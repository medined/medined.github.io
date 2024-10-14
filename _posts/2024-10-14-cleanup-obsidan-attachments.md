---
layout: post
title: Cleanup Obsidian Attachments Directory
author: David Medinets
categories:
  - "[[python]]"
year: 2024
theme: python
---

{:.no_toc}
* unordered list
{:toc}

* * *

# Goal

To trash attachments no longer used by Obsidian pages.

## Introduction

I've been copying Obsidian pages which also copies attachments. Then removing the attachment references and adding different attachments. This leaves orphan images in my attachments directory. 

The script below trashes any files in the attachments directory that are not referenced by a markdown page.

## How I use the script.

I created a small bash script that does the following so I don't need to re-type the command.

```
./cmd-find-unreferenced-attachments.py \
    -m /home/medined/Dropbox/david/fences \
    -a /home/medined/Dropbox/david/fences/Attachments
```

## Script

This script is located at https://github.com/medined/obsidian-attachment-cleanup.

Scroll down to the main function below and the logic of the script should be easy to follow.

```
#!/bin/env python
# -*- coding: utf-8 -*-

import argparse
import logging
import os
import re
from send2trash import send2trash

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


markdown_dir = '/home/medined/Dropbox/david/'
attachments_dir = '/home/medined/Dropbox/david/fences/Attachments'

def traverse_markdown_files(directory):
    """
    Traverse the given directory to find all markdown files.

    Args:
        directory (str):g The directory to traverse.

    Returns:
        list: A list of full paths to markdown files.
    """
    markdown_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.md'):
                markdown_files.append(os.path.join(root, file))
    return markdown_files

def extract_image_references(markdown_files):
    """
    Extract image references from the given markdown files.

    Args:
        markdown_files (list): A list of full paths to markdown files.

    Returns:
        set: A set of image references found in the markdown files.
    """
    image_references = set()
    image_pattern = re.compile(r'!\[\[([^\|\]]+)(?:\|\d+)?\]\]')
    for md_file in markdown_files:
        with open(md_file, 'r', encoding='utf-8') as file:
            content = file.read()
            matches = image_pattern.findall(content)
            for match in matches:
                image_references.add(match)
    return image_references

def list_all_attachments(directory):
    """
    List all attachment files in the given directory with full paths.

    Args:
        directory (str): The directory to list attachment files from.

    Returns:
        set: A set of full paths to attachment files.
    """
    return set(os.path.join(directory, f) for f in os.listdir(directory))

def find_unreferenced_files(all_attachments, image_references, attachments_dir):
    """
    Find unreferenced files by comparing all attachments with image references.

    Args:
        all_attachments (set): A set of full paths to all attachment files.
        image_references (set): A set of image references found in markdown files.
        attachments_dir (str): The directory where attachments are stored.

    Returns:
        set: A set of unreferenced attachment files.
    """
    return all_attachments - set(os.path.join(attachments_dir, ref) for ref in image_references)

def main():
    """
    Main function to find and move unreferenced attachment files to trash.
    """

    parser = argparse.ArgumentParser(description="Clean unreferenced attachment files.")
    parser.add_argument("-m", '--markdown_dir', required=True, help="Directory containing markdown files")
    parser.add_argument("-a", '--attachments_dir', required=True, help="Directory containing attachment files")
    args = parser.parse_args()
    
    markdown_dir = args.markdown_dir
    attachments_dir = args.attachments_dir

    markdown_files = traverse_markdown_files(markdown_dir)
    image_references = extract_image_references(markdown_files)
    all_attachments = list_all_attachments(attachments_dir)
    unreferenced_files = find_unreferenced_files(all_attachments, image_references, attachments_dir)

    for file in unreferenced_files:
        logging.info(f"Moving to trash: {file}")
        send2trash(file)
            

if __name__ == "__main__":
    main()
```