#!/bin/bash
./build.sh
java -cp "./temp" info.kgeorgiy.ja.kozlov.rmi.Server $@
