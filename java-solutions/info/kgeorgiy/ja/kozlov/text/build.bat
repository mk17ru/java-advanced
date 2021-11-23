@echo off
mkdir "./temp"
echo %PWD%
set commonWay=../../../../../..
set nlib=%commonWay%/lib
set com=%nlib%/junit-platform-console-standalone-1.8.0-M1.jar;%nlib%/hamcrest-core-1.3.jar;%nlib%/junit-4.11.jar
echo %com%

javac -cp %com% *.java -encoding utf8 -d temp

echo "Build complete"