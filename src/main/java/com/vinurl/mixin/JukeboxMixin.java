package com.vinurl.mixin;

import com.vinurl.VinURL;
import com.vinurl.items.VinURLDiscItem;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vinurl.Helper.getNbt;
import static com.vinurl.VinURL.NETWORK_CHANNEL;
import static com.vinurl.VinURL.URL_KEY;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntityMixin implements SingleStackInventory {
	@Shadow
	private ItemStack recordStack;

	@Inject(at = @At("TAIL"), method = "dropRecord")
	public void dropRecord(CallbackInfo cir) {
		world.getPlayers().forEach(playerEntity -> {
			NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos,""));
		});
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void setStack(ItemStack stack, CallbackInfo cir) {
		if (recordStack.getItem() instanceof VinURLDiscItem && !world.isClient()) {
			String musicUrl = getNbt(recordStack).get(URL_KEY);

			if (musicUrl != null && !musicUrl.isEmpty()) {
				world.getPlayers().forEach(playerEntity -> {
					NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos, musicUrl));
				});
			} else {
				world.getPlayers().forEach(playerEntity -> {
					NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos,""));
				});
			}
		}
	}


}