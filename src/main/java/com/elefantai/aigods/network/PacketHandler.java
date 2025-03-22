package com.elefantai.aigods.network;

import com.elefantai.aigods.Player2ExampleMod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

// see here: https://www.youtube.com/watch?v=KrzsRqxaTug&t=2s
// although some stuff needed to be changed for this version

public class PacketHandler {
    private static final SimpleChannel INSTANCE = ChannelBuilder.named(
             ResourceLocation.fromNamespaceAndPath(Player2ExampleMod.MODID, "main"))
                .serverAcceptedVersions((status, version) -> true)
                .clientAcceptedVersions((status, version) -> true)
                .networkProtocolVersion(1)
                .simpleChannel();





    public static void register(){
        // convention:
        // Server -> Client : start name with C
        // Client -> Server : start name with S
        INSTANCE.messageBuilder(SSendTTSPacket.class, NetworkDirection.PLAY_TO_SERVER)
            .encoder(SSendTTSPacket::encode)
            .decoder(SSendTTSPacket::new)
            .consumerMainThread(SSendTTSPacket::handle)
            .add();
    }


    public static void sendToServer(Object msg){
        // instead of .SERVER...
        // do .PLAYER.with(player) (player : ServerPlayer) if you need to forward to player instead
        INSTANCE.send(msg, PacketDistributor.SERVER.noArg());
    }


    public static void sendToAllClients(Object msg){
        INSTANCE.send(msg, PacketDistributor.ALL.noArg());
    }


}
