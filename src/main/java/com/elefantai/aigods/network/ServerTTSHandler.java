package com.elefantai.aigods.network;


import com.elefantai.aigods.ClientServiceThreaded;
import com.elefantai.aigods.Player2ExampleMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.function.Supplier;

public class ServerTTSHandler {

    public static void handleData(final SSendTTSPacket data, final IPayloadContext context) {
        // Handle on network thread
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            assert player != null;
            //player.sendSystemMessage(Component.literal("KEY CHANGE"));
            System.out.println("SERVER: " + player.getName() + "SENT KEY PRESS " + data.isPressed());

            if(data.isPressed()){
                ClientServiceThreaded.startSTT();
            }
            else{
                ClientServiceThreaded.stopSTTAndProcess(Player2ExampleMod.instance);
            }
            // Handle on main thread
            // Your server-side logic here
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("aigods.networking.failed", e.getMessage()));
            return null;
        });
    }
}