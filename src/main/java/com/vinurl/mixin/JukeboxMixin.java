package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.vinurl.util.Constants.CUSTOM_RECORD;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntity implements SingleStackInventory, Clearable {

	public JukeboxMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(at = @At("HEAD"), method = "removeStack")
	public void stopPlaying(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
		if (getStack().getItem() == CUSTOM_RECORD) {
			VinURLSound.stop((ServerWorld) world, getStack(), getPos());
		}
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void startPlaying(int slot, ItemStack stack, CallbackInfo ci) {
		if (getStack().getItem() == CUSTOM_RECORD) {
			VinURLSound.play((ServerWorld) world, getStack(), getPos());
		}
	}
}