#!/bin/sh

curl -H "Content-Type: application/graphql+-" localhost:8080/query -XPOST -d $'
{
  me(func: has(agency)) @filter(ge(closed_date, "2018")) {
    descriptor
    borough
    latitude
    longitude
  }
}
' | python -m json.tool | less
