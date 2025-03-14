package com.elefantai.aigods;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private final List<JsonObject> conversationHistory = new ArrayList<>();

    public ConversationHistory(String initialSystemPrompt) {
        setBaseSystemPrompt(initialSystemPrompt); // Ensures system message always exists
    }

    public void addUserMessage(String userText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "user");
        objectToAdd.addProperty("content", userText);
        conversationHistory.add(objectToAdd);
        if (conversationHistory.size() > 100) {
            conversationHistory.removeFirst();
        }
    }

    /**
     * Sets or updates the first system message (base system prompt).
     * If there is no system message, it adds one at the start.
     *
     * @param newPrompt The new base system prompt.
     */
    public void setBaseSystemPrompt(String newPrompt) {
        if (!conversationHistory.isEmpty() && "system".equals(conversationHistory.getFirst().get("role").getAsString())) {
            conversationHistory.getFirst().addProperty("content", newPrompt);
        } else {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", newPrompt);
            conversationHistory.addFirst( systemMessage);
        }
    }

    /**
     * Adds a new system message at the end of the conversation history.
     *
     * @param systemText The system message to add.
     */
    public void addSystemMessage(String systemText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "system");
        objectToAdd.addProperty("content", systemText);
        conversationHistory.add(objectToAdd);
    }

    public List<JsonObject> getListJSON() {
        return conversationHistory;
    }
}
