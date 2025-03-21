package com.elefantai.aigods.network;

import com.elefantai.aigods.Player2APIService;
import com.elefantai.aigods.Player2ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SSendTTSPacket {
    // register any data to send here as fields (as long as it is byte-convertible)
    private boolean isPressed;
    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SSendTTSPacket(boolean isPressed){
        this.isPressed = isPressed;
    }

    public SSendTTSPacket(FriendlyByteBuf buffer){
        this(buffer.readBoolean());
    }
    public void encode(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.isPressed);
    }
    public void handle (CustomPayloadEvent.Context context){
        if(!context.isServerSide()){
            System.err.println("HANDLE NOT SERVER SIDE");
            context.setPacketHandled(false);
            return;
        }
        ServerPlayer player = context.getSender();
        assert player != null;
        player.sendSystemMessage(Component.literal("KEY CHANGE"));
        System.out.println("SERVER: " + player.getName() + "SENT KEY PRESS " + isPressed);

        if(isPressed){
            executorService.submit(() -> {
                System.out.println("STARTING STT");
                Player2APIService.startSTT();
            });
        }
        else{
            executorService.submit(() ->{
                System.out.println("STOPPING STT");
                final String sttResult = Player2APIService.stopSTT();
                System.out.printf("STT Result: '%s'%n", sttResult);
                if(sttResult.isEmpty()){
                    Player2ExampleMod.instance.processPlayerMessage("Could not hear user message");
                }
                else{
                    Player2ExampleMod.instance.processPlayerMessage(sttResult);
                }
            });
        }
    }


}
