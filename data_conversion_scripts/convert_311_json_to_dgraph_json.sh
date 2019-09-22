#!/bin/sh

file_prefix=`echo "$1" | cut -f 1 -d '.'`

#remove @ because dgraph takes these as language directives
sed 's/@//g' $1 > ${file_prefix}_converted.json

cat ${file_prefix}_converted.json | jq '{ set: . }' > ${file_prefix}_dgraph.json

rm ${file_prefix}_converted.json

