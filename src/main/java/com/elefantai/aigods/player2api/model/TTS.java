package com.elefantai.aigods.player2api.model;

import com.google.gson.annotations.SerializedName;

public class TTS {
    public float speed = 0.25f;
    @SerializedName("audio_format")
    public String audioFormat = "wav";


    public TTS() {}
}
