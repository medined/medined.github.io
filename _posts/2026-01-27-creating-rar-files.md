---
layout: post
title: Create rar File Using Python
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

Create rar File Using Python

## Code

```python
#!/usr/bin/env python3
"""Create RAR archives containing a text file with one sentence.

Note: RAR archive creation requires the 'rar' command-line tool (from WinRAR).
This script uses the rarfile library for reading and analyzing RAR files,
and attempts to create them via the command-line tool if available.
"""

import rarfile
import os
import glob
import subprocess
import tempfile


def create_rar_archive(output_file="archive.rar", sentence="Hello, this is a test sentence.", password=None):
    """
    Create a RAR archive containing a text file with the specified sentence.
    
    Requires the 'rar' command-line tool to be installed.
    On Linux, you can install it with: sudo apt-get install rar
    On macOS: brew install rar
    On Windows: Install WinRAR
    
    Args:
        output_file: Path to the output RAR file
        sentence: The sentence to write in the text file
        password: Optional password to encrypt the archive
    """
    # Use a temporary file to create the RAR archive
    with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False) as temp_file:
        temp_file.write(sentence)
        temp_filename = temp_file.name
    
    try:
        # Build rar command
        cmd = ['rar', 'a', '-ep1']  # -ep1 excludes the base folder
        if password:
            cmd.extend(['-p' + password])
        cmd.extend([output_file, temp_filename])
        
        # Try to create RAR using command line
        try:
            result = subprocess.run(cmd, check=True, capture_output=True, text=True)
            if password:
                print(f"✓ Created password-protected RAR archive: {output_file}")
            else:
                print(f"✓ Created RAR archive: {output_file}")
        except FileNotFoundError:
            print(f"✗ Error: 'rar' command not found.")
            print(f"  Please install RAR to create RAR archives:")
            print(f"    - Linux: sudo apt-get install rar")
            print(f"    - macOS: brew install rar")
            print(f"    - Windows: Install WinRAR")
        except subprocess.CalledProcessError as e:
            print(f"✗ Error creating RAR archive: {e.stderr}")
    finally:
        # Clean up temporary file
        if os.path.exists(temp_filename):
            os.remove(temp_filename)


def is_password_protected(filepath):
    """
    Check if a RAR file is password-protected.
    
    Args:
        filepath: Path to the RAR file
        
    Returns:
        True if the archive is password-protected, False otherwise
    """
    try:
        with rarfile.RarFile(filepath, "r") as archive:
            # Use the built-in needs_password method
            return archive.needs_password()
    except Exception as e:
        print(f"Error checking {filepath}: {e}")
        return False


if __name__ == "__main__":
    # Create unencrypted archive
    create_rar_archive(output_file="archive.rar")
    
    # Create password-protected archive
    create_rar_archive(output_file="archive_protected.rar", password="secure123")
    
    # Check all RAR files in current directory
    print("\nChecking RAR files for password protection:")
    for filepath in sorted(glob.glob("*.rar")):
        is_protected = is_password_protected(filepath)
        status = "🔒 Password-protected" if is_protected else "🔓 Unprotected"
        print(f"  {filepath}: {status}")
```
