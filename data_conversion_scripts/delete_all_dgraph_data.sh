#!/bin/sh

echo "Deleting all dgraph data!"
container_id=`docker container ls | grep server | grep dgraph | cut -f1 -d ' '`
docker exec -it $container_id /bin/rm -rf p  w  zw
