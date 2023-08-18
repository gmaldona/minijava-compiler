VERSION=1.0

# build_jar.maven.sh -
#
# Will build an executable jar for the minijava.lang.MiniJava Compiler.
# Jar will be moved from $root/target to $root/build
#
# NOTE: This script does not check if the correct version of the JDK is installed.
#       This script will attempt to execute the maven command and if the wrong JDK is installed
#       maven will throw a "BUILD FAILURE"

root=$(git worktree list | cut -d " " -f1)

pushd $root
  mvn clean install test assembly:single
popd

# Finding any MiniJavac Jar. This is a work around for versioning and maven assembly metadata.
# To lazy and indirection to hard code versioning and metadata
jar=$(find "$root/target" -maxdepth 1 -name "minijava-$VERSION*.jar" | head -1)

if [ -z $jar ]; then                                                                        # /\
  exit 0  # ==> If exit code 0 assuming Maven did not build jar correctly. Assume Build NOTE: ||
fi

# Only copy over the Jar and Bash Script into $root/build if maven jar finished
cp $jar "$root/build"

# echo compile script to directly call jar
echo -n "#!/bin/bash \n
# MiniJavac along with dependencies are contained within jar. \n
# MiniJavac expects parameters.
java -jar $jar $@" > "$root/build/compile"
