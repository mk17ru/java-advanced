#!/bin/bash
echo $PWD
myModules="../../newModules"
sourceModules="../../../java-advanced-2021/modules"
mkdir $myModules/info.kgeorgiy.java.advanced.implementor
mkdir $myModules/info.kgeorgiy.java.advanced.base
cp -rf $sourceModules/info.kgeorgiy.java.advanced.implementor/* $myModules/info.kgeorgiy.java.advanced.implementor
cp -rf $sourceModules/info.kgeorgiy.java.advanced.base/* $myModules/info.kgeorgiy.java.advanced.base
cp -rf ../info/kgeorgiy/ja/kozlov/implementor/*.java $myModules/info.kgeorgiy.ja.kozlov.implementor/info/kgeorgiy/ja/kozlov/implementor
javac -p ../../../java-advanced-2021/artifacts/ -p ../../../java-advanced-2021/lib/ \
--module-source-path $myModules/ -d ../jarDir/ --module info.kgeorgiy.ja.kozlov.implementor
echo Manifest-Version: 1.0 > ../jarDir/Manifest.txt
echo Main-Class: info.kgeorgiy.ja.kozlov.implementor.Implementor >> ../jarDir/Manifest.txt
echo Class-Path: ../../../java-advanced-2021/artifacts/info.kgeorgiy.java.advanced.implementor.jar >> ../jarDir/Manifest.txt
cd ../jarDir/info.kgeorgiy.ja.kozlov.implementor
jar cfm ../implementor.jar ../Manifest.txt .
rm -r ../$myModules/info.kgeorgiy.java.advanced.implementor
rm -r ../$myModules/info.kgeorgiy.java.advanced.base
cd ../
rm -r info.kgeorgiy.ja.kozlov.implementor/
rm -r info.kgeorgiy.java.advanced.base/
rm -r info.kgeorgiy.java.advanced.implementor/
rm Manifest.txt

