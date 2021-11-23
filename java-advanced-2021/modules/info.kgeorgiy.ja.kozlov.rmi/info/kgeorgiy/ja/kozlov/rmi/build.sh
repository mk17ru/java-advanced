#!/bin/bash
mkdir "./temp"
echo $PWD
nlib=../../../../../../lib

echo $nlib
com="$nlib/junit-platform-console-standalone-1.8.0-M1.jar:/hamcrest-core-1.3.jar"
javac -cp $com *.java -d temp

echo "Build complete"