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

	public static void set(String id, int progressPercent) {
		if (progressQueue.put(id, MathHelper.clamp(progressPercent, 0, 100)) == null) {
			batchSize++;
		};
	}

	public static void stop(String id) {
		progressQueue.remove(id);
		if (progressQueue.isEmpty()){
			batchSize = 0;
			animationStep = 0;
			completedTime = 0;
			lastAnimationTime = 0;
		}
	}

	public static void render(DrawContext context) {
		if (progressQueue.isEmpty()) {return;}

		int percent = progressQueue.values().iterator().next();
		long now = System.currentTimeMillis();

		if (percent == 100 && completedTime == 0) {
			completedTime = now;
		} else if (percent < 100) {
			completedTime = 0;
			lastAnimationTime = 0;
		}

		boolean transcoding = percent == 100 && now - completedTime >= 1000;
		String status = transcoding ? "Transcoding" : "Downloading";
		Text progress;

		if (transcoding && now - lastAnimationTime >= 100) {
			if (lastAnimationTime != 0) {
				animationStep = (animationStep + 1) % 20;
			}
			lastAnimationTime = now;
		}

		if (!transcoding) {
			progress = Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(Text.literal("|".repeat(percent / 5)).formatted(Formatting.GREEN))
					.append(Text.literal("|".repeat(20 - percent / 5)).formatted(Formatting.GRAY));
		} else {
			progress = Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(Text.literal("|".repeat(animationStep)).formatted(Formatting.GRAY))
					.append(Text.literal("|").formatted(Formatting.BLUE))
					.append(Text.literal("|".repeat(19 - animationStep)).formatted(Formatting.GRAY));

		}

		context.drawTextWithShadow(CLIENT.textRenderer, status,
				(CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(status)) / 2,
				CLIENT.getWindow().getScaledHeight() - 72, 0xFFFFFF);

		context.drawTextWithShadow(CLIENT.textRenderer, progress,
				(CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(progress)) / 2,
				CLIENT.getWindow().getScaledHeight() - 62, 0xFFFFFF);
	}
}