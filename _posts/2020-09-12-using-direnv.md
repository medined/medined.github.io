---
layout: post
title: Using direnv To Customize Your Environment Per Directory
author: David Medinets
categories: bash
year: 2020
theme: bash
---

## Introduction

direnv  is an environment variable manager for your shell. It knows how to hook into bash, zsh and fish shell to load or unload environment variables depending on your current directory. This allows
you to have project-specific environment variables and not clutter the " /.profile" file.

Before each prompt it checks for the existence of an .envrc file in the current and parent directories. If the file exists, it is loaded into a bash sub-shell and all  exported  variables  are  then
captured by direnv and then made available to your current shell, while unset variables are removed.

Because  direnv  is  compiled  into  a single static executable it is fast enough to be unnoticeable on each prompt. It is also language agnostic and can be used to build solutions similar to rbenv, pyenv, phpenv, ...

## Links

* https://direnv.net/

## Baskstory

Like many people I use a different directory per project. And many of my projects each have their own virtual python enviroments. I was using a terrible hack that consisted of setting an environment variable in `~/.bashrc`:

```bash
export FOCUSED_PROJECT="typhoon"
```

Then using `if` statements to configure the environment based on the project:

```
if [ $FOCUSED_PROJECT == "kubespray" ]; then
    ssh-add $HOME/Downloads/pem/some.pem
    cd /data/projects/ic1/kubespray
    export AWS_REGION="us-east-1"
    export AWS_PROFILE=gloop
    export NAMESPACE=default
    export PKI_PRIVATE_PEM=$HOME/Downloads/pem/some.pem
    export SSH_USER=centos
    export SSM_BINARY_DIR=/data/projects/dva/amazon-ssm-agent/bin
fi
```

I had ten of those `if` statements in `~/.bashrc`. It was ungainly.

Then I learned about `direnv`. Now I have a `.envrc` file in each directory. My environment is automatically configured as I switch directories.

## Installation

Add the follow to the end of your `.bashrc` file.

```
show_virtual_env() {
  if [[ -n "$VIRTUAL_ENV" && -n "$DIRENV_DIR" ]]; then
    echo "($(basename $VIRTUAL_ENV)) "
  fi
}
export -f show_virtual_env
PS1='$(show_virtual_env)'$PS1

eval "$(direnv hook bash)"
```

Now start a new shell.

## Configure To Use venv.

Use the following commands to create a `direnvrc` file. Notice that variable interpolation is not used in the HEREDOC.

```
mkdir -p ~/.config/direnv

cat <<-'EOF' >$HOME/.config/direnv/direnvrc
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
layout_python-venv() {
    local python=${1:-python3}
    [[ $# -gt 0 ]] && shift
    unset PYTHONHOME
    if [[ -n $VIRTUAL_ENV ]]; then
        VIRTUAL_ENV=$(realpath "${VIRTUAL_ENV}")
    else
        local python_version
        python_version=$("$python" -c "import platform; print(platform.python_version())")
        if [[ -z $python_version ]]; then
            log_error "Could not detect Python version"
            return 1
        fi
        VIRTUAL_ENV=$PWD/.direnv/python-venv-$python_version
    fi
    export VIRTUAL_ENV
    if [[ ! -d $VIRTUAL_ENV ]]; then
        log_status "no venv found; creating $VIRTUAL_ENV"
        "$python" -m venv "$VIRTUAL_ENV"
    fi

    PATH="${VIRTUAL_ENV}/bin:${PATH}"
    export PATH
}
EOF
```

## Create .envrc File

Switch to a directory which needs a python virtual environment.

```
cat <<EOF >.envrc
export VIRTUAL_ENV=venv
layout python-venv python3.8
EOF
```

Note that `direnv` is smart enough to deactivate the virtual directory when you change out of it.

After creating a `.envrc` file, you will notice that direnv complains about the .envrc being blocked. This is the security mechanism to avoid loading new files automatically. Otherwise  any git repo  that  you pull, or tar archive that you unpack, would be able to wipe your hard drive once you cd into it.

Run `direnv allow` and watch direnv loading your new environment. Note that direnv edit . is a handy shortcut that open the file in your $EDITOR and automatically allows it if the file's modification time has changed.

## Configure git To Ignore direnv Files

Because direnv is essentially a personal tool for now, I recommend that you hide the direnv files and folders so that you don't have to set them in all your project's `.gitignore`:

Note that this setting is personal and needs to be run by each `git` user. Also note that editors, like Visual Code Studio, might not know about this setting and there might not handle it properly.

```bash
git config --global core.excludesfile "~/.gitignore_global"
cat <<EOF >> ~/.gitignore_global
# Direnv Files
.direnv
.envrc
# Python Virtual Environment
venv
EOF
```

## Create Scheduled Job To Backup Files

Create `/etc/cron.hourly/backup-direnv-configuration-files.sh` with the following content to have your .envrc files backed hourly. This seems like a good ida to me.

```
#!/bin/bash

#
# Find all the .envrc files in my projects directory and its sub-directories.
#

PROJECT_DIR=/data/projects
BACKUP_DIR=/data/direnv-backups

for f in $(find $PROJECT_DIR -name .envrc -type f); do
  BASE_DIR=$(echo $f | rev | cut -b8- | rev)
  ABSOLUTE_DIR=$BACKUP_DIR$BASE_DIR
  mkdir -p $ABSOLUTE_DIR
  cp $f $ABSOLUTE_DIR
  echo "Created backup: $f"
done
```
