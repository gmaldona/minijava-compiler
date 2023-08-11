#!/bin/zsh

# build_env.docker.sh -
#
# Building the Antlr4 Tool Kit using Docker to avoid having to install on local machine.
# Download the Antlr4 Tools in a container to compile the grammar and keeping the container on the local machine
# is not necessary.
# Antlr4 Tool Kit Auto-generates Java classes from *.g4 grammar file to be used within the Compiler
#
# Compiling the grammar using Docker is an alternative way than installing the Antlr4 Tools using Python

root=$(git worktree list | cut -d " " -f1)

pushd "$root/scripts"
  docker build -t antlr/antlr4 --platform linux/amd64 .
popd