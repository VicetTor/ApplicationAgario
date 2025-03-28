package org.example.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;

public class Protocol {
    private static final Gson gson = new GsonBuilder().create();

    public static String createMessage(String type, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("data", data);
        return gson.toJson(message);
    }

    public static Map<String, Object> parseMessage(String json) {
        return gson.fromJson(json, Map.class);
    }
}
