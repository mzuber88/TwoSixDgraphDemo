# TwoSixDgraphDemo

Task
------------
1.  Set up dgraph in docker
2.  Model 311 data as graph data
3.  Ingest into dgraph
4.  Write small example client to query dgraph in java and print results

Setting up dgraph in docker
------------
Followed <https://docs.dgraph.io/get-started/> to start up the 3 dgraph docker nodes

Created some helper shell scripts for working with dgraph in docker

[delete\_all\_dgraph\_data.sh](data_conversion_scripts/delete_all_dgraph_data.sh)
(deletes all dgraph storage folders in the docker container)

[shutdown\_and\_restart\_dgraph.sh](data_conversion_scripts/shutdown_and_restart_dgraph.sh)
(stops and restarts all docker dgraph containers)

Model 311 data as graph data and Ingesting into dgraph
------------
311 New York data located here: <https://data.cityofnewyork.us/Social-Services/311-Service-Requests-from-2010-to-Present/erm2-nwe9>

I originally started with the csv data (which contained 24 million rows), but then realized the data could be retreived already in json by querying the json api endpoints.  I will go over trying to use both data sets which had slightly different data (both in content and format).  Data files included in the github repository are truncated to 1000 items.

### Working with the CSV Data ###
[CSV Data here](data/311_Service_Requests.csv)

The CSV data needed to be converted and manipulated to json so it could be imported into dgraph.  It also wasn't as dgraph ready as the provided json data.  (Dates were in a format dgraph didn't understand, Location data wasn't automatically recognized as points, etc...)

#### Converting the CSV data to json ####
**Perquisites:**

1. csvtojson node module

>     npm install csvtojson --save

2.  jq command line json processor (used homebrew to install on mac)

>     brew install jq

[Script that does the conversion](data_conversion_scripts/convert_csv_to_dgraph_json.sh)

The script does the following:

1.  Replaces spaces with underscores in the csv headers since dgraph can't have spaces its' predicates.
2. Run the csvtojson node module and remove any empty data
3. Use jq to insert the required set root key needed for dgraph

The resulting .json file can be imported into dgraph using curl.

>     data_conversion_scripts/insert_json_311_data_dgraph.sh 311_Service_Requests.csv.json


### Working with the provided json data ###
The first 1000 items of json can be easily downloaded from here: <https://data.cityofnewyork.us/resource/fhrw-4uyv.json> also included in git repo here: [fhrw-4uyv.json](data/fhrw-4uyv.json)

The json data was already closer in format to what dgraph wanted.  Dates were in the correct parsable format and location data was already in the Point format so dgraph automatically applied the geo type once imported.

Some manipulation was still necessary so dgraph would take the data.
[Script that does the conversion](data_conversion_scripts/convert_311_json_to_dgraph_json.sh)

The script does the following:

1.    Removes @ symbols from the computed_region keys (dgraph takes these as language directives)
2.    Use jq to insert the required set root key needed by dgraph.

The resulting .json file can be imported into dgraph using curl.

>     data_conversion_scripts/insert_json_311_data_dgraph.sh fhrw-4uyv_dgraph.json

Updating the dgraph schema
------------

dgraph automatically generates a schema automatically applying some types if it can (It does this better with the fhrw-4uyv.json than the csv data).

The following scripts update the schema to set indicicies and types for some of the more interesting fields.  The json and csv data had slightly different fields, so there are two scripts, but the csv header data could have been processed to make them more the same.  The scripts update the schema using curl.

[csv\_starting\_point\_schema\_script](data_conversion_scripts/alter_schema_csv.sh)

[json\_starting\_point\_schema\_script](data_conversion_scripts/alter_schema_json.sh)

Sample Query Scripts
------------
I created some scripts to do some sample queries so I could verify the data was ingested into dgraph and that updating the schema worked.

[all\_longitude\_greater\_csv.sh](query_scripts/all_longitude_greater_csv.sh) 
(query when starting with the csv data, outputs Descriptor, Borough, Longitude and Latitude for all nodes with Longitude > -73.75)

[all\_dates\_greater\_json.sh](query_scripts/all_dates_greater_json.sh)
(query when starting with the json data, if the node has an agency predicate and the closed_date > 2018, outputs the descriptor, borough, latitude, and longitude)

[all\_longitude\_latitude\_greater\_json.sh](query_scripts/all_longitude_latitude_greater_json.sh)
(query when starting with the json data, if the node has a borough predicate and the longitude > -74 and the latitude > 40.9, outputs the descriptor, borough, latitude, and longitude)