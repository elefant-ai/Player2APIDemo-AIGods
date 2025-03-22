package com.elefantai.aigods;
import com.elefantai.aigods.network.PacketHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


@Mod.EventBusSubscriber(modid= Player2ExampleMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup (FMLCommonSetupEvent event){
        event.enqueueWork(() ->{
           // do any logic after register event
            PacketHandler.register();
        });
    }
}
