package com.vinurl.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.vinurl.client.AudioHandler;
import com.vinurl.exe.Executable;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.Objects;
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
			FileUtils.deleteDirectory(AudioHandler.AUDIO_DIRECTORY.toFile());
			ctx.getSource().sendFeedback(Text.literal("Deleted all audio files"));
			return 1;
		} catch (IOException e) {
			ctx.getSource().sendFeedback(Text.literal("Deleted only non active audio files"));
			return 0;
		}
	}

	private static int updateExecutables(CommandContext<FabricClientCommandSource> ctx) {
		ctx.getSource().sendFeedback(Text.literal("Checking for updates..."));
		CompletableFuture.runAsync(() -> {
			boolean anyUpdate = false;
			for (Executable executable : Executable.values()) {
				String current = executable.currentVersion();
				if(executable.checkForUpdates()){
					String latest = executable.currentVersion();
					ctx.getSource().sendFeedback(Text.literal(String.format("%s: %s -> %s", executable, current, latest)));
					anyUpdate = true;
				}
			}
			if (!anyUpdate) {
				ctx.getSource().sendFeedback(Text.literal("Everything is up to date!"));
			}
		});
		return 1;
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		CLIENT.send(() -> CLIENT.setScreen(Objects.requireNonNull(ConfigScreenProviders.get(MOD_ID)).apply(null)));
		return 0;
	}
}