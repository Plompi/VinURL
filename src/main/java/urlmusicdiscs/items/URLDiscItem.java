package urlmusicdiscs.items;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import urlmusicdiscs.URLMusicDiscs;
import net.minecraft.util.Hand;

public class URLDiscItem extends MusicDiscItem {
    public URLDiscItem(int comparatorOutput, SoundEvent sound, Settings settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings, lengthInSeconds);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (!world.isClient) {
            PacketByteBuf bufInfo = PacketByteBufs.create();
            bufInfo.writeItemStack(stackInHand);

            ServerPlayNetworking.send((ServerPlayerEntity) player, URLMusicDiscs.CUSTOM_RECORD_GUI, bufInfo);
        }

        return TypedActionResult.success(stackInHand);
    }

    @Override
    public int getSongLengthInTicks() {
        return 0;
    }
}