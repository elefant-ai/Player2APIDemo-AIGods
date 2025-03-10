package com.elefantai.player2api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod {
    public static final String MODID = "player2api";

    private final ConversationHistory conversationHistory;

    // these two will be set after the first chat message:
    private ServerPlayer player = null;
    private MinecraftServer server = null;

    public static String initialPrompt = "You are an AI assistant that always responds in JSON format. Your response must always be a JSON object containing at least one (or both) of the following fields:\n" +
            "\n" +
            "- \"chat\": A string containing a natural language response when the input is a question or conversational.\n" +
            "- \"command\": A string containing a command when the input is an instruction or action request.";


    public Player2ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
        conversationHistory = new ConversationHistory(initialPrompt);
}


    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        this.player = player;
        final String message = event.getMessage().getString();
        MinecraftServer server = player.getServer();
        if(server != null){
            System.out.println("Setting Server");
            this.server = server;
        }


        System.out.println("Received message: " + message);
        //sendChat("Received message " + message);


        // add user message
        conversationHistory.addUserMessage(message);

        try {
            JsonObject response = ChatCompletion.getResponse(conversationHistory);
            String responseAsString = response.toString();
            System.out.println("LLM Response: " + responseAsString);
            conversationHistory.addSystemMessage(responseAsString);

            String command = response.has("command") ? response.get("command").getAsString() : null;

            String chatMessage = response.has("chat") ? response.get("chat").getAsString() : null;

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
        if(this.server == null){
            System.err.println("Server is empty");
            return;
        }
        CommandSourceStack commandSource = this.server.createCommandSourceStack();
        this.server.getCommands().performPrefixedCommand(commandSource, command);

    }

    private void sendChat(String message) {
        System.out.println("Sending chat message: " + message);
        if(this.player == null){
            System.err.println("Player is empty");
            return;
        }
        this.player.sendSystemMessage(Component.literal(message));
    }
}
