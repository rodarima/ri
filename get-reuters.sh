#!/bin/bash

DATA="data"
REUTERS_DIR="$DATA/reuters"
REUTERS_TAR="reuters21578.tar.gz"
OUT_TAR="$DATA/$REUTERS_TAR"

test -e "$OUT_TAR" && exit

wget http://kdd.ics.uci.edu//databases/reuters21578/reuters21578.tar.gz -O data/reuters21578.tar.gz

test -d "$REUTERS_DIR" && exit

mkdir -p "$REUTERS_DIR"

tar zxvf "$OUT_TAR" -C "$REUTERS_DIR"
