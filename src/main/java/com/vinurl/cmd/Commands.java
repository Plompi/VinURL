package com.vinurl.cmd;

import static com.vinurl.VinURL.*;
import com.vinurl.exe.FFmpeg;
import com.vinurl.exe.YoutubeDL;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;

import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class Commands {

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(MOD_ID)
				.then(ClientCommandManager.literal("delete").executes(Commands::deleteAudioFiles))
				.then(ClientCommandManager.literal("update").executes(Commands::updateExecutables))
				.then(ClientCommandManager.literal("config").executes(Commands::openConfig))
				.then(ClientCommandManager.literal("set").then(argument("url", StringArgumentType.string()).then(argument("loop",BoolArgumentType.bool()).executes(Commands::setURLToDisc))))
		));
	}

	private static int deleteAudioFiles(CommandContext<FabricClientCommandSource> ctx) {
		try {
			FileUtils.deleteDirectory(VINURLPATH.resolve("client_downloads").toFile());
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
		ConfigScreen screen = (ConfigScreen) Objects.requireNonNull(ConfigScreenProviders.get(MOD_ID)).apply(null);
		MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screen));
		return 0;
	}

	private static int setURLToDisc(CommandContext<FabricClientCommandSource> ctx) {
		NETWORK_CHANNEL.clientHandle().send(new SetURLRecord(StringArgumentType.getString(ctx, "url"), BoolArgumentType.getBool(ctx, "loop")));
		return 1;
	}
}