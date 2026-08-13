package com.vinurl.net.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.vinurl.util.Constants.STOP_SOUND_PACKET_ID;

public record StopSoundPacket(BlockPos pos, int entityID, String url, boolean cancel) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<StopSoundPacket> TYPE = new CustomPacketPayload.Type<>(STOP_SOUND_PACKET_ID);

	public static final StreamCodec<RegistryFriendlyByteBuf, StopSoundPacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, StopSoundPacket::pos,
		ByteBufCodecs.INT, StopSoundPacket::entityID,
		ByteBufCodecs.STRING_UTF8, StopSoundPacket::url,
		ByteBufCodecs.BOOL, StopSoundPacket::cancel,
		StopSoundPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
