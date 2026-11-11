package com.vinurl.net.packet;

import com.vinurl.VinURL;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GUIPacket(String url, int duration) implements CustomPacketPayload {
	public static final ResourceLocation GUI_PACKET_ID = VinURL.identifier("gui_packet");
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
