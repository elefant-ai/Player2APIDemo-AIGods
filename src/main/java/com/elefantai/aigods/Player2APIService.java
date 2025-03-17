package com.elefantai.aigods;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    private static Map<String, JsonElement> sendRequest(String endpoint, boolean postRequest, JsonObject requestBody) throws Exception {
        URL url = new URI(BASE_URL + endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(postRequest ? "POST" : "GET");

        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("accept", "application/json; charset=utf-8");

        if (postRequest && requestBody != null) {
            System.out.println("Sending post request to " + endpoint + ": " +  requestBody.toString());
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }


        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            // read error info:
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("Error response: " + errorResponse);
            }
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
                    conversationHistory.addAssistantMessage(content);
                    return Utils.parseCleanedJson(content);
                }
            }
        }

        throw new Exception("Invalid response format: " + responseMap.toString());
    }

    /**
     * Fetches the first selected character from the API and returns it as a Character object.
     *
     * @return A Character object containing the character's details.
     */
    public static Character getSelectedCharacter() {
        // TODO: Maybe update later? this just takes the top character.
        try {
            Map<String, JsonElement> responseMap = sendRequest("/v1/selected_characters", false, null);

            if (!responseMap.containsKey("characters")) {
                throw new Exception("No characters found in API response.");
            }
            JsonArray charactersArray = responseMap.get("characters").getAsJsonArray();
            if (charactersArray.isEmpty()) {
                throw new Exception("Character list is empty.");
            }

            JsonObject firstCharacter = charactersArray.get(0).getAsJsonObject();

            String name = Utils.getStringJsonSafely(firstCharacter, "short_name");
            if (name == null) {
                throw new Exception("Character is missing 'short_name'.");
            }

            String greeting = Utils.getStringJsonSafely(firstCharacter, "greeting");
            String description = Utils.getStringJsonSafely(firstCharacter, "description");
            String[] voiceIds = Utils.getStringArrayJsonSafely(firstCharacter, "voice_ids");
            return new Character(name, greeting, description, voiceIds);
        } catch (Exception e) {
            System.err.println("Warning, getSelectedCharacter failed, reverting to default. Error message: " + e.getMessage());
            return new Character("AI god", "Greetings", "You are a helpful AI God", new String [0]);
        }
    }


    public static void textToSpeech(String message, Character character){
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("play_in_app", true);
            requestBody.addProperty("speed", 1);
            requestBody.addProperty("text", message );
            JsonArray voiceIdsArray = new JsonArray();
            for (String voiceId : character.voiceIds) {
                voiceIdsArray.add(voiceId);
            }
            requestBody.add("voice_ids", voiceIdsArray);

            sendRequest("/v1/tts/speak", true, requestBody);

        } catch (Exception ignored) {
        }
    }

    public static void startSTT(){
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("timeout", 30);
        try {
            sendRequest("/v1/stt/start", true, requestBody);
        } catch (Exception e) {
            System.err.println("Error in startSST: " + e.getMessage());
        }
    }


    // todo: Add comment
    public static String stopSTT () {
        try{
            Map<String, JsonElement> responseMap = sendRequest("/v1/stt/stop", true, null);
            if(!responseMap.containsKey("text")){
                throw new Exception("Could not find key 'text' in response");
            }
            return responseMap.get("text").getAsString();
        } catch (Exception e) {
            // handle timeout err here?
            return e.getMessage();
        }
    }
}
