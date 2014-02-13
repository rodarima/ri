#!/bin/bash

LIB="/home/rodrigo/fic/3/ri/lib"
BIN="/home/rodrigo/fic/3/ri/test1/bin"

java -cp "$BIN:$LIB/*" simpleindexing.SimpleIndexing $@
