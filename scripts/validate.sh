#!/bin/bash

# validate.sh -
#
# A validation script to determine if the necessary files for the MiniJava Compiler
# was generated. These auto-generated files come from running the Antlr4 Tools on
# a *.g4 file.

root=$(git worktree list | cut -d " " -f1)

expected_files=(MiniJava.g4 MiniJava.interp MiniJava.tokens MiniJavaBaseVisitor.java MiniJavaLexer.java \
        MiniJavaLexer.interp MiniJavaLexer.tokens MiniJavaParser.java MiniJavaVisitor.java)
expected_directory=$root/src/main/java/antlr4
for file in ${expected_files[@]}; do
  if [ ! -f $expected_directory/$file ]; then
    >&2 echo "$file was not found in $expected_directory"
  fi
done