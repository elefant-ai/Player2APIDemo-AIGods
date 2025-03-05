package com.elefantai.player2api;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Player2ExampleMod.MODID)
public class Player2ExampleMod
{
    public static final String MODID = "player2api";

    public Player2ExampleMod()
    {
        // Register this class to receive Forge events
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event)
    {
        final String message = event.getMessage().getString();


        // Check if the message is exactly "aaa"
        if (message.equals("aaa"))
        {
            // Respond with "bbb"
            event.getPlayer().sendSystemMessage(Component.literal("bbb"));
        }
    }
}