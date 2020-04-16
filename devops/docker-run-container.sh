#!/usr/bin/env bash
#启动单个容器
DOCKER_REGISTRY=localhost:5000
APP_VERSION=0.0.1-SNAPSHOT

#因为这build需要一个过程，run的时候可能还没build好，所以只能手动build，然后在运行
#mvn clean install dockerfile:build
#构造imageName
imageName=`${DOCKER_REGISTRY}/springboot-docker:${APP_VERSION}`
#过滤image获取imageId
imageId=`docker images ${imageName} -q`
#just run -d后台运行
docker run -p 8080:8080 ${imageId} -d

#过滤镜像 docker images -f
#删除tag为空的镜像
#$ docker images -f dangling=true
#$ docker rmi $(docker images -q -f dangling=true)
