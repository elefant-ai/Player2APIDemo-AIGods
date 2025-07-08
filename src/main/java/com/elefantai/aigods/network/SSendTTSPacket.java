package com.elefantai.aigods.network;

import com.elefantai.aigods.Player2ExampleMod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SSendTTSPacket(boolean isPressed) implements CustomPacketPayload {
    // register any data to send here as fields (as long as it is byte-convertible)

    public static final Type<SSendTTSPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Player2ExampleMod.MODID, "send_tts_packet"));


    public static final StreamCodec<FriendlyByteBuf, SSendTTSPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SSendTTSPacket::isPressed,
            SSendTTSPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
