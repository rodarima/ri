#!/bin/bash

LIB="../lib"
SRC="./src"

F=$(find $SRC -name "*.java")
javac -verbose -d bin -cp "$LIB/*:src" $F
