#!/usr/bin/env bash
host=${1}
profile=${2}
service=${3}
version=${4}
prefix=${5}
image=${prefix}/${service}:${version}

target=`docker -H ${host} service ls -f name="${profile}_${service}" -q`
if [[ ! -z ${target} ]]; then
  docker -H ${host} service update ${profile}_${service} --image ${image}
else
  echo "not find service ${profile}_${service}"
fi