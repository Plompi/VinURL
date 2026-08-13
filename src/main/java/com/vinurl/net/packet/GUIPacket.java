package com.vinurl.net.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.vinurl.util.Constants.GUI_PACKET_ID;

public record GUIPacket(String url, int duration) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<GUIPacket> TYPE = new CustomPacketPayload.Type<>(GUI_PACKET_ID);

	public static final StreamCodec<RegistryFriendlyByteBuf, GUIPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, GUIPacket::url,
		ByteBufCodecs.INT, GUIPacket::duration,
		GUIPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
