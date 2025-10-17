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
	private static CompletableFuture<Boolean> waitingFuture;
	private static long timeout;

	public static void register() {
		acceptKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.vinurl.accept",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_Y,
			"category.vinurl"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (waitingFuture == null || waitingFuture.isDone()) return;

			if (acceptKey.isPressed()) {
				waitingFuture.complete(true);
				waitingFuture = null;
			} else if (System.currentTimeMillis() > timeout) {
				waitingFuture.complete(false);
				waitingFuture = null;
			}
		});
	}

	public static CompletableFuture<Boolean> waitForKeyPress() {
		waitingFuture = new CompletableFuture<>();
		timeout = System.currentTimeMillis() + KEY_PRESS_TIMEOUT_MILLIS;
		return waitingFuture;
	}

	public static String getHotKey() {
		return acceptKey.getBoundKeyLocalizedText().getLiteralString();
	}
}
