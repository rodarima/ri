#!/bin/bash

LIB="./lib"
BASE="$1"
SRC="$BASE/src"
BIN="$BASE/bin"

if [ "$#" -lt "1" ]; then
	echo "Compila un proyecto Java."
	echo "Uso:"
	echo "	$0 [BASE DIR]"
	exit
fi

shift

test -d "$BIN" || mkdir -p "$BIN"

find "$SRC" -name "*.java" -exec javac -verbose -d "$BIN" -cp "$LIB/*" "{}" \;
