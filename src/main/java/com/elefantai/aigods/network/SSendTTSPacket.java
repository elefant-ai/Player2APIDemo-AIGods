package com.elefantai.aigods.network;

import com.elefantai.aigods.Player2APIService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SSendTTSPacket {
    // register any data to send here as fields (as long as it is byte-convertible)
    private boolean isPressed;

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
            System.out.println("STARTING STT");
            Player2APIService.startSTT();
        }
        else{
            System.out.println("STOPPING STT");
            System.out.printf("Result: '%s'%n", Player2APIService.stopSTT());
        }
    }


}
