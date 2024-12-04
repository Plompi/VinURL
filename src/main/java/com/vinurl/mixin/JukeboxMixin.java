package com.vinurl.mixin;

import com.vinurl.Helper;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
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
		Helper.stopVinURLDisc(world, pos);
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void setStack(ItemStack stack, CallbackInfo cir) {
		Helper.playVinURLDisc(world, recordStack, pos);
	}


}