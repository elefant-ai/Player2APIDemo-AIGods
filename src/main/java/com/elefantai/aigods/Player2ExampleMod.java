package com.elefantai.aigods;

import com.elefantai.aigods.player2api.Player2APIService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

import com.google.gson.JsonObject;

import java.util.List;

@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod {
    public static final String MODID = "aigods";

    // These will be set after the first chat message or on login
    public ServerPlayer player = null;
    public static MinecraftServer server = null;
    private ConversationHistory conversationHistory = null;
    private Character character = null;
    private Boolean shouldSpeak = true;
    public static Player2ExampleMod instance; // hack for single instance
    public static long lastHeartbeatTime;

    public static String initialPrompt = """
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

        Player2APIService.StreamEventHandler eventHandler = new Player2APIService.StreamEventHandler("minecraft",json -> {

            if (json.commands!= null) {
                json.commands.forEach(command -> {

                    if (command.name.equals("minecraft_command")) {
                        JsonObject arguments = command.arguments(JsonObject.class);
                        String cmd = arguments.get("command").getAsString();
                        if (cmd != null && !cmd.isEmpty()) {
                            System.out.println("Executing command: " + cmd);
                            executeCommandString(cmd);
                        } else {
                            System.out.println("Received empty command, skipping execution.");
                        }
                    }
                });
            }
            if (json.message != null) {
                instance.sendCharacterMessage(json.message);
            }
        });

        MinecraftForge.EVENT_BUS.register(eventHandler);

        // Start listening

        MinecraftForge.EVENT_BUS.register(this);
        instance = this;
        lastHeartbeatTime = System.nanoTime();
    }

    public void processModCommand(String command) {
        switch (command) {
            case "tts":
                shouldSpeak = !shouldSpeak;
                if (!shouldSpeak) {
                    sendInfoMessage("Turning off text to speech");
                } else {
                    sendInfoMessage("Turning on text to speech");
                }
                break;
            case "help":
                sendInfoMessage("Commands:");
                sendInfoMessage("'!tts' : toggles text-to-speech");
                break;
            default:
                sendInfoMessage("Unknown command. Type !help for all commands");
        }
    }

    /**
     * Handles chat messages sent by players.
     * Processes the message, updates conversation history, and generates a
     * response.
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
            Player2ExampleMod.server = server;
        }
        if (!message.isEmpty() && message.charAt(0) == '!') {
            processModCommand(message.substring(1)); // remove '!' and process the command
            return;
        }
        event.setCanceled(true); // prevent the chat message from being sent
        ClientServiceThreaded.processPlayerMessage(instance, message);
    }

    /**
     * Sends a greeting message when a player joins the server, also updates info
     * and sets player.
     *
     * @param event The player login event triggered when a player connects.
     */
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            this.player = (ServerPlayer) event.getEntity();
            MinecraftServer server = player.getServer();
            if (server != null) {
                System.out.println("Setting Server");
                Player2ExampleMod.server = server;
            }
            ClientServiceThreaded.sendGreeting(instance);

        }
    }

    /**
     * Executes a server command.
     *
     * @param command The list of commands to execute.
     */
    public void executeCommandString(String command) {
        if (command == null || Player2ExampleMod.server == null)
            return;
        try {
            String[] commands = Utils.splitLinesToArray(command);
            for (String cmd : commands) {
                if (!cmd.isBlank()) {
                    System.out.println("Sending command: " + cmd);
                    CommandSourceStack commandSource = Player2ExampleMod.server.createCommandSourceStack();
                    Player2ExampleMod.server.getCommands().performPrefixedCommand(commandSource, command);
                }
            }
        } catch (Exception e) {
            System.err.printf("Failed to run command(s): '%s'. error message:%s%n", command, e.getMessage());
        }
    }

    /**
     * Sends a chat message as the character
     * 
     * @param message The message to send in chat.
     */
    public void sendCharacterMessage(String message) {
        if (message == null)
            return;
        System.out.println("Sending Character message: " + message);

        if (this.player == null) {
            System.err.println("Player is empty");
            return;
        }

        this.player.sendSystemMessage(Component.literal(String.format("<%s> %s", this.character.name, message.trim())));
    }

    /**
     * Sends a chat message as INFO.
     *
     * @param message The message to send in chat.
     */
    private void sendInfoMessage(String message) {
        System.out.println("Sending system message");

        if (this.player == null) {
            System.err.println("Player is empty");
            return;
        }
        this.player.sendSystemMessage(Component.literal(String.format("INFO: %s", message)));
    }

    public String addPlayerStatusToUsrMessage(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("message", message);

        if (this.player != null) {
            String dimension = this.player.level().dimension().location().toString(); // Get dimension as string
            // int because otherwise tp doesnt work properly

            json.addProperty("playerStatus",
                    String.format("Player name is '%s' and is at (%d, %d, %d) in %s", player.getName().getString(),
                            (int) player.getX(), (int) player.getY(), (int) player.getZ(), dimension));
        } else {
            json.addProperty("playerStatus", ""); // Blank if player is null
        }
        return json.toString();
    }

    public void setCharacter(Character c) {
        this.character = c;
    }

    public Character getCharacter() {
        return this.character;
    }

    public void setConversationHistory(ConversationHistory conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    public ConversationHistory getConversationHistory() {
        return this.conversationHistory;
    }

    public static String getInitialPrompt() {
        return initialPrompt;
    }

    public void sendUserMessage(String message) {
        System.out.println("Sending player message + adding to conversation history");
        this.player.sendSystemMessage(
                Component.literal(String.format("<%s> %s", this.player.getName().getString(), message)));

    }

    public void addProcessedUserMessage(String processed) {
        this.conversationHistory.addUserMessage(processed);
    }

    public boolean getShouldSpeak() {
        return this.shouldSpeak;
    }

    public void addAssistantResponse(String response) {
        this.conversationHistory.addAssistantMessage(response);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        long now = System.nanoTime();
        // every 60 seconds send heartbeat
        if (now - lastHeartbeatTime > 60_000_000_000L) {
            ClientServiceThreaded.sendHeartbeat();
            lastHeartbeatTime = now;
        }
    }

}
