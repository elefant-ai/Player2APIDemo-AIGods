package com.elefantai.aigods;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player2APIService {
    private static final String BASE_URL = "http://127.0.0.1:4315";

    /**
     * Handles boilerplate logic for interacting with the API endpoint
     *
     * @param endpoint The API endpoint (e.g., "/v1/chat/completions").
     * @param postRequest True -> POST request, False -> GET request
     * @param requestBody JSON payload to send.
     * @return A map containing JSON keys and values from the response.
     * @throws Exception If there is an error.
     */
    public static Map<String, JsonElement> sendRequest(String endpoint, boolean postRequest, JsonObject requestBody) throws Exception {
        URL url = new URI(BASE_URL + endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(postRequest ? "POST" : "GET");

        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "application/json; charset=utf-8");

        if (postRequest && requestBody != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        Map<String, JsonElement> responseMap = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonResponse.entrySet()) {
            responseMap.put(entry.getKey(), entry.getValue());
        }

        return responseMap;
    }


    /**
     * Handles a chat completion request using the AI API.
     *
     * @param conversationHistory The conversation history object.
     * @return The AI's response as a JSON object.
     * @throws Exception If there is an error.
     */
    public static JsonObject completeConversation(ConversationHistory conversationHistory) throws Exception {
        JsonObject requestBody = new JsonObject();
        JsonArray messagesArray = new JsonArray();

        for (JsonObject msg : conversationHistory.getListJSON()) {
            messagesArray.add(msg);
        }

        requestBody.add("messages", messagesArray);
        Map<String, JsonElement> responseMap = sendRequest("/v1/chat/completions", true, requestBody);

        if (responseMap.containsKey("choices")) {
            JsonArray choices = responseMap.get("choices").getAsJsonArray();

            if (!choices.isEmpty()) {
                JsonObject messageObject = choices.get(0).getAsJsonObject().getAsJsonObject("message");

                if (messageObject != null && messageObject.has("content")) {
                    String content = messageObject.get("content").getAsString();
                    conversationHistory.addSystemMessage(content);
                    return Utils.parseCleanedJson(content);
                }
            }
        }

        throw new Exception("Invalid response format: " + responseMap.toString());
    }

    /**
     * Fetches selected characters from the API.
     *
     * @return A list of JsonObjects, each containing character details (id, name, description, meta).
     * @throws Exception If there is an error.
     */
    public static List<JsonObject> getSelectedCharacters() throws Exception {
        Map<String, JsonElement> responseMap = sendRequest("/v1/selected_characters", false, null);

        List<JsonObject> characterList = new ArrayList<>();

        if (responseMap.containsKey("characters")) {
            JsonArray charactersArray = responseMap.get("characters").getAsJsonArray();

            for (JsonElement element : charactersArray) {
                if (element.isJsonObject()) {
                    characterList.add(element.getAsJsonObject());
                } else {
                    System.err.println("Skipping non-object character: " + element);
                }
            }
        }

        return characterList;
    }


}
