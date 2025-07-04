package com.elefantai.aigods.player2api;

import com.elefantai.aigods.Character;
import com.elefantai.aigods.Utils;
import com.elefantai.aigods.player2api.model.*;
import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Player2APIService {
    @Nullable
    private static UUID currentNpcId;

    public static void setCurrentNpcId(UUID npcId) {
        currentNpcId = npcId;
    }

    public static UUID spawnNpc(SpawnNPC payload) {
        try {
            String path = "/v1/npc/games/ai-gods/npcs/spawn";
            URI uri = URI.create(BASE_URL + path);
            Gson gson = new Gson();
            String json = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("accept", "text/plain")
                    .header("player2-game-key", "ai-gods")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Unexpected status: " + response.statusCode());
            }

            currentNpcId = UUID.fromString(response.body().trim());
            return UUID.fromString(response.body().trim());
        } catch (Exception e) {
            System.err.println("Failed to spawn NPC: " + e.getMessage());
            return null;
        }
    }

    public static class JsonStreamListener {
        private final HttpClient client;
        private final Gson gson;
        private final Consumer<Response> onMessage;
        private final ConcurrentLinkedQueue<JsonObject> pendingMessages = new ConcurrentLinkedQueue<>();
        private CompletableFuture<Void> streamTask;

        public JsonStreamListener(Consumer<Response> onMessage) {
            this.client = HttpClient.newHttpClient();
            this.gson = new Gson();
            this.onMessage = onMessage;
        }

        public void startListening(String url) {
            if (streamTask != null && !streamTask.isDone()) {
                streamTask.cancel(true);
            }

            streamTask = CompletableFuture.runAsync(() -> {
                boolean finished = false;
                while (!finished) {
                    try {
                        listenToEndpoint(url);
                        finished = true;
                    } catch (Exception ignored) {
                    }
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ignored) {}
                }
            });
        }

        private void listenToEndpoint(String url) throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofDays(10))
                    .GET()
                    .build();

            HttpResponse<java.io.InputStream> response = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new RuntimeException("HTTP " + response.statusCode());
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    if (!line.trim().isEmpty()) {
                        try {
                            Response jsonObject = gson.fromJson(line, Response.class);
                            onMessage.accept(jsonObject);
                        } catch (Exception e) {
                            System.err.println("Failed to parse JSON: " + line);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // Call this from a client tick event or similar
        public void processPendingMessages() {
            JsonObject message;
            int processed = 0;
            // Process a few messages per tick to avoid lag
            while ((message = pendingMessages.poll()) != null && processed < 10) {
                handleJsonObject(message);
                processed++;
            }
        }

        private void handleJsonObject(JsonObject jsonObject) {
            // Your processing logic here - this runs on the main thread
            System.out.println("Received: " + jsonObject);

            // Example: send chat message to player
            if (FMLEnvironment.dist.isClient()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    // Process your JSON data here
                }
            }
        }

        public void stop() {
            if (streamTask != null) {
                streamTask.cancel(true);
            }
            pendingMessages.clear();
        }
    }

    public static class StreamEventHandler {
        private final JsonStreamListener streamListener;

        public StreamEventHandler(String gameId, Consumer<Response> onMessage) {
            this.streamListener = new JsonStreamListener(onMessage);
            this.streamListener.startListening("http://127.0.0.1:4315/v1/npc/games/"+ gameId + "/npcs/responses");
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                streamListener.processPendingMessages();
            }
        }
    }
    private static final String BASE_URL = "http://127.0.0.1:4315"; // ACTUAL
//    private static final String BASE_URL = "http://127.0.0.1:8080"; // PROXY


    /**
     * Handles boilerplate logic for interacting with the API endpoint
     *
     * @param endpoint The API endpoint (e.g., "/v1/chat/completions").
     * @param requestType HTTP method to use (e.g., "GET", "POST").
     * @param requestBody JSON payload to send.
     * @return A map containing JSON keys and values from the response.
     * @throws Exception If there is an error.
     */
    private static Map<String, JsonElement> sendRequest(String endpoint, String requestType, JsonObject requestBody) throws Exception {
        URL url = new URI(BASE_URL + endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestType);

        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("accept", "application/json; charset=utf-8");
        connection.setRequestProperty("player2-game-key", "ai-gods");

        System.out.printf("Sending %s request to %s\n", requestType, endpoint);


        if (requestBody != null) {
            System.out.printf("Request Body: %s\n", requestBody);
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
        System.out.printf("DONE %s request to %s \n", requestType, endpoint);

        return responseMap;
    }


    /**
     * Handles a chat completion request using the AI API.
     *
     * @param message Message you want to send.
     * @param senderName The name of the sender (e.g., "Player").
     * @return The AI's response as a JSON object.
     * @throws Exception If there is an error.
     */
    public static void completeConversation(String message, String senderName) throws Exception {
        JsonObject requestBody = new JsonObject();

        if (currentNpcId == null) {
            HashMap<String, Property> properties = new HashMap<>();
            HashMap<String, Object> property = new HashMap<>();
            property.put("type", "string");
            property.put("description", "The minecraft command you want to run, without a slash prefix");
            properties.put("command", new Property(property));

            currentNpcId = spawnNpc(new SpawnNPC("A helpful AI God", List.of(
                    new Function("minecraft_command", "Run any Minecraft command", new Parameters(properties, List.of("command")))),"AI God", "God","Greetings, You are an AI overlord", "test"));
        }



        requestBody.addProperty("sender_message", message);
        requestBody.addProperty("sender_name", senderName);
        //requestBody.addProperty("game_state_info", "N/A");

        try {
            String path = "/v1/npc/games/ai-gods/npcs/" + currentNpcId + "/chat";
            URI uri = URI.create(BASE_URL + path);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            HttpResponse<Void> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
            System.out.println(response);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Fetches the first selected character from the API and returns it as a Character object.
     *
     * @return A Character object containing the character's details.
     */
    public static com.elefantai.aigods.Character getSelectedCharacter() {
        // TODO: Maybe update later? this just takes the top character.
        try {
            Map<String, JsonElement> responseMap = sendRequest("/v1/selected_characters", "GET", null);

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
            return new com.elefantai.aigods.Character(name, greeting, description, voiceIds);
        } catch (Exception e) {
            System.err.println("Warning, getSelectedCharacter failed, reverting to default. Error message: " + e.getMessage());
            return new com.elefantai.aigods.Character("AI god", "Greetings", "You are a helpful AI God", new String [0]);
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

            sendRequest("/v1/tts/speak", "POST", requestBody);

        } catch (Exception ignored) {
        }
    }

    public static void startSTT(){
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("timeout", 30);

        try {
            sendRequest("/v1/stt/start", "POST", requestBody);
        } catch (Exception e) {
            System.err.println("Error in startSST: " + e.getMessage());
        }
    }


    // todo: Add comment
    public static String stopSTT () {
        try{
            Map<String, JsonElement> responseMap = sendRequest("/v1/stt/stop", "POST", null);
            if(!responseMap.containsKey("text")){
                throw new Exception("Could not find key 'text' in response");
            }
            return responseMap.get("text").getAsString();
        } catch (Exception e) {
            // handle timeout err here?
            return e.getMessage();
        }
    }
    public static void sendHeartbeat(){
        try{
            System.out.println("Sending Heartbeat");
            Map<String, JsonElement> responseMap = sendRequest("/v1/health", "GET", null);
            if(responseMap.containsKey("client_version")){
                System.out.println("Heartbeat Successful");
            }
        }
        catch(Exception e){
            System.err.printf("Heartbeat Fail: %s",e.getMessage());
        }
    }
}
