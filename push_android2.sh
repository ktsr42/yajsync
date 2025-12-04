#!/bin/bash

cp -v --target-directory=/home/klaas/Projects/RsyncServerApp/app/libs/ \
  --update=none-fail \
  libintf/build/libs/*.jar yajsync-core/build/libs/*.jar yajsync-app/build/libs/*.jar