package urlmusicdiscs.items;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import urlmusicdiscs.URLMusicDiscs;
import net.minecraft.util.Hand;

public class URLDiscItem extends MusicDiscItem {
    public URLDiscItem(int comparatorOutput, SoundEvent sound, Settings settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings, lengthInSeconds);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (!world.isClient) {
            PacketByteBuf bufInfo = PacketByteBufs.create();
            bufInfo.writeItemStack(stackInHand);
            ServerPlayNetworking.send((ServerPlayerEntity) player, URLMusicDiscs.CUSTOM_RECORD_GUI, bufInfo);
        }
        return TypedActionResult.success(stackInHand);
    }
}