package urlmusicdiscs.items;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import urlmusicdiscs.URLMusicDiscs;

public class URLDiscItem extends MusicDiscItem {
    public URLDiscItem(int comparatorOutput, SoundEvent sound, Settings settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings, lengthInSeconds);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos;
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos = context.getBlockPos());

        if (!blockState.isOf(Blocks.JUKEBOX)) {
            if (!world.isClient) {
                PacketByteBuf bufInfo = PacketByteBufs.create();
                bufInfo.writeItemStack(context.getStack());

                ServerPlayNetworking.send((ServerPlayerEntity) context.getPlayer(), URLMusicDiscs.CUSTOM_RECORD_GUI, bufInfo);
            }

            return ActionResult.PASS;
        }

        if (blockState.get(JukeboxBlock.HAS_RECORD).booleanValue()) {
            return ActionResult.PASS;
        }

        ItemStack itemStack = context.getStack();

        if (!world.isClient) {
            PlayerEntity playerEntity = context.getPlayer();
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
                jukeboxBlockEntity.setStack(itemStack.copy());
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, blockState));
            }
            itemStack.decrement(1);
            if (playerEntity != null) {
                playerEntity.incrementStat(Stats.PLAY_RECORD);
            }

            NbtCompound nbtInfo = context.getStack().getNbt();

            if (nbtInfo == null) {
                nbtInfo = new NbtCompound();
            }

            String musicUrl = nbtInfo.getString("music_url");

            if (musicUrl != null && !musicUrl.equals("")) {
                PacketByteBuf bufInfo = PacketByteBufs.create();
                bufInfo.writeBlockPos(blockPos);
                bufInfo.writeString(musicUrl);

                world.getPlayers().forEach(playerEntity1 -> {
                    ServerPlayNetworking.send((ServerPlayerEntity) playerEntity1, URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, bufInfo);
                });
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public int getSongLengthInTicks() {
        return 0;
    }
}
