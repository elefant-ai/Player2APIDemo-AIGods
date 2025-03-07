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

    public Player2ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
        String initialPrompt =
                "You are an AI assistant in a Minecraft server. You can either execute a command (starting with '/') or send a chat message.";
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
            String responseText = ChatCompletion.getResponse(conversationHistory);
            System.out.println("LLM Response: " + responseText);
            conversationHistory.addSystemMessage(responseText);

            // this will process the response text, if it starts with / then it is a command
            if (responseText.startsWith("/") && server != null) {
                sendCommand(responseText.trim());
            } else {
                sendChat(responseText.trim());
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
