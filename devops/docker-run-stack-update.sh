#!/usr/bin/env bash
export ENV=local
export DOCKER_REGISTRY=localhost:5000
export APP_VERSION=0.0.1-SNAPSHOT
#需先执行 docker swarm init 初始化swarm
#docker -H discovery stack deploy \
image=${DOCKER_REGISTRY}/springboot-docker:${APP_VERSION}
docker services ls --name ${image}
docker stack update app_${service} --image ${image}


image=${prefix}/${service}:${version}

target=`docker -H ${host} service ls -f name="${profile}_${service}" -q`
if [[ ! -z ${target} ]]; then
  docker -H ${host} service update ${profile}_${service} --image ${image}
else
  echo "not find service ${profile}_${service}"
fi