package com.elefantai.player2api;

import com.google.gson.JsonObject;

public class TestChatCompletion {
    public static void main(String[] args) {
        try {
            ConversationHistory conversationHistory = new ConversationHistory(Player2ExampleMod.initialPrompt);

            conversationHistory.addUserMessage("Use command '/give coal @a' ");
            JsonObject response1 = ChatCompletion.getResponse(conversationHistory);
            conversationHistory.addSystemMessage(response1.toString());
            processResponse(response1, 1);

            conversationHistory.addUserMessage("Use chat to say 'hello'");
            JsonObject response2 = ChatCompletion.getResponse(conversationHistory);
            conversationHistory.addSystemMessage(response2.toString());
            processResponse(response2, 2);

            conversationHistory.addUserMessage("Use chat to say 'hello again', also run command '/give diamond @a'");
            JsonObject response3 = ChatCompletion.getResponse(conversationHistory);
            conversationHistory.addSystemMessage(response3.toString());
            processResponse(response3, 3);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processResponse(JsonObject response, int responseNumber) {
        System.out.println("AI Response " + responseNumber + ": " + response.toString());
        if (response.has("command")) {
            String command = response.get("command").getAsString();
            System.out.println("Executing Command: " + command);
        }

        if (response.has("chat")) {
            String chatMessage = response.get("chat").getAsString();
            System.out.println("Chatting: " + chatMessage);
        }
    }
}