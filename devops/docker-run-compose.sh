#!/usr/bin/env bash
export ENV=local
export DOCKER_REGISTRY=localhost:5000
export APP_VERSION=0.0.1-SNAPSHOT
docker-compose -f docker-compose-app.yml up -d