package com.huawei.esight.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hyuan on 2017/6/30.
 */
public class JsonUtil {

  public static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(new TypeToken<Map>(){}.getType(),  new MapDeserializerDoubleAsIntFix())
      .registerTypeAdapter(new TypeToken<String>(){}.getType(),new StringDeserializer()).create();
  
  public static Map<String, Object> readAsMap(String json) throws IOException {
    
    return GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
  }
  
  public static String writeAsString(Map object) {
    return GSON.toJson(object);
  }
  
  public static class MapDeserializerDoubleAsIntFix 
      implements JsonDeserializer<Map<String, Object>> {
    
    @Override  @SuppressWarnings("unchecked")
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return (Map<String, Object>) read(json);
    }
    
    public Object read(JsonElement in) {
      
      if (in.isJsonArray()) {
        List<Object> list = new ArrayList<Object>();
        JsonArray arr = in.getAsJsonArray();
        for (JsonElement anArr : arr) {
          list.add(read(anArr));
        }
        return list;
      } else if (in.isJsonObject()) {
        Map<String, Object> map = new LinkedTreeMap<String, Object>();
        JsonObject obj = in.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
        for (Map.Entry<String, JsonElement> entry: entitySet) {
          map.put(entry.getKey(), read(entry.getValue()));
        }
        return map;
      } else if (in.isJsonPrimitive()) {
        JsonPrimitive prim = in.getAsJsonPrimitive();
        if (prim.isBoolean()) {
          return prim.getAsBoolean();
        } else if (prim.isString()) {
          return prim.getAsString();
        } else if (prim.isNumber()) {
          Number num = prim.getAsNumber();
          // here you can handle double int/long values
          // and return any type you want
          // this solution will transform 3.0 float to long values
          if (Math.abs(Math.ceil(num.doubleValue())-num.longValue()) < .0000001) {
            return num.intValue();
          } else {
            return num.doubleValue();
          }
        }
      }
      return null;
    }
  }
  
  public static class StringDeserializer implements JsonDeserializer<String> {
    
    @Override
    public String deserialize(JsonElement jsonElement, Type type, 
        JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
      return jsonElement.toString();
    }
  }
  
  /**
   * 对象转换为Map.
   * @param obj 待转换对象
   * @return map
   */
  public static Map<String,Object> object2Map(Object obj) {
    String jsonString;
    if (obj instanceof String) {
      jsonString = (String) obj;
    } else {
      jsonString = GSON.toJson(obj);
    }
    Map<String, Object> map = GSON.fromJson(jsonString, new TypeToken<Map<String, Object>>(){}.getType());
    return map;
  }
}
