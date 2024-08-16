package com.example.benderbluetooth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResponseParser {

    public static String extractReplyCode(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        return jsonObject.getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }
}
