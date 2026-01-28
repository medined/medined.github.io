---
layout: post
title: Create xz File Using Python
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

Create xz File Using Python

## Code

```python
#!/usr/bin/env python3
"""Create XZ-compressed files containing a text file with one sentence.

Note: XZ (.xz) is a compression format, not an archive format, and it does not
support native password protection. For encrypted archives, prefer 7z or RAR.
"""

import lzma
import glob


def create_xz_file(output_file="archive.xz", sentence="Hello, this is a test sentence.", password=None):
    """
    Create an XZ-compressed file containing a text file with the specified sentence.

    Args:
        output_file: Path to the output .xz file
        sentence: The sentence to write in the text file
        password: Optional password (not supported by XZ; will be ignored with a warning)
    """
    if password:
        print("⚠ Warning: XZ format does not support native password protection.")
        print("  Use 7z or RAR for encrypted archives.")

    # Create file content
    file_content = sentence.encode("utf-8")

    # Compress with XZ (LZMA) at a reasonable preset
    compressed_content = lzma.compress(file_content, preset=6)

    # Write compressed bytes to disk
    with open(output_file, "wb") as f:
        f.write(compressed_content)

    print(f"✓ Created XZ-compressed file: {output_file}")


def is_password_protected(filepath):
    """
    Check if an XZ file is password-protected.

    Args:
        filepath: Path to the .xz file

    Returns:
        False - XZ format does not support password protection
    """
    return False


def decompress_xz(filepath):
    """
    Decompress an XZ file to verify its contents.

    Args:
        filepath: Path to the .xz file

    Returns:
        Decompressed content as a string, or None on error
    """
    try:
        with lzma.open(filepath, "rt", encoding="utf-8") as f:
            return f.read()
    except Exception as e:
        print(f"Error decompressing {filepath}: {e}")
        return None


if __name__ == "__main__":
    # Create unencrypted file
    create_xz_file(output_file="archive.xz")

    print("Note: XZ format does not support password protection.")

    # Check all XZ files in current directory
    print("\nChecking XZ files:")
    for filepath in sorted(glob.glob("*.xz")):
        is_protected = is_password_protected(filepath)
        status = "🔒 Password-protected" if is_protected else "🔓 Unprotected"
        print(f"  {filepath}: {status}")

        content = decompress_xz(filepath)
        if content is not None:
            print(f"    Content: {content}")
```
