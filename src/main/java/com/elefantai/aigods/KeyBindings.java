package com.elefantai.aigods;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Handles key binding registration and tick events for key press/release.
 */
@Mod.EventBusSubscriber(modid = "aigods", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    // Create a key binding. Change the key description and category as needed.
    public static final KeyMapping STTKey = new KeyMapping(
            "AI God's STT Key", // keybinding's name in game
            GLFW.GLFW_KEY_X, // default key (X)
                             // NOTE: V will not work (for now) as our launcher binds V already,
                             // so you will get an internal server error
            "key.categories.misc" // category in the controls menu
    );

    /**
     * Registers key mappings.
     * @param event The event when key mappings should be registered.
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(STTKey);
    }
}