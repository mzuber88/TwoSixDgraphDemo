#!/bin/sh

curl localhost:8080/alter -XPOST -d $'
  Latitude: float @index(float) .
  Longitude: float @index(float) .
' | python -m json.tool | less
