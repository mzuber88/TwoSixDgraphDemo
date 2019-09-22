#!/bin/sh

#Replace csv header line spaces with _
sed '1s/ /_/g' $1 > $1_no_space

#convert csv to json using node module csvtojson
./node_modules/.bin/csvtojson $1_no_space --ignoreEmpty=true > $1.json

#use jq to add needed set root
cat $1.json | jq '{ set: . }' > $1.json.jq

mv $1.json.jq $1.json
rm $1_no_space


