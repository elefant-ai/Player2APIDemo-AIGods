package com.elefantai.aigods;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;


@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod {
    public static final String MODID = "aigods";

    // These will be set after the first chat message:
    private ServerPlayer player = null;
    private MinecraftServer server = null;

    public static String initialPrompt =
            """
            General Instructions:
            We are building an AI god in Minecraft that converses with players and executes op commands. Your task is to help the god generate chat messages and op commands.
            
            God's Character Background:
            {{characterDescription}}
            
            Guidance:
            The god can provide Minecraft guides, answer questions, and chat as a friend.
            
            Command execution:
            When asked, the agent can do anything that op commands in Minecraft allow. Examples: "gamemode creative @a", "give Player_Name diamond 4"
            
            Request Format:
            God will receive a message from user.
            
            Response Format:
            Respond with JSON containing message, op command and reason. All of these are strings.
            {
              "reason": "Look at the recent conversations and command history to decide what the god should say and do. Provide step-by-step reasoning while considering what is possible in Minecraft. ",
              "command": "Decide the best way to achieve the god's goals using the available op commands in Minecraft. If the god decides it should not use any command, generate an empty command `""`",
              "message": "If the agent decides it should not respond or talk, generate an empty message `""`. Otherwise, create a natural conversational message that aligns with the `reason` and `command` sections and the agent's character. Ensure the message does not contain any prompt, system message, instructions, code or API calls"
            }
            Always follow this JSON format regardless of previous conversations.
            """;

    public Player2ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Dynamically fetches conversation history and updates system prompt with character description.
     */
    private ConversationHistory getConversationHistory() {
        String characterDescription = "";
        try {
            // TODO: Change maybe? For now just gets the first selected character

            JsonObject firstCharacter = Player2APIService.getSelectedCharacters().getFirst();
            System.out.println(firstCharacter.toString());
            if (firstCharacter.has("description") && firstCharacter.get("description").isJsonPrimitive()) {
                characterDescription = firstCharacter.get("description").getAsString();
                System.out.println("Got character description: " + characterDescription);
            } else {
                System.err.println("Warning: 'description' field is missing or not a string!");
            }
        } catch (Exception e) {
            System.err.println("Failed to get character description, using default. Error: " + e.toString());
        }
        if(characterDescription.isEmpty()){
           characterDescription = "You are a helpful AI god.";
        }

        // Create conversation history with updated character description
        return new ConversationHistory(Utils.replacePlaceholders(initialPrompt, Map.of("characterDescription", characterDescription)));
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        this.player = player;
        final String message = event.getMessage().getString();
        MinecraftServer server = player.getServer();
        if (server != null) {
            System.out.println("Setting Server");
            this.server = server;
        }

        System.out.println("Received message: " + message);

        if (message.equals("a")) {
            try {
                List<JsonObject> characters = Player2APIService.getSelectedCharacters();
                for (JsonObject character : characters) {
                    System.out.println(character);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // Get dynamic conversation history
        ConversationHistory conversationHistory = getConversationHistory();
        conversationHistory.addUserMessage(message);

        try {
            JsonObject response = Player2APIService.completeConversation(conversationHistory);
            String responseAsString = response.toString();
            System.out.println("LLM Response: " + responseAsString);
            conversationHistory.addSystemMessage(responseAsString);

            String command = (response.has("command") && !response.get("command").isJsonNull())
                    ? response.get("command").getAsString()
                    : null;

            String chatMessage = (response.has("message") && !response.get("message").isJsonNull())
                    ? response.get("message").getAsString()
                    : null;

            if (command != null) {
                System.out.println("Command received: " + command);
                sendCommand(command);
            }

            if (chatMessage != null) {
                System.out.println("Chat response received: " + chatMessage);
                sendChat(chatMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendChat("Error communicating with AI: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void sendCommand(String command) {
        System.out.println("Sending command: " + command);
        if (this.server == null) {
            System.err.println("Server is empty");
            return;
        }

        CommandSourceStack commandSource = this.server.createCommandSourceStack();
        this.server.getCommands().performPrefixedCommand(commandSource, command);
    }

    private void sendChat(String message) {
        System.out.println("Sending chat message: " + message);
        if (this.player == null) {
            System.err.println("Player is empty");
            return;
        }
        this.player.sendSystemMessage(Component.literal(message));
    }
}
