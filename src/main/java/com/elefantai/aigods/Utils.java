package com.elefantai.aigods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

public class Utils {
    // TODO: Add comments
    public static String replacePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "\\{\\{" + entry.getKey() + "}}";  // Escape {{hole}}
            input = input.replaceAll(placeholder, entry.getValue());
        }
        return input;
    }

    public static String getStringJsonSafely(JsonObject input, String fieldName) {
        return (input.has(fieldName) && !input.get(fieldName).isJsonNull())
                ? input.get(fieldName).getAsString()
                : null;
    }

    /**
     * Removes Markdown-style code block formatting (```json ... ```) and parses the JSON.
     *
     * @param content The raw string content from the LLM response.
     * @return The cleaned JSON object.
     * @throws JsonSyntaxException If the content is not valid JSON.
     */
    public static JsonObject parseCleanedJson(String content) throws JsonSyntaxException {
        content = content.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();

        return JsonParser.parseString(content).getAsJsonObject();
    }


    public static String[] splitLinesToArray(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }
        return input.split("\\R+"); // \\R is any of: (\n, \r, \r\n)
    }

}
