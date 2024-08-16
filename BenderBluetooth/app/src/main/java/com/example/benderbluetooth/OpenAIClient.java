package com.example.benderbluetooth;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OpenAIClient {

    private static final String TAG = "OpenAIClient";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "";

    private final OkHttpClient client;
    private final List<JsonObject> conversationHistory;

    public OpenAIClient() {
        client = new OkHttpClient();
        conversationHistory = new ArrayList<>();
        PromptBuilder pb = new PromptBuilder();
        addMessageToHistory("system",pb.buildSystemPrompt());
        addMessageToHistory("assistant","0031");

    }

    public void addMessageToHistory(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        conversationHistory.add(message);
    }

    public void makePostRequest(ReplyCallback callback) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");

        JsonArray messages = new JsonArray();
        for (JsonObject message : conversationHistory) {
            messages.add(message);
        }
        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .header("Authorization", "Bearer " + API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed", e);
                callback.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Log.d(TAG, "Response: " + jsonResponse);
                    String completionText = ResponseParser.extractReplyCode(jsonResponse);
                    callback.onReplyFound(completionText);
                } else {
                    Log.e(TAG, "Request failed with code: " + response.code());
                    callback.onError();
                }
            }
        });
    }

    public interface ReplyCallback {
        void onReplyFound(String replyCode);
        void onError();
    }
}
