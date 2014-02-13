#!/bin/bash

LIB="./lib"
BASE="$1"
SRC="$BASE/src"
BIN="$BASE/bin"

shift

test -d "$BIN" || mkdir -p "$BIN"

F=$(find "$SRC" -name "*.java")
javac -verbose -d "$BIN" -cp "$LIB/*" "$F"
