#!/usr/bin/env bash
test -t 1 && USE_TTY="-t"
docker -H newsgitlab.meishubao.com:2375 run -i ${USE_TTY} --rm --name maven-ai-app-service-account \
-v /var/run/docker.sock:/var/run/docker.sock \
-v "/root/.m2/repository:/root/.m2/repository" \
-v "/root/.m2/wrapper:/root/.m2/wrapper" \
-v "/data/aicode/ai-app-service-account:/usr/src" \
-w /usr/src \
--add-host newsgitlab.meishubao.com:10.10.1.50 \
--add-host msb-ai.meixiu.mobi:10.10.1.34 \
--add-host docker.meixiu.mobi:10.10.1.32 \
msb-ai.meixiu.mobi:5000/maven:3-jdk-8-7 sh -c "$1"
