package com.elefantai.aigods.player2api.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;

public class SpawnNPC {
    @SerializedName("character_description")
    private String characterDescription;

    private List<Function> commands;

    private String name;

    @SerializedName("short_name")
    private String shortName;

    @SerializedName("system_prompt")
    private String systemPrompt;

    @SerializedName("voice_id")
    private String voiceId;

    // Constructor
    public SpawnNPC(String characterDescription, List<Function> commands, String name, String shortName, String systemPrompt, String voiceId) {
        this.characterDescription = characterDescription;
        this.commands = commands;
        this.name = name;
        this.shortName = shortName;
        this.systemPrompt = systemPrompt;
        this.voiceId = voiceId;
    }

    // Getters and setters
    public String getCharacterDescription() {
        return characterDescription;
    }

    public void setCharacterDescription(String characterDescription) {
        this.characterDescription = characterDescription;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}