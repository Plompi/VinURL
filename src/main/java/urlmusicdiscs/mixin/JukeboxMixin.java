package urlmusicdiscs.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
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
import urlmusicdiscs.items.URLDiscItem;

@Mixin(JukeboxBlockEntity.class)
public class JukeboxMixin {
	@Inject(at = @At("TAIL"), method = "dropRecord", cancellable = true)
	public void dropRecord(CallbackInfo ci) {
		JukeboxBlockEntity jukebox = (JukeboxBlockEntity)(Object)this;

		PacketByteBuf bufInfo = PacketByteBufs.create();
		bufInfo.writeBlockPos(jukebox.getPos());
		bufInfo.writeString("");

		jukebox.getWorld().getPlayers().forEach(playerEntity1 -> {
			ServerPlayNetworking.send((ServerPlayerEntity) playerEntity1, URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, bufInfo);
		});
	}

	@Inject(at = @At("HEAD"), method = "startPlaying", cancellable = true)
	public void startPlaying(CallbackInfo ci) {
		JukeboxBlockEntity jukebox = (JukeboxBlockEntity)(Object)this;

		ItemStack stack = jukebox.getStack();
		Item item = stack.getItem();

		if (item instanceof URLDiscItem && !jukebox.getWorld().isClient()) {
			NbtCompound nbtInfo = stack.getNbt();

			if (nbtInfo == null) {
				nbtInfo = new NbtCompound();
			}

			String musicUrl = nbtInfo.getString("music_url");

			if (musicUrl != null && !musicUrl.equals("")) {
				PacketByteBuf bufInfo = PacketByteBufs.create();
				bufInfo.writeBlockPos(jukebox.getPos());
				bufInfo.writeString(musicUrl);

				jukebox.getWorld().getPlayers().forEach(
						playerEntity1 -> ServerPlayNetworking.send(
								(ServerPlayerEntity) playerEntity1,
								URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, bufInfo
						)
				);
			}
		}
	}
}