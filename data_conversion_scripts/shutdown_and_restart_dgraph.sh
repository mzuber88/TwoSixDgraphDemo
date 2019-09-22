#!/bin/sh

container_ids=`docker container ls -f name=dgraph -aq`

echo "stopping dgraph containers"
docker container stop $container_ids

echo "starting dgraph containers"
docker container start $container_ids

#docker container stop $(docker container ls -q)
#docker container start 8b9c9d33e795 44afe905a9af 373ac1fe1b8b
