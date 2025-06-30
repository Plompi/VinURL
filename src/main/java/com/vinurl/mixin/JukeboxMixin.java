package com.vinurl.mixin;

import com.vinurl.api.VinURLSound;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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

	@Inject(at = @At("HEAD"), method = "tick")
	private static void tick(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity.getStack().getItem() == CUSTOM_RECORD) {
			NbtComponent nbt = blockEntity.getStack().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
			JukeboxManager manager = blockEntity.getManager();
			if (manager.getTicksSinceSongStarted() > nbt.copyNbt().get(DURATION_KEY) * 20L){
				manager.stopPlaying(world, state);
				VinURLSound.stop((ServerWorld) world, blockEntity.getStack(), pos);
			}
		}
	}
}