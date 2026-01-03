package com.vinurl.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.concurrent.CompletableFuture;

public class KeyListener {
	private static final int KEY_PRESS_TIMEOUT_MILLIS = 5000;
	private static KeyMapping acceptKey;
	private static CompletableFuture<Boolean> waitingFuture;
	private static long timeout;

	public static void register() {
		acceptKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.vinurl.accept",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_Y,
			"key.category.vinurl.mapping"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (waitingFuture == null || waitingFuture.isDone()) {return;}

			if (acceptKey.isDown()) {
				waitingFuture.complete(true);
				waitingFuture = null;
			} else if (System.currentTimeMillis() > timeout) {
				waitingFuture.complete(false);
				waitingFuture = null;
			}
		});
	}

	public static CompletableFuture<Boolean> waitForKeyPress() {
		timeout = System.currentTimeMillis() + KEY_PRESS_TIMEOUT_MILLIS;
		return (waitingFuture = new CompletableFuture<>());
	}

	public static String getHotKey() {
		return acceptKey.getTranslatedKeyMessage().tryCollapseToString();
	}
}
