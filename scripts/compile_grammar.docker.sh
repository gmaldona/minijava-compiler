#!/bin/sh

# compile_grammar.docker.sh -
#
# Using the built docker image for Antlr4 Tools, a container will be created using the Tools
# jar and can generated Auto-generated Java classes for the grammar.
# The docker container has a volume mount mapper $ROOT/src/main/java/antlr4 (the directory where the grammar is located
# and where the compiler expects the Antlr4 Auto-generated classes to live) and /work directory on the container where
# the Antlr4 tools starting workdir is
#
# Compiling the grammar using Docker is an alternative way than installing the Antlr4 Tools using Python

root=$(git worktree list | cut -d " " -f1)
docker run -v "$root/src/main/java/antlr4:/work" antlr/antlr4 -no-listener -visitor MiniJava.g4