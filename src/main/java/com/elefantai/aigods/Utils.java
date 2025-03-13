package com.elefantai.aigods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

public class Utils {
    /**
     * Replaces placeholders in a string with corresponding values from a map.
     * Placeholders are of the form {{key}}.
     *
     * @param input The input string containing placeholders.
     * @param replacements A map containing keys and their corresponding replacement values.
     * @return The input string with placeholders replaced.
     */
    public static String replacePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "\\{\\{" + entry.getKey() + "}}";  // Escape {{hole}}
            input = input.replaceAll(placeholder, entry.getValue());
        }
        return input;
    }

    /**
     * Safely retrieves a string value from a JsonObject.
     * Returns null if the field does not exist or is null.
     *
     * @param input The JsonObject to extract the field from.
     * @param fieldName The name of the field to retrieve.
     * @return The string value of the field, or null if it does not exist or is null.
     */
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

    /**
     * Splits a multiline string into an array of strings, where each line is an element.
     * Handles different newline formats (\n, \r, \r\n).
     *
     * @param input The input string containing multiple lines.
     * @return A string array containing each line as an element. Returns an empty array if input is null or empty.
     */
    public static String[] splitLinesToArray(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }
        return input.split("\\R+"); // \\R is any of: (\n, \r, \r\n)
    }

}
