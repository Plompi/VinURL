package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin implements SingleStackInventory {
	@Shadow
	private ItemStack recordStack;

	@Shadow
	public abstract BlockEntity asBlockEntity();

	@Inject(at = @At("HEAD"), method = "dropRecord")
	public void dropRecord(CallbackInfo cir) {
		VinURLSound.stop(asBlockEntity().getWorld(), recordStack, asBlockEntity().getPos());
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void setStack(ItemStack stack, CallbackInfo cir) {
		VinURLSound.play(asBlockEntity().getWorld(), recordStack, asBlockEntity().getPos());
	}


}