package com.vinurl.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.vinurl.VinURL;
import com.vinurl.exe.FFmpeg;
import com.vinurl.exe.YoutubeDL;
import io.wispforest.owo.config.ui.ConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.vinurl.VinURL.CUSTOM_RECORD;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class Commands {

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(VinURL.MOD_ID)
				.then(ClientCommandManager.literal("delete").executes(Commands::deleteAudioFiles))
				.then(ClientCommandManager.literal("update").executes(Commands::updateExecutables))
				.then(ClientCommandManager.literal("config").executes(Commands::openConfig))
				.then(ClientCommandManager.literal("set").then(argument("url", StringArgumentType.string()).executes(Commands::setURLToDisc)))
		));
	}

	private static int deleteAudioFiles(CommandContext<FabricClientCommandSource> ctx) {
		try {
			FileUtils.deleteDirectory(VinURL.VINURLPATH.resolve("client_downloads").toFile());
			ctx.getSource().sendFeedback(Text.literal("Deleted all Audio Files"));
			return 1;
		} catch (IOException e) {
			ctx.getSource().sendFeedback(Text.literal("Deleted only non active Audio Files"));
			return 0;
		}
	}

	private static int updateExecutables(CommandContext<FabricClientCommandSource> ctx) {
		ctx.getSource().sendFeedback(Text.literal("Checking for Updates..."));
		CompletableFuture.runAsync(() -> {
			if (YoutubeDL.getInstance().checkForUpdates() | FFmpeg.getInstance().checkForUpdates()) {
				ctx.getSource().sendFeedback(Text.literal("Successfully updated Executables"));
			} else {
				ctx.getSource().sendFeedback(Text.literal("No Updates found"));
			}
		});
		return 1;
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		ConfigScreen screen = Objects.requireNonNull(ConfigScreen.getProvider(VinURL.MOD_ID)).apply(null);
		MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screen));
		return 0;
	}

	private static int setURLToDisc(CommandContext<FabricClientCommandSource> ctx) {
		String url = StringArgumentType.getString(ctx, "url");
		ItemStack heldItem = ctx.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND);

		if (heldItem.getItem() != CUSTOM_RECORD) {
			ctx.getSource().sendFeedback(Text.of("VinURL-Disc needed in Main Hand"));
			return 0;
		}

		ClientPlayNetworking.send(new VinURL.SetURLPayload(url));
		return 1;
	}
}