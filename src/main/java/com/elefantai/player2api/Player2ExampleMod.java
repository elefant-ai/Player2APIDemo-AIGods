package com.elefantai.player2api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod {
    public static final String MODID = "player2api";
    private static final String API_BASE_URL = "http://localhost:4315/v1/chat/completions";

    private final List<JsonObject> conversationHistory = new ArrayList<>();

    public Player2ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);

        // Initial System Prompt
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are an AI assistant in a Minecraft server. You can either execute a command (starting with '/') or send a chat message.");
        conversationHistory.add(systemMessage);
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        final String message = event.getMessage().getString();
        MinecraftServer server = player.getServer();

        // Append user message to history
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversationHistory.add(userMessage);

        // Send the conversation to the LLM
        try {
            String responseText = getLLMResponse();
            if (responseText.startsWith("/")) {
                // If response starts with "/", it's a command
                if (server != null) {
                    sendCommand(server, responseText.substring(1)); // Remove "/"
                }
            } else {
                // Otherwise, send as a chat message
                sendChat(player, responseText);
            }
        } catch (Exception e) {
            sendChat(player, "Error communicating with AI: " + e.getMessage());
        }
    }

    private String getLLMResponse() throws Exception {
        // Create JSON payload
        JsonObject requestBody = new JsonObject();
        JsonArray messagesArray = new JsonArray();
        for (JsonObject msg : conversationHistory) {
            messagesArray.add(msg);
        }
        requestBody.add("messages", messagesArray);

        // SEND REQUEST
        URL url = new URL(API_BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);


        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }


        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // PARSE JSON RESPONSE
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray choices = jsonResponse.getAsJsonArray("choices");
        if (!choices.isEmpty()) {
            JsonObject messageObject = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            return messageObject.get("content").getAsString();
        } else {
            throw new Exception("No response from AI");
        }
    }

    private void sendCommand(MinecraftServer server, String command) {
        CommandSourceStack commandSource = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(commandSource, command);
    }

    private void sendChat(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
