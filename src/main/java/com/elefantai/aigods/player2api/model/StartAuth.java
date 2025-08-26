package com.elefantai.aigods.player2api.model;

import com.google.gson.annotations.SerializedName;

public class StartAuth {
    @SerializedName("client_id")
    public String clientId;

    public StartAuth(String clientId) {
        this.clientId = clientId;
    }
}
