package com.vinurl.net.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.vinurl.util.Constants.SET_URL_PACKET_ID;

public record SetURLPacket(String url, int duration, boolean lock) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SetURLPacket> TYPE = new CustomPacketPayload.Type<>(SET_URL_PACKET_ID);

	public static final StreamCodec<RegistryFriendlyByteBuf, SetURLPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, SetURLPacket::url,
		ByteBufCodecs.INT, SetURLPacket::duration,
		ByteBufCodecs.BOOL, SetURLPacket::lock,
		SetURLPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
