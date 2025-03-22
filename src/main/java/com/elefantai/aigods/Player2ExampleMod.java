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
    private Character character = null;
    private Boolean shouldSpeak = true;
    public static Player2ExampleMod instance; // hack for single instance

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
            
            For teleport commands, instead of using relative tp commands, use the player's position provided. 
            
            Request Format:
            God will receive a message from user, as a stringified JSON of the form:
            {
                "message" : string // the message that the user sends
                "playerStatus" : string // metadata relating to the player's position and current dimension
            }
            
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
        instance = this;
    }


    /**
     * Updates this. (conversationHistory, character) based on the currently selected character.
     */
    private void updateInfo() {
        Character newCharacter = Player2APIService.getSelectedCharacter();
        System.out.println(newCharacter);
        this.character = newCharacter;

        String newPrompt = Utils.replacePlaceholders(initialPrompt, Map.of("characterDescription", character.description, "characterName", character.name));

        if (this.conversationHistory == null) {
            this.conversationHistory = new ConversationHistory(newPrompt);
        } else {
            this.conversationHistory.setBaseSystemPrompt(newPrompt);
        }
    }

    public void processModCommand(String command){
        switch (command){
            case "tts":
                shouldSpeak = !shouldSpeak;
                if(!shouldSpeak){
                    sendSystemMessage("Turning off text to speech");
                }
                else{
                    sendSystemMessage("Turning on text to speech");
                }
                break;
            case "help":
                sendSystemMessage("Commands:");
                sendSystemMessage("'!tts' : toggles text-to-speech");
                sendSystemMessage("'!start' : Starts listening for microphone speech-to-text");
                sendSystemMessage("'!stop' : Stops listening for microphone speech-to-text");
                break;
            case "start":
                System.out.println("Start STT");
                Player2APIService.startSTT();
                break;
            case "stop":
                System.out.println("STOP STT");
                String result = Player2APIService.stopSTT();
                System.out.printf("Result: '%s'%n", Player2APIService.stopSTT());
                processPlayerMessage(result);
                break;
            default:
                sendSystemMessage("Unknown command. Type !help for all commands");
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
        processPlayerMessage(message);
        event.setCanceled(true); // prevent the chat message from being sent
    }
    public void processPlayerMessage(String message){
        System.out.println("Player location: X=" + player.getX() + ", Y=" + player.getY() + ", Z=" + player.getZ());

        if (!message.isEmpty() && message.charAt(0) == '!') {
            processModCommand(message.substring(1)); // remove '!' and process the command
            return;
        }

        // shows player's message
        System.out.println("Sending player message");
        this.player.sendSystemMessage(Component.literal(String.format("<%s> %s",  this.player.getName().getString(), message)));

        // Get dynamic conversation history
        updateInfo();
        String processedMessage = processUserMessage(message);
        System.out.println("Processed message: " + processedMessage);
        conversationHistory.addUserMessage(processedMessage);

        try {
            JsonObject response = Player2APIService.completeConversation(conversationHistory);
            String responseAsString = response.toString();
            System.out.println("LLM Response: " + responseAsString);

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
                processAIChatMessage(chatMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            processAIChatMessage("Error communicating with AI: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }


    /**
     * Sends a greeting message when a player joins the server, also updates info and sets player.
     *
     * @param event The player login event triggered when a player connects.
     */
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer) {
            this.player = (ServerPlayer) event.getEntity();
            updateInfo();
            processAIChatMessage(this.character.greetingInfo);
            MinecraftServer server = player.getServer();

            if (server != null) {
                System.out.println("Setting Server");
                this.server = server;
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
     * Sends a chat message as the character.
     *
     * @param message The message to send in chat.
     */
    private void processAIChatMessage(String message) {
        // TODO: figure out how to send above text instead of below.

        System.out.println("Processing AI Chat Response: " + message);
        // tried sendCommand(/say ...) but still prints above user message
        if (this.player == null) {
            System.err.println("Player is empty");
            return;
        }
        this.player.sendSystemMessage(Component.literal(String.format("<%s> %s", this.character.name, message)));
        if(shouldSpeak){
            Player2APIService.textToSpeech(message, character);
        }

    }
    /**
     * Sends a chat message as INFO.
     *
     * @param message The message to send in chat.
     */
    private void sendSystemMessage(String message){
        System.out.println("Sending system message");

        if(this.player == null){
            System.err.println("Player is empty");
            return;
        }
        this.player.sendSystemMessage(Component.literal(String.format("INFO: %s", message)));
    }

    private String processUserMessage(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("message", message);

        if (this.player != null) {
            String dimension = this.player.level().dimension().location().toString(); // Get dimension as string
            // int because otherwise tp doesnt work properly

            json.addProperty("playerStatus", String.format("Player name is '%s' and is at (%d, %d, %d) in %s", player.getName().getString(),
                    (int) player.getX(), (int) player.getY(), (int) player.getZ(), dimension));
        } else {
            json.addProperty("playerStatus", ""); // Blank if player is null
        }
        return json.toString();
    }



}
