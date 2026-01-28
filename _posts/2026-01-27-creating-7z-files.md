---
layout: post
title: Create 7z File Using Python
author: David Medinets
categories:
  - "[[python]]"
year: 2026
theme: python
---

{:.no_toc}
* unordered list
{:toc}

* * *

# Goal

Create 7z File Using Python

## Code

```python
#!/usr/bin/env python3
"""Create 7z archives containing a text file with one sentence."""

import py7zr
import os
import glob
from io import BytesIO


def create_7z_archive(output_file="archive.7z", sentence="Hello, this is a test sentence.", password=None):
    """
    Create a 7z archive containing a text file with the specified sentence.
    Uses in-memory operations (BytesIO) to avoid creating temporary files on disk.
    
    Args:
        output_file: Path to the output 7z file
        sentence: The sentence to write in the text file
        password: Optional password to encrypt the archive
    """
    # Create file content in memory
    file_content = sentence.encode('utf-8')
    
    # Create a 7z archive with in-memory content
    with py7zr.SevenZipFile(output_file, "w", password=password) as archive:
        # Add the file to the archive using writestr (no temp file needed)
        archive.writestr(file_content, "message.txt")
    
    if password:
        print(f"✓ Created password-protected 7z archive: {output_file}")
    else:
        print(f"✓ Created 7z archive: {output_file}")


def is_password_protected(filepath):
    """
    Check if a 7z file is password-protected.
    
    Args:
        filepath: Path to the 7z file
        
    Returns:
        True if the archive is password-protected, False otherwise
    """
    try:
        with py7zr.SevenZipFile(filepath, "r") as archive:
            # Use the built-in needs_password method
            return archive.needs_password()
    except Exception as e:
        print(f"Error checking {filepath}: {e}")
        return False


if __name__ == "__main__":
    # Create unencrypted archive
    create_7z_archive(output_file="archive.7z")
    
    # Create password-protected archive
    create_7z_archive(output_file="archive_protected.7z", password="secure123")
    
    # Check all 7z files in current directory
    print("\nChecking 7z files for password protection:")
    for filepath in sorted(glob.glob("*.7z")):
        is_protected = is_password_protected(filepath)
        status = "🔒 Password-protected" if is_protected else "🔓 Unprotected"
        print(f"  {filepath}: {status}")
```
