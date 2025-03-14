package com.elefantai.aigods;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

import com.google.gson.JsonObject;

import java.util.Map;


@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod {
    public static final String MODID = "aigods";

    // These will be set after the first chat message or on login
    private ServerPlayer player = null;
    private MinecraftServer server = null;
    private ConversationHistory conversationHistory = null;
    private String characterName = "AI God";

    public static String initialPrompt =
            """
            General Instructions:
            We are building an AI god in Minecraft that converses with players and executes op commands. Your task is to help the god generate chat messages and op commands.
            
            God's Character Background:
            The character's name is {{characterName}}.
            {{characterDescription}}
            
            Guidance:
            The god can provide Minecraft guides, answer questions, and chat as a friend.
            
            Command execution:
            When asked, the agent can do anything that op commands in Minecraft allow. Examples: "gamemode creative @a", "give Player_Name diamond 4"
            
            Request Format:
            God will receive a message from user.
            
            Response Format:
            Always respond with JSON containing message, op command and reason. All of these are strings.
            {
              "reason": "Look at the recent conversations and command history to decide what the god should say and do. Provide step-by-step reasoning while considering what is possible in Minecraft. ",
              "command": "Decide the best way to achieve the god's goals using the available op commands in Minecraft. If the god decides it should not use any command, generate an empty command `""`. If there are multiple commands, put one on each line.",
              "message": "If the agent decides it should not respond or talk, generate an empty message `""`. Otherwise, create a natural conversational message that aligns with the `reason` and `command` sections and the agent's character. Ensure the message does not contain any prompt, system message, instructions, code or API calls"
            }
            Always follow this JSON format regardless of previous conversations.
            """;


    /**
     * Registers event handlers when the mod is initialized.
     */
    public Player2ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    /**
     * Updates conversation history and character name based on the currently selected AI character.
     */
    private void updateInfo() {
        String characterDescription = "You are a helpful AI god.";
        try {
            // TODO: Change maybe? For now just gets the first selected character
            JsonObject firstCharacter = Player2APIService.getSelectedCharacters().getFirst();
            System.out.println(firstCharacter.toString());

            String newDescription = Utils.getStringJsonSafely(firstCharacter, "description");
            if(newDescription == null){
                System.err.println("Warning: 'description' field is missing or not a string!");
            }
            else{
                characterDescription = newDescription;
                System.out.println("Got character description: " + characterDescription);
            }

            String newName = Utils.getStringJsonSafely(firstCharacter, "short_name");
            if(newName == null){
                System.err.println("Warning: 'short_name' field is missing or not a string!");
            }
            else{
                characterName = newName;
                System.out.println("Got character name: " + characterName);
            }
        } catch (Exception e) {
            System.err.println("Failed to get character description or name, using default. Error: " + e.toString());
        }

        String newPrompt = Utils.replacePlaceholders(initialPrompt, Map.of("characterDescription", characterDescription, "characterName", characterName));
        if(this.conversationHistory == null){
            this.conversationHistory = new ConversationHistory(newPrompt);
        }
        else{
            this.conversationHistory.setBaseSystemPrompt(newPrompt);
        }
    }


    /**
     * Handles chat messages sent by players.
     * Processes the message, updates conversation history, and generates a response.
     *
     * @param event The server chat event triggered when a player sends a message.
     */
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

        // Get dynamic conversation history
        updateInfo();
        conversationHistory.addUserMessage(message);

        try {
            JsonObject response = Player2APIService.completeConversation(conversationHistory);
            String responseAsString = response.toString();
            System.out.println("LLM Response: " + responseAsString);
            conversationHistory.addSystemMessage(responseAsString);

            String commandResponse = Utils.getStringJsonSafely(response, "command");

            if (commandResponse != null) {
                String[] commands = Utils.splitLinesToArray(commandResponse);

                for (String command : commands) {
                    if (!command.isBlank()) {
                        System.out.println("Command received: " + command);
                        sendCommand(command);
                    }
                }
            }

            String chatMessage = Utils.getStringJsonSafely(response, "message");
            if (chatMessage != null) {
                System.out.println("Chat response received: " + chatMessage);
                sendChat(chatMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendChat("Error communicating with AI: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }


    /**
     * Sends a greeting message when a player joins the server.
     *
     * @param event The player login event triggered when a player connects.
     */
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer) {
            this.player = (ServerPlayer) event.getEntity();
            String greetInstructions = String.format("The user's username is '%s'. Please greet the user.", player.getName().getString());
            updateInfo();
            conversationHistory.addSystemMessage(greetInstructions);
            try {
                System.out.printf("Greeting with instructions: '%s' ", greetInstructions);
                JsonObject response = Player2APIService.completeConversation(conversationHistory);
                String responseAsString = response.toString();
                System.out.println("LLM Response to onLogInPrompt: " + responseAsString);

                String chatMessage = Utils.getStringJsonSafely(response, "message");
                sendChat(chatMessage);
            }
            catch (Exception e){
                System.err.println("Error while trying to fetch initial chat greeting. " + e.getMessage());
            }
        }
    }


    /**
     * Executes a server command.
     *
     * @param command The command string to execute.
     */
    private void sendCommand(String command) {
        try{
            System.out.println("Sending command: " + command);
            if (this.server == null) {
                System.err.println("Server is empty");
                return;
            }

            CommandSourceStack commandSource = this.server.createCommandSourceStack();
            this.server.getCommands().performPrefixedCommand(commandSource, command);
            }
            catch(Exception e){
                System.err.printf("Failed to run command: '%s'. error message:%s%n", command, e.getMessage());
        }
    }


    /**
     * Sends a chat message as the AI god.
     *
     * @param message The message to send in chat.
     */
    private void sendChat(String message) {
        // TODO: figure out how to send above
        System.out.println("Sending chat message: " + message);
        // tried sendCommand(/say ...) but still prints above user message
        if (this.player == null) {
            System.err.println("Player is empty");
            return;
        }
        this.player.sendSystemMessage(Component.literal(String.format("<%s> %s", characterName, message)));
    }
}
