package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import com.vinurl.component.AudioComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vinurl.VinURL.AUDIO_COMPONENT;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxMixin extends BlockEntity {
	@Shadow
	private ItemStack item;

	public JukeboxMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Inject(at = @At("HEAD"), method = "setTheItem")
	public void stopPlaying(ItemStack stack, CallbackInfo ci) {
		if (this.getLevel() == null || this.getLevel().isClientSide()) {return;}
		VinURLSound.stopAt((ServerLevel) this.getLevel(), item, this.getBlockPos(), false);
	}

	@Inject(at = @At("TAIL"), method = "setTheItem")
	public void startPlaying(ItemStack stack, CallbackInfo ci) {
		if (this.getLevel() == null || this.getLevel().isClientSide()) {return;}
		VinURLSound.playAt((ServerLevel) this.getLevel(), item, this.getBlockPos());
	}

	@Inject(at = @At("HEAD"), method = "popOutTheItem")
	public void cancelDownload(CallbackInfo ci) {
		if (this.getLevel() == null || this.getLevel().isClientSide()) {return;}
		VinURLSound.stopAt((ServerLevel) this.getLevel(), item, this.getBlockPos(), true);
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private static void tick(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
		if (level == null || level.isClientSide()) {return;}
		AudioComponent component = blockEntity.getTheItem().get(AUDIO_COMPONENT);
		JukeboxSongPlayer manager = blockEntity.getSongPlayer();
		if (component != null && manager.getTicksSinceSongStarted() > component.duration() * 20L) {
			manager.stop(level, state);
			VinURLSound.stopAt((ServerLevel) level, blockEntity.getTheItem(), pos, false);
		}
	}
}