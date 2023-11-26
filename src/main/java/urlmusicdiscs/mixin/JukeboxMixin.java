package urlmusicdiscs.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import urlmusicdiscs.URLMusicDiscs;

@Mixin(JukeboxBlockEntity.class)
public class JukeboxMixin {
	@Inject(at = @At("TAIL"), method = "clear", cancellable = true)
	public void dropRecord(CallbackInfo ci) {
		JukeboxBlockEntity jukebox = (JukeboxBlockEntity)(Object)this;

		PacketByteBuf bufInfo = PacketByteBufs.create();
		bufInfo.writeBlockPos(jukebox.getPos());
		bufInfo.writeString("");

		jukebox.getWorld().getPlayers().forEach(playerEntity1 -> {
			ServerPlayNetworking.send((ServerPlayerEntity) playerEntity1, URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, bufInfo);
		});
	}
}