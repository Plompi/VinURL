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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntityMixin implements SingleStackInventory {
	@Shadow
	private ItemStack recordStack;

	@Inject(at = @At("TAIL"), method = "dropRecord")
	public void dropRecord(CallbackInfo cir) {
		world.getPlayers().forEach(playerEntity -> {
			ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new VinURL.PlaySoundPayload(pos, ""));
		});
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void setStack(ItemStack stack, CallbackInfo cir) {
		if (recordStack.getItem() instanceof VinURLDiscItem && !world.isClient() && recordStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
			String musicUrl = recordStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("music_url");

			if (musicUrl != null && !musicUrl.isEmpty()) {
				world.getPlayers().forEach(
						playerEntity -> ServerPlayNetworking.send(
								(ServerPlayerEntity) playerEntity,
								new VinURL.PlaySoundPayload(pos, musicUrl)
						)
				);
			} else {
				world.getPlayers().forEach(playerEntity -> {
					ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new VinURL.PlaySoundPayload(pos, ""));
				});
			}
		}
	}


}