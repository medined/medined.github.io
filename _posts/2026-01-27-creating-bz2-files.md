---
layout: post
title: Create bz2 File Using Python
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

Create bz2 File Using Python

## Code

```python
#!/usr/bin/env python3
"""Create BZ2 compressed files containing a text file with one sentence.

Note: BZ2 is a compression format, not an archive format. It compresses a single file.
BZ2 does not natively support password protection. For password-protected compression,
consider using 7z or RAR formats instead.
"""

import bz2
import os
import glob


def create_bz2_archive(output_file="archive.bz2", sentence="Hello, this is a test sentence.", password=None):
    """
    Create a BZ2 compressed file containing a text file with the specified sentence.
    
    Args:
        output_file: Path to the output BZ2 file
        sentence: The sentence to write in the text file
        password: Optional password (not supported by BZ2 - will be ignored with warning)
    """
    if password:
        print(f"⚠ Warning: BZ2 format does not support native password protection.")
        print(f"  Use 7z or RAR formats for password-protected archives.")
    
    # Create file content
    file_content = sentence.encode('utf-8')
    
    # Compress content with BZ2
    compressed_content = bz2.compress(file_content, compresslevel=9)
    
    # Write to file
    with open(output_file, 'wb') as f:
        f.write(compressed_content)
    
    print(f"✓ Created BZ2 compressed file: {output_file}")


def is_password_protected(filepath):
    """
    Check if a BZ2 file is password-protected.
    
    Args:
        filepath: Path to the BZ2 file
        
    Returns:
        False - BZ2 format does not support password protection
    """
    # BZ2 doesn't support password protection
    return False


def decompress_bz2(filepath):
    """
    Decompress a BZ2 file to verify its contents.
    
    Args:
        filepath: Path to the BZ2 file
        
    Returns:
        Decompressed content as a string
    """
    try:
        with bz2.open(filepath, 'rt', encoding='utf-8') as f:
            return f.read()
    except Exception as e:
        print(f"Error decompressing {filepath}: {e}")
        return None


if __name__ == "__main__":
    # Create compressed file
    create_bz2_archive(output_file="archive.bz2")
    
    # Note: BZ2 doesn't support password protection, so we skip creating a password-protected version
    print("Note: BZ2 format does not support password protection.")
    
    # Check all BZ2 files in current directory
    print("\nChecking BZ2 files:")
    for filepath in sorted(glob.glob("*.bz2")):
        is_protected = is_password_protected(filepath)
        status = "🔒 Password-protected" if is_protected else "🔓 Unprotected"
        print(f"  {filepath}: {status}")
        
        # Show decompressed content
        content = decompress_bz2(filepath)
        if content:
            print(f"    Content: {content}")
```
