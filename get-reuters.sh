#!/bin/bash

DATA="data"
REUTERS_DIR="$DATA/reuters"
REUTERS_TAR="reuters21578.tar.gz"
OUT_TAR="$DATA/$REUTERS_TAR"

test -d "$DATA" || mkdir -p "$DATA"

test -e "$OUT_TAR" || wget http://kdd.ics.uci.edu//databases/reuters21578/reuters21578.tar.gz -O $OUT_TAR

test -d "$REUTERS_DIR" || mkdir -p "$REUTERS_DIR"

tar zxvf "$OUT_TAR" -C "$REUTERS_DIR"
