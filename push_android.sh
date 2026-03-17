#!/bin/bash

set -euo pipefail

Version="0.9.4"

RsyncServerApp="/home/klaas/Projects/RsyncServerApp"
tgtdir="$RsyncServerApp/yajsync-libs-$Version" 

mkdir -p "$tgtdir" 

for jar in "libintf/build/libs/libintf-${Version}.jar" \
    "yajsync-core/build/libs/yajsync-core-${Version}.jar" \
    "yajsync-app/build/libs/yajsync-app-${Version}.jar"
do
  cp -v $jar "$tgtdir"
done

cd "$RsyncServerApp/app"
ln -snf "../yajsync-libs-$Version" libs


