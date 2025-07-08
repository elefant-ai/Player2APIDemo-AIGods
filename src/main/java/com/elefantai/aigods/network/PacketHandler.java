package com.elefantai.aigods.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// see here: https://www.youtube.com/watch?v=KrzsRqxaTug&t=2s
// although some stuff needed to be changed for this version

public class PacketHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(SSendTTSPacket.TYPE, SSendTTSPacket.STREAM_CODEC, ServerTTSHandler::handleData);

    }


    public static void sendToServer(SSendTTSPacket msg){
        ClientPacketDistributor.sendToServer(msg);
    }


    public static void sendToAllClients(SSendTTSPacket msg){
        PacketDistributor.sendToAllPlayers(msg);
    }
}
