#!/bin/sh

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-11.0.17.jdk/Contents/Home/"
cd src/main/java/antlr4/ && antlr4 -no-listener -visitor MiniJava.g4