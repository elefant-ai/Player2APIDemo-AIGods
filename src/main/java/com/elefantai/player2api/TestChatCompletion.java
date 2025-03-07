package com.elefantai.player2api;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TestChatCompletion {
    public static void main(String[] args) {
        try {
            ConversationHistory conversationHistory = new ConversationHistory("You are an agent that can either chat in minecraft, or execute minecraft commands. For a user's message, respond with either a command or a message. Respond with only `/commandname parameters` if want to execute a command. Example commands include (not limited to):" + "['/give <target> <item> [<count>]', '/gamemode <gamemode> [<target>]'  ]. For targets use either the player's name or @a for all, @r for random player, @p for nearest player. ");

            conversationHistory.addUserMessage("Give everyone diamonds!");
            String response1 = ChatCompletion.getResponse(conversationHistory);
            conversationHistory.addSystemMessage(response1);
            System.out.println("AI Response 1: " + response1);

            conversationHistory.addUserMessage("Do the same thing again!");
            String response2 = ChatCompletion.getResponse(conversationHistory);
            conversationHistory.addSystemMessage(response2);
            System.out.println("AI Response 2: " + response2);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
