#!/bin/bash

Version="0.9.3"

for jar in "libintf/build/libs/libintf-${Version}.jar" \
    "yajsync-core/build/libs/yajsync-core-${Version}.jar" \
    "yajsync-app/build/libs/yajsync-app-${Version}.jar"
do
  cp -v $jar /home/klaas/Projects/RsyncServerApp/app/libs/
done
