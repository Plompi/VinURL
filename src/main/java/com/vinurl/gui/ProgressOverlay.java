package com.vinurl.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.vinurl.client.VinURLClient.CLIENT;

public class ProgressOverlay {
	private static final int BAR_SIZE = 20;
	private static int batchSize = 0;
	private static final LinkedHashMap<String, ProgressEntry> progressQueue = new LinkedHashMap<>();

	public static void set(String id, int progressPercent) {
		if(progressQueue.put(id, new ProgressEntry(progressPercent)) == null) {
			batchSize++;
		}
	}

	public static void stop(String id) {
		if (progressQueue.remove(id) != null && progressQueue.isEmpty()) {
			batchSize = 0;
		}
	}

	public static void stopFailed(String id) {
		progressQueue.put(id, new ProgressEntry(-1));
	}

	public static void render(DrawContext context) {
		if (progressQueue.isEmpty()) {return;}

		long now = System.currentTimeMillis();
		Map.Entry<String, ProgressEntry> firstEntry = progressQueue.entrySet().iterator().next();
		String currentId = firstEntry.getKey();
		ProgressEntry entry = firstEntry.getValue();

		if (entry.shouldRemove(now)) {
			stop(currentId);
			return;
		}

		Text progress = switch (entry.state) {
			case INTERRUPTED ->
				Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(createProgressText(20, Formatting.RED));
			case TRANSCODING -> {
				int animationStep = (int) ((now - entry.stateChangeTime) / 100) % BAR_SIZE;
				yield Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(createProgressText(animationStep, Formatting.GRAY))
					.append(createProgressText(1, Formatting.BLUE))
					.append(createProgressText(BAR_SIZE - 1 - animationStep, Formatting.GRAY));
			}
			default -> {
				int progressBars = BAR_SIZE * entry.progress / 100;
				yield Text.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(createProgressText(progressBars, Formatting.GREEN))
					.append(createProgressText(BAR_SIZE - progressBars, Formatting.GRAY));
			}
		};

		renderText(context, Text.literal(entry.state.toString()), 72);
		renderText(context, progress, 62);
	}

	private static Text createProgressText(int count, Formatting formatting) {
		return Text.literal("|".repeat(count)).formatted(formatting);
	}

	private static void renderText(DrawContext context, Text text, int offset) {
		context.drawTextWithShadow(CLIENT.textRenderer, text,
			(CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(text)) / 2,
			CLIENT.getWindow().getScaledHeight() - offset, 0xFFFFFF);
	}
}