package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vinurl.util.Constants.CUSTOM_RECORD;
import static com.vinurl.util.Constants.DURATION_KEY;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntity implements SingleStackInventory, Clearable {

	@Shadow private long tickCount;

	@Shadow private long recordStartTick;

	@Shadow
	private void stopPlaying() {}

	public JukeboxMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(at = @At("HEAD"), method = "dropRecord")
	public void stopPlaying(CallbackInfo ci) {
		if (getStack().getItem() == CUSTOM_RECORD) {
			VinURLSound.stop(world, getStack(), getPos(), true);
		}
	}

	@Inject(at = @At("TAIL"), method = "setStack")
	public void startPlaying(int slot, ItemStack stack, CallbackInfo ci) {
		if (getStack().getItem() == CUSTOM_RECORD) {
			VinURLSound.play(world, getStack(), getPos());
		}
	}

	@Inject(at = @At("HEAD"), method = "tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
	private void tick(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (getStack().getItem() == CUSTOM_RECORD) {
			NbtCompound nbt = getStack().getOrCreateNbt();
			if (tickCount > recordStartTick + nbt.getInt(DURATION_KEY) * 20L) {
				stopPlaying();
				VinURLSound.stop(world, getStack(), pos, false);
			}
		}
	}
}