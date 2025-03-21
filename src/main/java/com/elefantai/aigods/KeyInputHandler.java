package com.elefantai.aigods;

import com.elefantai.aigods.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Player2ExampleMod.MODID, value = Dist.CLIENT)
public class KeyInputHandler {
    private static boolean wasSTTKeyPressed = false; // Track previous key state

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        boolean isKeyPressed = KeyBindings.STTKey.get().isDown(); // Check if key is currently pressed

        if (isKeyPressed && !wasSTTKeyPressed) {
            System.out.println("CLIENT: Sending start TTS to server");
            Player2APIService.startSTT();

        } else if (!isKeyPressed && wasSTTKeyPressed) {
            System.out.println("CLIENT: Sending stop STT to server");
            Player2APIService.stopSTT();
        }

        wasSTTKeyPressed = isKeyPressed;
    }
}