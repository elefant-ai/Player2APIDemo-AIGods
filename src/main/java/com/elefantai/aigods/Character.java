package com.elefantai.aigods;

import java.util.Arrays;

public class Character {
    public final String name;
    public final String greetingInfo;
    public final String description;
    public final String[] voiceIds;

    /**
     * Constructs a Character instance with provided values.
     *
     * @param characterName The name of the character.
     * @param greetingInfo A description of what the greeting should look like. Note this is not what the actual greeting should be, but rather a prompt that can add to it.
     * @param voiceIds An array of voice IDs associated with the character.
     */
    public Character(String characterName, String greetingInfo, String description, String[] voiceIds) {
        this.name = characterName;
        this.greetingInfo = greetingInfo;
        this.voiceIds = voiceIds;
        this.description = description;
    }


    /**
     * Returns a formatted string representation of the Character object.
     *
     * @return A string containing character details.
     */
    @Override
    public String toString() {
        return String.format(
                "Character{name='%s', greeting='%s', voiceIds=%s}",
                name,
                greetingInfo,
                Arrays.toString(voiceIds)
        );
    }

}