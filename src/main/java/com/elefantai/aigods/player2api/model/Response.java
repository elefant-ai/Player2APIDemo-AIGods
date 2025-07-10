package com.elefantai.aigods.player2api.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;


public class Response {
    @Nullable
    public String message;
    public UUID id;
    @SerializedName("command")
    @Nullable
    public List<CommandResponse> commands;
    @Nullable
    public String audio;
}
