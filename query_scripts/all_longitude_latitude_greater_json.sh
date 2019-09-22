#!/bin/sh

curl -H "Content-Type: application/graphql+-" localhost:8080/query -XPOST -d $'
{
   me(func: has(borough)) @filter(ge(longitude, -74) AND ge(latitude, 40.9) ) {
    descriptor
    borough
    latitude
    longitude
  }
}
' | python -m json.tool | less
