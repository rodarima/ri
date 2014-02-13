#!/bin/bash

LIB="/home/rodrigo/fic/3/ri/lib"

javac -verbose -d bin -cp "$LIB/*:src" src/simpleindexing/SimpleIndexing.java
