package com.vinurl.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.vinurl.client.SoundManager;
import com.vinurl.client.VinURLClient;
import com.vinurl.exe.Executable;
import io.wispforest.owo.config.ui.ConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.MOD_ID;


public class Commands {

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal(MOD_ID)
				.then(ClientCommandManager.literal("delete").executes(Commands::deleteAudioFiles))
				.then(ClientCommandManager.literal("update").executes(Commands::updateExecutables))
				.then(ClientCommandManager.literal("config").executes(Commands::openConfig))
			)
		);
	}

	private static int deleteAudioFiles(CommandContext<FabricClientCommandSource> ctx) {
		try {
			FileUtils.deleteDirectory(SoundManager.AUDIO_DIRECTORY.toFile());
			ctx.getSource().sendFeedback(Component.translatable("command.vinurl.delete.success"));
			return 1;
		} catch (IOException e) {
			ctx.getSource().sendFeedback(Component.translatable("command.vinurl.delete.exception"));
			return 0;
		}
	}

	private static int updateExecutables(CommandContext<FabricClientCommandSource> ctx) {
		ctx.getSource().sendFeedback(Component.translatable("command.vinurl.update.check"));
		CompletableFuture.runAsync(() -> {
			for (Executable exe : Executable.values()) {
				String current = exe.currentVersion();
				if (exe.checkForUpdates()) {
					String latest = exe.currentVersion();
					ctx.getSource().sendFeedback(Component.literal("%s: %s -> %s".formatted( exe, current, latest)));
				}
			}
			ctx.getSource().sendFeedback(Component.translatable("command.vinurl.update.latest"));
		});
		return 1;
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		ctx.getSource().getClient().schedule(() -> CLIENT.setScreen(ConfigScreen.create(VinURLClient.CONFIG, null)));
		return 1;
	}
}