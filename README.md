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

>     data_conversion_scripts/insert_json_311_data_dgraph.sh ./data/fhrw-4uyv_dgraph.json

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

Sample Java Query App
------------
A small java app to Qeury the dgraph injected with the json data from above is here: [Query311.java](Query311Java/src/main/java/Query311.java)

It uses the dgraph4j library: <https://github.com/dgraph-io/dgraph4j>

The app does the same queries as the scripts all\_dates\_greater\_json.sh and all\_longitude\_latitude\_greater\_json.sh above.

Requirements: Java 8, gradle, maven

To Build and Run:
>     cd Query311Java
>     ./gradlew run

Putting It All Together
------------
1.  Clone this repo
>     git clone git@github.com:mzuber88/TwoSixDgraphDemo.git
>     cd TwoSixDgraphDemo

2.   If your docker containers aren't running start them up with: [docker-compose.yml](docker-compose.yml)
>       docker-compose up -d

3.  Delete any dgraph data you have in running dgraph containers and restart the containers. **Warning: This will delete all your dgraph data on running container instances** 
>     ./data_conversion_scripts/delete_all_dgraph_data.sh
>     ./data_conversion_scripts/shutdown_and_restart_dgraph.sh

4.  Convert the included [fhrw-4uyv.json](data/fhrw-4uyv.json) into the format expected by dgraph.
>     ./data_conversion_scripts/convert_311_json_to_dgraph_json.sh ./data/fhrw-4uyv.json

5.   Inject the converted json into dgraph.
>     ./data_conversion_scripts/insert_json_311_data_dgraph.sh ./data/fhrw-4uyv_dgraph.json

6.   Update the dgraph schema with some types and indicies so we can query.
>     ./data_conversion_scripts/alter_schema_json.sh

7.   Build and run the Sample Java app that does two queries and prints the results:
>     cd Query311Java
>     ./gradlew run

Program output:

```
> Task :run 
Query nodes that have agency property and closed date > 2018, return predicates descriptor, borough, latitude, and longitude
Raw Query:{me(func: has(agency)) @filter(ge(closed_date, "2018")) {descriptor borough latitude longitude}}
Response:
{
  "me": [
    {
      "descriptor": "Failure To Retain Water/Improper Drainage- (LL103/89)",
      "borough": "QUEENS",
      "latitude": 40.710692,
      "longitude": -73.830549
    },
    {
      "descriptor": "Illegal. Commercial Use In Resident Zone",
      "borough": "QUEENS",
      "latitude": 40.738545,
      "longitude": -73.830510
    }
  ]
}

Query nodes that have borough property and are located at a longitude > -74 and latitude > 40.9, return predicates descriptor, borough, latitude, and longitude
Raw Query:{me(func: has(borough)) @filter(ge(longitude, -74) AND ge(latitude, 40.9) ) {descriptor borough latitude longitude}}
Response:
{
  "me": [
    {
      "descriptor": "Noise: air condition/ventilation equipment (NV1)",
      "borough": "BRONX",
      "latitude": 40.902675,
      "longitude": -73.851033
    },
    {
      "descriptor": "Overnight Commercial Storage",
      "borough": "BRONX",
      "latitude": 40.900435,
      "longitude": -73.840696
    },
    {
      "descriptor": "HEAT",
      "borough": "BRONX",
      "latitude": 40.900576,
      "longitude": -73.863022
    }
  ]
}
```