package com.vinurl.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashMap;

import static com.vinurl.client.VinURLClient.CLIENT;

public class ProgressOverlay {
	private static final LinkedHashMap<String, Integer> progressQueue = new LinkedHashMap<>();
	private static int animationStep;
	private static long lastAnimationTime;
	private static long completedTime;
	private static int batchSize;
	private static long failedDisplayTime;

	public static void set(String id, int progressPercent) {
		if (progressQueue.put(id, MathHelper.clamp(progressPercent, 0, 100)) == null) {
			batchSize++;
		}
	}

	public static void stop(String id) {
		progressQueue.remove(id);
		if (progressQueue.isEmpty()) {
			batchSize = 0;
			animationStep = 0;
			completedTime = 0;
			lastAnimationTime = 0;
			failedDisplayTime = 0;
		}
	}

	public static void stopFailed(String id) {
		if (progressQueue.containsKey(id)) {
			progressQueue.put(id, -1);
			failedDisplayTime = System.currentTimeMillis();
		}
	}

	public static void render(DrawContext context) {
		if (progressQueue.isEmpty()) {return;}

		String currentId = progressQueue.firstEntry().getKey();
		int percent = progressQueue.get(currentId);
		long now = System.currentTimeMillis();

		if (percent == -1 && now - failedDisplayTime >= 2000) {
			stop(currentId);
			return;
		}

		if (percent == 100 && completedTime == 0) {
			completedTime = now;
		} else if (percent < 100 && percent != -1) {
			completedTime = 0;
			lastAnimationTime = 0;
		}

		String status;
		Text progress;

		if (percent == -1) {
			status = "Interrupted";
			progress = Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(Text.literal("|".repeat(20)).formatted(Formatting.RED));
		} else if (percent == 100 && now - completedTime >= 1000) {
			status = "Transcoding";
			if (now - lastAnimationTime >= 100) {
				if (lastAnimationTime != 0) {
					animationStep = (animationStep + 1) % 20;
				}
				lastAnimationTime = now;
			}
			progress = Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
				.append(Text.literal("|".repeat(animationStep)).formatted(Formatting.GRAY))
				.append(Text.literal("|").formatted(Formatting.BLUE))
				.append(Text.literal("|".repeat(19 - animationStep)).formatted(Formatting.GRAY));
		} else {
			status = "Downloading";
			progress = Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
				.append(Text.literal("|".repeat(percent / 5)).formatted(Formatting.GREEN))
				.append(Text.literal("|".repeat(20 - percent / 5)).formatted(Formatting.GRAY));
		}

		context.drawTextWithShadow(CLIENT.textRenderer, status,
			(CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(status)) / 2,
			CLIENT.getWindow().getScaledHeight() - 72, 0xFFFFFF
		);

		context.drawTextWithShadow(CLIENT.textRenderer, progress,
			(CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(progress)) / 2,
			CLIENT.getWindow().getScaledHeight() - 62, 0xFFFFFF
		);
	}
}