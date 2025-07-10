package com.elefantai.aigods.player2api.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class CommandResponse {
    public String name;

    private String arguments;

    public <T> T arguments(Class<T> type) {
        return new Gson().fromJson(arguments,type);
    }

}
