#!/bin/bash
echo $PWD
javac -p ../../artifacts -p ../../lib --module-source-path ../../modules -d ../_build --module info.kgeorgiy.ja.kozlov.implementor
echo Manifest-Version: 1.0 > ../_build/Manifest.txt
echo Main-Class: info.kgeorgiy.ja.kozlov.implementor.Implementor >> ../_build/Manifest.txt
echo Class-Path: /java-advanced-2021/artifacts/info.kgeorgiy.java.advanced.implementor.jar >> ../_build/Manifest.txt
cd ../_build/info.kgeorgiy.ja.kozlov.implementor


jar cfm ../implementor.jar ../Manifest.txt info/kgeorgiy/ja/kozlov/implementor/*.class