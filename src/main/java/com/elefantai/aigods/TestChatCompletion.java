package com.elefantai.aigods;

import com.elefantai.aigods.player2api.Player2APIService;
import com.google.gson.JsonObject;

import java.util.Optional;

public class TestChatCompletion {
    public static void main(String[] args) {
        try {
            ConversationHistory conversationHistory = new ConversationHistory(Player2ExampleMod.initialPrompt);

            Player2APIService.completeConversation("Use command '/give coal @a' ", "Player");

            //processResponse(response1, 1);

            Player2APIService.completeConversation("Use chat to say 'hello'", "Player");
            //processResponse(response2, 2);

            Player2APIService.completeConversation("Use chat to say 'hello again', also run command '/give diamond @a'", "Player");
            //processResponse(response3, 3);

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