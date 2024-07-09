---
layout: post
title: Generating Placeholder Docstrings Using The ast Python Package
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

To generate placeholder docstrings in a legacy code base to reduce pyliny message clutter.

## Introduction

In July 2024, Running PyLint on the [OpenDevin](https://github.com/OpenDevin) codebase produced 713 messages. 234 of these messages were related to missing docstrings. It is possible, even easy, to configure PyLint to ignore these messages. However, doing so won't nudge the code base towards better documentation. Once all placeholder docstrings are in place, future code (pull requests) can be rejected if docstrings are not present.

Additionally, having a large number of PyLint messages make it hard to find important messages. For example, this message might be important enough to fix since when check is True a CalledProcessError exception will be raised when the sub-process returns a non-zero exit code.

```python
opendevin/runtime/docker/local_box.py:31:32: W1510: 
Using subprocess.run without explicitly set `check` is not recommended. (subprocess-run-check)
```

## Aid To Conformance

If your goal is to have "real" docstrings, this tool can help. Add the placeholders, then search for "placeholder" and start to add real comments. This approach provides metrics about how many comments need to be added before the work is done. 

## Solution

The following python script will add module, class, and function placeholder docstrings everywhere they are missing. 

NOTE: If an existing docstring contains a null byte ("\x00") then the script below will convert that docstring into a single-quote string instead of a triple-quote string. This is an issue with how python and the aster package handles the null byte. Replace your "\0x00" with "\\x00" to avoid this issue.

```python
"""
This script adds placeholder module, class, and function docstrings.

It is intended to be used in legacy code bases to reduce the number of lint messages while still encouraging real docstrings to be added. Over time, the placeholder docstring can be replaced.
"""

import os
import ast
import astor
error_count = 0
errors = []


def find_classes_without_docstrings(tree):
    """
    Walks the abstract syntax tree looking for classes without a docstring.
    """
    classes_without_docstrings = []
    for node in ast.walk(tree):
        if isinstance(node, ast.ClassDef):
            if not node.body or not isinstance(node.body[0], ast.Expr
                ) or not isinstance(node.body[0].value, ast.Constant):
                classes_without_docstrings.append(node)
    return classes_without_docstrings


def find_functions_without_docstrings(tree):
    """
    Walks the abstract syntax tree looking for functions without a docstring.
    """
    functions_without_docstrings = []
    for node in ast.walk(tree):
        if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef)):
            if not node.body or not isinstance(node.body[0], ast.Expr
                ) or not isinstance(node.body[0].value, ast.Constant):
                functions_without_docstrings.append(node)
    return functions_without_docstrings


def docstring(s):
    """
    A helper function to wrap a string into a docstring node.
    """
    return ast.Expr(value=ast.Constant(s))


def add_placeholder_docstrings(file_path):
    """
    The function that gets the work done. It reads and parse the python file. Then
    finds where the docstrings are missing and adds them.
    """
    global error_count

    needs_writing = False
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
            if not content.strip():
                with open(file_path, 'w', encoding='utf-8') as empty_file:
                    empty_file.write('"""\nPlaceholder Module Docstring\n"""\n'
                        )
                return True
            tree = ast.parse(content, filename=file_path)
        if not tree.body or not isinstance(tree.body[0], ast.Expr
            ) or not isinstance(tree.body[0].value, ast.Constant):
            tree.body.insert(0, docstring('Placeholder Module Docstring'))
            needs_writing = True
        for func in find_classes_without_docstrings(tree):
            func.body.insert(0, docstring('Placeholder Class Docstring'))
            needs_writing = True
        for func in find_functions_without_docstrings(tree):
            func.body.insert(0, docstring('Placeholder Function Docstring'))
            needs_writing = True
        if needs_writing:
            with open(file_path, 'w', encoding='utf-8') as file:
                file.write(astor.to_source(tree))
    except Exception as e:
        errors.append(f'Error processing {file_path}: {e}')
        print(f'Error processing {file_path}: {e}')
        error_count += 1
        pass
    return needs_writing


def check_directory_for_missing_docstrings(root_dir):
    """
    This is the directory walker. It looks for python files.
    """
    for subdir, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.py'):
                file_path = os.path.join(subdir, file)
                if add_placeholder_docstrings(file_path):
                    print(f'Added placeholder docstrings in {file_path}')


check_directory_for_missing_docstrings('.')
print('---------------------------')
for error in errors:
    print(error)
print(f'Finished with {error_count} errors.')
```
