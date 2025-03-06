package com.elefantai.player2api;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TestChatCompletion {
    public static void main(String[] args) {
        try {
            List<JsonObject> conversationHistory = new ArrayList<>();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "You are a helpful assistant in a Minecraft server.");
            conversationHistory.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", "Give everyone diamonds!");
            conversationHistory.add(userMessage);

            String response = ChatCompletion.getResponse(conversationHistory);
            System.out.println("AI Response: " + response);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
