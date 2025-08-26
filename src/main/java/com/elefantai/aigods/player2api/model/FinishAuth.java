package com.elefantai.aigods.player2api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request payload for polling the device authorization token endpoint.
 */
public class FinishAuth {
    @SerializedName("client_id")
    public String clientId;

    @SerializedName("device_code")
    public String deviceCode;

    @SerializedName("grant_type")
    public String grantType;

    public FinishAuth(String clientId, String deviceCode) {
        this.clientId = clientId;
        this.deviceCode = deviceCode;
        this.grantType = "urn:ietf:params:oauth:grant-type:device_code";
    }
}