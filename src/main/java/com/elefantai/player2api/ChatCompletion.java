package com.elefantai.player2api;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatCompletion {
    private static final String API_BASE_URL = "http://127.0.0.1:4315/v1/chat/completions";

    public static JsonObject getResponse(ConversationHistory conversationHistory) throws Exception {
        JsonObject requestBody = new JsonObject();
        JsonArray messagesArray = new JsonArray();
        for (JsonObject msg : conversationHistory.getListJSON()) {
            messagesArray.add(msg);
        }
        requestBody.add("messages", messagesArray);

        URL url = new URL(API_BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "application/json; charset=utf-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // PARSING
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray choices = jsonResponse.getAsJsonArray("choices");

        if (choices != null && !choices.isEmpty()) {
            JsonObject messageObject = choices.get(0).getAsJsonObject().getAsJsonObject("message");

            if (messageObject != null && messageObject.has("content")) {
                String content = messageObject.get("content").getAsString();

                content = content.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();

                return JsonParser.parseString(content).getAsJsonObject();
            }
        }

        throw new Exception("Invalid response format: " + response.toString());
    }
}