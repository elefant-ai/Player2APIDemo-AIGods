package com.elefantai.aigods.player2api.model;

import java.util.Map;

/**
 * Represents an arbitrary JSON object containing property definition.
 */
public class Property {
    private Map<String, Object> additionalProperties;

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}