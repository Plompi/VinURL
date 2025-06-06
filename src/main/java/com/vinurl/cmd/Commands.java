package com.vinurl.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.vinurl.client.AudioHandler;
import com.vinurl.exe.Executable;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.vinurl.util.Constants.MOD_ID;


public class Commands {

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(MOD_ID)
				.then(ClientCommandManager.literal("delete").executes(Commands::deleteAudioFiles))
				.then(ClientCommandManager.literal("update").executes(Commands::updateExecutables))
				.then(ClientCommandManager.literal("config").executes(Commands::openConfig))
		));
	}

	private static int deleteAudioFiles(CommandContext<FabricClientCommandSource> ctx) {
		try {
			FileUtils.deleteDirectory(AudioHandler.AUDIO_DIRECTORY.toFile());
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
			boolean anyUpdate = false;
			for (Executable executable : Executable.values()) {
				if(executable.checkForUpdates()){
					ctx.getSource().sendFeedback(Text.literal("Successfully updated " + executable.name()));
					anyUpdate = true;
				}
			}
			if (!anyUpdate) {
				ctx.getSource().sendFeedback(Text.literal("No updates found."));
			}
		});
		return 1;
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		ConfigScreen screen = (ConfigScreen) Objects.requireNonNull(ConfigScreenProviders.get(MOD_ID)).apply(null);
		MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screen));
		return 0;
	}
}