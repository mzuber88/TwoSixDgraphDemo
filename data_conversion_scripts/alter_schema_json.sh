#!/bin/sh

curl localhost:8080/alter -XPOST -d $'
  latitude: float @index(float) .
  longitude: float @index(float) .
  closed_date: datetime @index(year) .
' | python -m json.tool | less
