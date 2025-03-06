package com.elefantai.player2api;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod {
    public static final String MODID = "player2api";

    private final List<JsonObject> conversationHistory = new ArrayList<>();

    public Player2ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);

        // Initial System Prompt
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are an AI assistant in a Minecraft server. You can either execute a command (starting with '/') or send a chat message.");
        conversationHistory.add(systemMessage);
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        final String message = event.getMessage().getString();
        MinecraftServer server = player.getServer();

        sendChat(player, "Recieved message " + message);

        // HISTORY
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversationHistory.add(userMessage);

        try {
            String responseText = ChatCompletion.getResponse(conversationHistory);
            System.out.println("AAA GOT RESPONSE TEXT " + responseText);
            if (responseText.startsWith("/")) {
                // If response starts with "/", it's a command
                if (server != null) {
                    sendCommand(server, responseText.substring(1)); // Remove "/"
                }
            } else {
                // Otherwise, send as a chat message
                sendChat(player, responseText);
            }
        } catch (Exception e) {
            e.printStackTrace();  // Log the full error
            sendChat(player, "Error communicating with AI: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void sendCommand(MinecraftServer server, String command) {
        CommandSourceStack commandSource = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(commandSource, command);
    }

    private void sendChat(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
