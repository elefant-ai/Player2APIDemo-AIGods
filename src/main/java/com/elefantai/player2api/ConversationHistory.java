package com.elefantai.player2api;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private final List<JsonObject> conversationHistory = new ArrayList<>();

    public ConversationHistory(String initialSystemPrompt) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", initialSystemPrompt);
        conversationHistory.add(systemMessage);
    }

    public void addUserMessage(String userText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "user");
        objectToAdd.addProperty("content", userText);
        conversationHistory.add(objectToAdd);
    }

    public void addSystemMessage(String userText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "user");
        objectToAdd.addProperty("content", userText);
        conversationHistory.add(objectToAdd);
    }

    public List<JsonObject> getListJSON(){
        return conversationHistory;
    }
}
