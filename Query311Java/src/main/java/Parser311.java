import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser311 {
    private String pathTo311Json;
    public Parser311(String pathTo311Json) {
        this.pathTo311Json = pathTo311Json;
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
}
