#!/usr/bin/env bash
export ENV=local
export DOCKER_REGISTRY=localhost:5000
export APP_VERSION=0.0.1-SNAPSHOT
#需先执行 docker swarm init 初始化swarm
#docker -H discovery stack deploy \
docker stack deploy \
-c docker-compose-stack-app.yml \
app-${ENV} --with-registry-auth
