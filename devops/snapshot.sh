#!/usr/bin/env bash
skipDocker="-Ddockerfile.skip=true"
skipTest="-Dmaven.test.skip=true"
skipDoc="-Dmaven.javadoc.skip=true"

enable_params="meixiu-nexus-repo,meixiu-docker-registry,ai-app-dev"
disable_params="!devlab2-nexus-repo,!devlab2-docker-registry"
params="-DskipTests -P ${enable_params},${disable_params}"

./mvnw clean deploy ${params}



