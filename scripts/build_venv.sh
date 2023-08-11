#!/bin/sh

# build_venv.sh -
#
# Installing the Antlr4 Tool Kit using pip install.
# # Antlr4 Tool Kit Auto-generates Java classes from *.g4 grammar file to be used within the Compiler
#
# Installing the Antlr4 Tool Kit using pip install is an alternative way than Building the Antlr4 Tool Kit using Docker

root=$(git worktree list | cut -d " " -f1)
python3 -m venv "$root/venv"
source "$root/venv/bin/activate"
python3 -m pip install -r "$root/requirements.txt"
which antlr4


