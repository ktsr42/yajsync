#!/bin/bash

for jar in "libintf/build/libs/libintf-0.9.2.jar" \
    "yajsync-core/build/libs/yajsync-core-0.9.2.jar" \
    "yajsync-app/build/libs/yajsync-app-0.9.2.jar"
do
  cp -v $jar /home/klaas/Projects/RsyncServerApp/app/libs/
done
