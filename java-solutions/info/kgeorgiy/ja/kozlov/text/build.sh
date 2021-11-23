#!/bin/bash
mkdir "./temp"
echo $PWD
nlib=../../../../../../lib

echo $nlib
com="$nlib/junit-platform-console-standalone-1.8.0-M1.jar:/hamcrest-core-1.3.jar"
javac -cp $com *.java -encoding utf8 -d temp
cp -r bundles temp/info/kgeorgiy/ja/kozlov/text
cp -r goldenCollection temp/info/kgeorgiy/ja/kozlov/text

echo "Build complete"