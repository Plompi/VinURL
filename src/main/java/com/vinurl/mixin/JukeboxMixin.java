package com.vinurl.mixin;

import com.vinurl.VinURL;
import com.vinurl.items.VinURLDiscItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntityMixin implements SingleStackInventory {
	@Inject(at = @At("TAIL"), method = "dropRecord")
	public void dropRecord(CallbackInfo ci) {
		world.getPlayers().forEach(playerEntity -> {
			ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new VinURL.PlaySoundPayload(pos, ""));
		});
	}

	@Inject(at = @At("HEAD"), method = "startPlaying")
	public void startPlaying(CallbackInfo ci) {
		ItemStack recordStack = this.getStack();
		if (recordStack.getItem() instanceof VinURLDiscItem && !world.isClient()) {
			String musicUrl = Objects.requireNonNull(recordStack.get(DataComponentTypes.CUSTOM_DATA)).copyNbt().getString("music_url");

			if (musicUrl != null && !musicUrl.isEmpty()) {
				world.getPlayers().forEach(
						playerEntity -> ServerPlayNetworking.send(
								(ServerPlayerEntity) playerEntity,
								new VinURL.PlaySoundPayload(pos, musicUrl)
						)
				);
			}
		}
	}
}