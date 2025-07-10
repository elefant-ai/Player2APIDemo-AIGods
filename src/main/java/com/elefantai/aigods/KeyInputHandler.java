package com.elefantai.aigods;

import com.elefantai.aigods.network.PacketHandler;
import com.elefantai.aigods.network.SSendTTSPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
@Mod(Player2ExampleMod.MODID)
public class KeyInputHandler {
    private static boolean wasSTTKeyPressed = false; // Track previous key state

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        boolean isKeyPressed = KeyBindings.STTKey.get().isDown(); // Check if key is currently pressed

        if (isKeyPressed && !wasSTTKeyPressed) {
            System.out.println("CLIENT: Sending start TTS to server");
            PacketHandler.sendToServer(new SSendTTSPacket(true));

        } else if (!isKeyPressed && wasSTTKeyPressed) {
            System.out.println("CLIENT: Sending stop STT to server");
            PacketHandler.sendToServer(new SSendTTSPacket(false));
        }

        wasSTTKeyPressed = isKeyPressed;
    }
}