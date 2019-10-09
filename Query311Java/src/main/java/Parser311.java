import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class Parser311 {
    private String pathTo311Json;
    public Parser311(String pathTo311Json) {
        this.pathTo311Json = pathTo311Json;
    }

    private static String valueToDgraphUID(String value)
    {
        return "_:" + value.toLowerCase();
    }

    private static void insertUniqueDgraphNodes(JsonWriter writer, Map<String, Map<String, Integer>> hmap, List<String> edgeFilter) throws IOException
    {
        for (Map.Entry<String, Map<String, Integer>> entry : hmap.entrySet()) {
            String key = entry.getKey();

            if (edgeFilter.size() == 0 || edgeFilter.contains(key)) {
                for (Map.Entry<String, Integer> nameEntry : entry.getValue().entrySet()) {
                    writeJsonStringAsUniqueDgraphNode(key,nameEntry.getKey(),writer);

                }
            }
        }
    }

    private static void writeJsonStringAsUniqueDgraphNode(String key, String value, JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("uid");
        writer.value(valueToDgraphUID(value));
        writer.name(key + "_value");
        writer.value(value);
        writer.endObject();
    }

    private static void writeJsonStringAsDgraphEdgeLink(String value, JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("uid");
        writer.value(valueToDgraphUID(value));
        writer.endObject();
    }

    private static void insertEdgesWork(JsonReader reader, JsonWriter writer, Map<String, Map<String, Integer>> hmap, List<String> edgeFilter) throws IOException {
        /*
            {
                "uid": "_:manhattan",
                "name":"MANHATTAN"
            },
         */
        // "borough": { "uid": "_:manhattan" },
        // "borough": "MANHATTAN",
        Map<String,Integer> lastNameMap = null;
        String lastName = "";
        boolean processedSet = false;
        while (true) {
            JsonToken token = reader.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    reader.beginArray();
                    writer.beginArray();
                    if(!processedSet && lastName.equals("set") )
                    {
                        processedSet = true;
                        insertUniqueDgraphNodes(writer,hmap,edgeFilter);
                    }
                    break;
                case END_ARRAY:
                    reader.endArray();
                    writer.endArray();
                    break;
                case BEGIN_OBJECT:
                    reader.beginObject();
                    writer.beginObject();
                    break;
                case END_OBJECT:
                    reader.endObject();
                    writer.endObject();
                    break;
                case NAME:
                    String name = reader.nextName();
                    writer.name(name);
                    lastName = name;
                    if(edgeFilter.size() == 0 || edgeFilter.contains(name))
                        lastNameMap = hmap.get(name);
                    else
                        lastNameMap = null;
                    break;
                case STRING:
                    String s = reader.nextString();
                    if(lastNameMap != null && lastNameMap.get(s) != null)
                        writeJsonStringAsDgraphEdgeLink(s,writer);
                    else
                        writer.value(s);
                    break;
                case NUMBER:
                    String n = reader.nextString();
                    writer.value(new BigDecimal(n));
                    break;
                case BOOLEAN:
                    boolean b = reader.nextBoolean();
                    writer.value(b);
                    break;
                case NULL:
                    reader.nextNull();
                    writer.nullValue();
                    break;
                case END_DOCUMENT:
                    return;
            }
        }
    }

    private void process311Object(JsonReader jsonReader, Map<String, Map<String, Integer>> hmap) throws Exception
    {
        jsonReader.beginObject();

        while (jsonReader.hasNext() && !JsonToken.END_OBJECT.equals(jsonReader.peek())) {
            String name = jsonReader.nextName();

            if (!hmap.containsKey(name)) {
                Map<String, Integer> fieldMap = new HashMap<>();
                if (JsonToken.STRING.equals(jsonReader.peek())) {
                    String value = jsonReader.nextString();
                    fieldMap.put(value, 1);
                    hmap.put(name, fieldMap);
                } else {
                    jsonReader.skipValue();
                }
            } else {
                Map<String, Integer> fieldMap = hmap.get(name);
                if (JsonToken.STRING.equals(jsonReader.peek())) {
                    String value = jsonReader.nextString();
                    if (fieldMap.containsKey(value)) {
                        Integer count = fieldMap.get(value);
                        fieldMap.put(value, count + 1);
                    } else {
                        fieldMap.put(value, 1);
                    }
                } else {
                    jsonReader.skipValue();
                }

            }
        }

        jsonReader.endObject();
    }

    public Map<String, Map<String, Integer>> generate311HashTable() throws Exception {
        if(!isValid311DgraphJson())
            throw new Exception("Invalid Dgraph Json");

        Map<String, Map<String, Integer>> hmap = new HashMap<>();
        Gson gson = new Gson();
        BufferedReader br;
        br = new BufferedReader(new FileReader(this.pathTo311Json));
        JsonReader jsonReader = gson.newJsonReader(br);

        //go past set
        jsonReader.beginObject();
        jsonReader.nextName();
        jsonReader.beginArray();

        while(jsonReader.hasNext() && JsonToken.BEGIN_OBJECT.equals(jsonReader.peek())) {
            process311Object(jsonReader,hmap);
        }

        return hmap;
    }

    private boolean rootHasSetArray(JsonReader jsonReader)
    {
        try {
            jsonReader.beginObject();
            String name = jsonReader.nextName();
            if(!name.equals("set")) return false;
            if(!JsonToken.BEGIN_ARRAY.equals(jsonReader.peek())) return false;
            jsonReader.skipValue();
            jsonReader.endObject();
            JsonToken nextToken = jsonReader.peek();
            if(!JsonToken.END_DOCUMENT.equals(jsonReader.peek())) return false;

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean isValid311DgraphJson() {
        Gson gson = new Gson();
        BufferedReader br;

        try {
            br = new BufferedReader(
                    new FileReader(this.pathTo311Json));
        } catch (FileNotFoundException e) {
            return false;
        }

        JsonReader jsonReader = gson.newJsonReader(br);

        //dgraph root key must be set which is an array
        return rootHasSetArray(jsonReader);
    }

    public Map<String, Map<String, Integer>> pruneUniqueNodesFrom311HashMap(Map<String, Map<String, Integer>> hmap) {
        Map<String, Map<String, Integer>> filtered = new HashMap<>();
        hmap.forEach((key, value) -> {
                    Map<String, Integer> pruned = value.entrySet().stream().filter(e -> e.getValue() > 1)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    if (pruned.size() > 0) filtered.put(key, pruned);
                }
        );
        return filtered;
    }

    public void insertEdgesUsingDuplicateMap(Map<String, Map<String, Integer>> hmap, List<String> edgeFilter) throws Exception
    {
        if(!isValid311DgraphJson())
            throw new Exception("Invalid Dgraph Json");

        if(edgeFilter == null)
            edgeFilter = new ArrayList<String>();

        String edgeFileName = this.pathTo311Json;
        String extension = "";
        int i = this.pathTo311Json.lastIndexOf('.');
        if (i > 0) {
            extension = this.pathTo311Json.substring(i+1);
            edgeFileName = this.pathTo311Json.substring(0,i);
            edgeFileName = edgeFileName + "_edges." + extension;
        }
        else {
            edgeFileName = edgeFileName + "_edges";
        }

        InputStream in = new FileInputStream(this.pathTo311Json);
        OutputStream out  = new FileOutputStream(edgeFileName);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
        writer.setIndent("  ");
        JsonReader reader = new JsonReader(new InputStreamReader(in));

        insertEdgesWork(reader,writer,hmap,edgeFilter);

        reader.close();
        writer.close();
    }
}
