#!/bin/bash
./build.sh

if [ $? -eq 0 ]
then
    echo "Code Compile "$?
else
  echo "Exit code "$?
  exit $?
fi

commonWay=$PWD/../../../../../..
nlib=$commonWay/lib

java -jar $nlib/junit-platform-console-standalone-1.8.0-M1.jar --class-path temp -c info.kgeorgiy.ja.kozlov.rmi.BankTest

if [ $? -eq 0 ]
then
  echo "Running complete"
else
  echo "Exit code "$?
fi

exit $?