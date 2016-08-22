#!/bin/bash
echo "$PWD"
mvn install:install-file -DgroupId=one-nio -DartifactId=one-nio -Dversion=1.0 -Dpackaging=jar -Dfile="$PWD/one-nio/one-nio.jar"
