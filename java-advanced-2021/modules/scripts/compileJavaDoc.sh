#!/bin/bash
cd ../
javadoc	-cp artifacts/info.kgeorgiy.java.advanced.implementor.jar:lib/junit-4.11.jar:lib/quickcheck-0.6.jar\
	info.kgeorgiy.ja.kozlov.implementor/info/kgeorgiy/ja/kozlov/implementor/Implementor.java \
	info.kgeorgiy.ja.kozlov.implementor/info/kgeorgiy/ja/kozlov/implementor/FileMethods.java \
	info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/ImplerException.java \
	info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/JarImpler.java \
	info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java \
	-author -d documents \
