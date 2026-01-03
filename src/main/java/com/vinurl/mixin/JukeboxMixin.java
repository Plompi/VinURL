package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vinurl.util.Constants.CUSTOM_RECORD;
import static com.vinurl.util.Constants.DURATION_KEY;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin {
	@Shadow
	private ItemStack item;

	@Shadow
	public abstract BlockEntity getContainerBlockEntity();

	@Inject(at = @At("HEAD"), method = "setTheItem")
	public void stopPlaying(ItemStack stack, CallbackInfo ci) {
		if (item.is(CUSTOM_RECORD)) {
			VinURLSound.stopAt(getContainerBlockEntity().getLevel(), item, getContainerBlockEntity().getBlockPos(), false);
		}
	}

	@Inject(at = @At("TAIL"), method = "setTheItem")
	public void startPlaying(ItemStack stack, CallbackInfo ci) {
		if (item.is(CUSTOM_RECORD)) {
			VinURLSound.playAt(getContainerBlockEntity().getLevel(), item, getContainerBlockEntity().getBlockPos());
		}
	}

	@Inject(at = @At("HEAD"), method = "popOutTheItem")
	public void cancelDownload(CallbackInfo ci) {
		if (item.is(CUSTOM_RECORD)) {
			VinURLSound.stopAt(getContainerBlockEntity().getLevel(), item, getContainerBlockEntity().getBlockPos(), true);
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private static void tick(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity.getTheItem().is(CUSTOM_RECORD)) {
			CompoundTag tag = blockEntity.getTheItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			JukeboxSongPlayer manager = blockEntity.getSongPlayer();
			if (manager.getTicksSinceSongStarted() > tag.get(DURATION_KEY) * 20L) {
				manager.stop(level, state);
				VinURLSound.stopAt(level, blockEntity.getTheItem(), pos, false);
			}
		}
	}
}