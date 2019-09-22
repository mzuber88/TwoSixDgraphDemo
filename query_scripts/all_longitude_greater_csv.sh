#!/bin/sh

curl -H "Content-Type: application/graphql+-" localhost:8080/query -XPOST -d $'
{
   me(func: has(Longitude)) @filter(ge(Longitude, -73.75)) {
    Descriptor
    Borough
    Longitude
    Latitude
  }
}
' | python -m json.tool | less
