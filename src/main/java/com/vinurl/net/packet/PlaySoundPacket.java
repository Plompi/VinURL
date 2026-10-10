package com.vinurl.net.packet;

import com.vinurl.VinURL;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlaySoundPacket(BlockPos pos, int entityID, String url) implements CustomPacketPayload {
	public static final ResourceLocation PLAY_SOUND_PACKET_ID = VinURL.identifier("play_sound_packet");
	public static final CustomPacketPayload.Type<PlaySoundPacket> TYPE = new CustomPacketPayload.Type<>(PLAY_SOUND_PACKET_ID);

	public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundPacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, PlaySoundPacket::pos,
		ByteBufCodecs.INT, PlaySoundPacket::entityID,
		ByteBufCodecs.STRING_UTF8, PlaySoundPacket::url,
		PlaySoundPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
