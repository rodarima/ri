#!/bin/bash

LIB="./lib"
BASE="$1"
BIN="$BASE/bin"

if [ "$#" -lt "2" ]; then
	echo "Ejecuta una clase de un proyecto Java."
	echo "Uso:"
	echo "	$0 [PROJECT DIR] [PACKAGE.CLASS] [ARGS...]"
	exit
fi

shift

java -cp "$LIB/*:$BIN" $@
