#!/bin/sh

# compile_grammar.sh -
#
# Using the installed Antlr4 Tools, Auto-generated Java classes will be created from the grammar.
# This script is assuming that the grammar lives in $ROOT/src/main/java/antlr4 and will auto-generate
# Java classes within the same directory to be used within the Compiler
#
# Installing the Antlr4 Tool Kit using pip install is an alternative way than Building the Antlr4 Tool Kit using Docker

root=$(git worktree list | cut -d " " -f1)
source "$root/venv/bin/activate"
pushd "$root/src/main/java/antlr4"
  antlr4 -v 4.12.0 -no-listener -visitor MiniJava.g4
popd