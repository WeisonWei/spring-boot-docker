#!/usr/bin/env bash
skipDocker="-Ddockerfile.skip=true"
skipTest="-Dmaven.test.skip=true"
skipDoc="-Dmaven.javadoc.skip=true"

enable_params="meixiu-nexus-repo,meixiu-docker-registry,ai-app-test"
disable_params="!devlab2-nexus-repo,!devlab2-docker-registry"
params="-DskipTests -P ${enable_params},${disable_params}"
params1="${skipDocker} ${skipTest} ${params}"
params2="${skipTest} ${skipDoc} ${params}"

./mvnw -B release:clean release:prepare -Darguments="${params1}" release:perform -Darguments="${params2}"

