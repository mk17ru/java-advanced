#!/bin/bash

cd ../
~/jdk-11.0.2/bin/javadoc \
	-link https://docs.oracle.com/en/java/javase/11/docs/api/ \
	-private \
	-d docs \
        -cp artifacts/info.kgeorgiy.java.advanced.implementor.jar:`
            `lib/junit-4.11.jar:`
            `lib/quickcheck-0.6.jar\
	mymodules/ru.ifmo.rain.kozlov.implementor/ru/ifmo/rain/koveshnikov/implementor/Implementor.java \
	modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/ImplerException.java \
	modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/JarImpler.java \
	modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java