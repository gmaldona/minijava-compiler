#!/bin/bash

root=$(git worktree list | cut -d " " -f1)
pushd $root/src/main/minijava
  for uri in $(cat minijava_examples.uris); do
    filename=$(basename $uri)
    curl -O $uri
    grep -q $filename '.gitignore' || echo $filename >> '.gitignore'
  done
popd