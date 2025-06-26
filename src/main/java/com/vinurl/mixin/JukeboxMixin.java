package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vinurl.util.Constants.CUSTOM_RECORD;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin implements SingleStackInventory {
	@Shadow
	private ItemStack recordStack;

	@Shadow
	public abstract BlockEntity asBlockEntity();

	@Inject(at = @At("HEAD"), method = "setStack")
	public void stopPlaying(ItemStack stack, CallbackInfo cir) {
		if (recordStack.getItem() == CUSTOM_RECORD) {
			VinURLSound.stop((ServerWorld) asBlockEntity().getWorld(), recordStack, asBlockEntity().getPos());
		}
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void startPlaying(ItemStack stack, CallbackInfo cir) {
		if (recordStack.getItem() == CUSTOM_RECORD) {
			VinURLSound.play((ServerWorld) asBlockEntity().getWorld(), recordStack, asBlockEntity().getPos());
		}
	}
}