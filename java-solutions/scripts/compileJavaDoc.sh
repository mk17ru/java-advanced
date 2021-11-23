#!/bin/bash
cd ../
echo $PWD
commonWay=../../java-advanced-2021/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/
javadoc	-author -d ../documents \
-cp ../../java-advanced-2021/artifacts/info.kgeorgiy.java.advanced.implementor.jar:lib/quickcheck-0.6.jar:lib/junit-4.11.jar \
$commonWay/ImplerException.java \
$commonWay/JarImpler.java \
$commonWay/Impler.java \
info/kgeorgiy/ja/kozlov/implementor/package-info.java \
info/kgeorgiy/ja/kozlov/implementor/Implementor.java \
info/kgeorgiy/ja/kozlov/implementor/FileMethods.java \
-link https://docs.oracle.com/en/java/javase/11/docs/api \
-private