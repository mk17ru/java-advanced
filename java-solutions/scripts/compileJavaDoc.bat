@echo off

cd ..\
set module=info\kgeorgiy\ja\kozlov\implementor
set moduleTask=..\..\java-advanced-2021\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
javadoc -author -d ..\documents ^
-cp ..\..\java-advanced-2021\artifacts\info.kgeorgiy.java.advanced.implementor.jar:lib\quickcheck-0.6.jar:lib\junit-4.11.jar ^
%moduleTask%\ImplerException.java ^ %moduleTask%\JarImpler.java ^ %moduleTask%\Impler.java ^
%module%\Implementor.java ^ %module%\FileMethods.java -private ^
%module%\package-info.java ^
-link https://docs.oracle.com/en/java/javase/13/docs/api