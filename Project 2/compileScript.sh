#!/bin/sh
javac -d bin src/*/*.java
java -cp bin/ server.CentralServer
