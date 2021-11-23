#!/bin/bash
./build.sh

if [ $? -eq 0 ]
then
  echo "Running complete"
else
  echo "Exit code "$?
fi

commonWay=$PWD/../../../../../..
nlib=$commonWay/lib

java -cp "./temp":"$nlib/junit-platform-console-standalone-1.8.0-M1.jar":"$nlib/hamcrest-core-1.3.jar" info.kgeorgiy.ja.kozlov.rmi.BankTest > zoutput.txt
if [ $? -eq 0 ]
then
  echo "Running complete with code "$?
else
  echo "Exit code "$?
fi

exit $?