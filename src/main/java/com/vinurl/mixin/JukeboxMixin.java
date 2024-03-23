package com.vinurl.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.vinurl.VinURL;
import com.vinurl.items.VinURLDiscItem;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntityMixin implements SingleStackInventory {
	@Inject(at = @At("TAIL"), method = "dropRecord")
	public void dropRecord(CallbackInfo ci) {
		PacketByteBuf bufInfo = PacketByteBufs.create();
		bufInfo.writeBlockPos(pos);
		bufInfo.writeString("");

		world.getPlayers().forEach(playerEntity -> {
			ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, VinURL.CUSTOM_RECORD_PACKET_ID, bufInfo);
		});
	}

	@Inject(at = @At("HEAD"), method = "startPlaying")
	public void startPlaying(CallbackInfo ci) {
		ItemStack recordStack = this.getStack();
		if (recordStack.getItem() instanceof VinURLDiscItem && !world.isClient()) {
			String musicUrl = recordStack.getOrCreateNbt().getString("music_url");

			if (musicUrl != null && !musicUrl.isEmpty()) {
				PacketByteBuf bufInfo = PacketByteBufs.create();
				bufInfo.writeBlockPos(pos);
				bufInfo.writeString(musicUrl);

				world.getPlayers().forEach(
						playerEntity -> ServerPlayNetworking.send(
								(ServerPlayerEntity) playerEntity,
								VinURL.CUSTOM_RECORD_PACKET_ID, bufInfo
						)
				);
			}
		}
	}
}