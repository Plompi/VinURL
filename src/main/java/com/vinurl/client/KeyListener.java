package com.vinurl.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

public class KeyListener {
	private static final int KEY_PRESS_TIMEOUT_MILLIS = 5000;
	private static KeyBinding acceptKey;

	public static void register() {
		acceptKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.vinurl.accept",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_Y,
			"category.vinurl"
		));
	}

	public static CompletableFuture<Boolean> waitForKeyPress() {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		long timeout = System.currentTimeMillis() + KEY_PRESS_TIMEOUT_MILLIS;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (future.isDone()) return;

			if (acceptKey.isPressed()) {
				future.complete(true);
			}

			if (System.currentTimeMillis() > timeout) {
				future.complete(false);
			}
		});

		return future;
	}

	public static String getHotKey() {
		return acceptKey.getBoundKeyLocalizedText().getLiteralString();
	}
}
