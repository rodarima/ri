#!/bin/bash

LIB="./lib"
BASE="$1"
BIN="$BASE/bin"

shift

java -cp "$LIB/*:$BIN" $@
