package urlmusicdiscs;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static urlmusicdiscs.URLMusicDiscs.CUSTOM_RECORD;

public class Commands {

    public static void register(){
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("urlmusicdiscs")
            .then(ClientCommandManager.literal("delete")
                .executes(context -> {
                    try {
                        FileUtils.deleteDirectory(URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/client_downloads").toFile());
                        context.getSource().sendFeedback(Text.literal("Deleted all Audio Files"));
                        return 1;
                    } catch (IOException e) {
                        context.getSource().sendFeedback(Text.literal("Deleted only non active Audio Files"));
                        return 0;
                    }
                })
            )

            .then(ClientCommandManager.literal("update").executes(context -> {
                context.getSource().sendFeedback(Text.literal("Checking for Updates..."));
                CompletableFuture.runAsync(() -> {
                    if (YoutubeDL.checkForUpdates() | FFmpeg.checkForUpdates()){
                        context.getSource().sendFeedback(Text.literal("Successfully updated Executables"));
                    }
                    else{
                        context.getSource().sendFeedback(Text.literal("No Updates found"));
                    }
                });
                return 1;
            }))

            .then(ClientCommandManager.literal("set")
                .then(argument("url", StringArgumentType.greedyString())
                    .executes(context -> {
                        String url = StringArgumentType.getString(context, "url");
                        ItemStack heldItem = context.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND);

                        if (heldItem.getItem() != CUSTOM_RECORD) {
                            context.getSource().sendFeedback(Text.of("URL-Music-Disc needed in Main Hand"));
                            return 0;
                        }

                        PacketByteBuf bufInfo = PacketByteBufs.create();
                        bufInfo.writeString(url);
                        ClientPlayNetworking.send(URLMusicDiscs.CUSTOM_RECORD_SET_URL, bufInfo);
                        return 1;
            })))
        ));
    }
}
