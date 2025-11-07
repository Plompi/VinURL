package com.vinurl.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

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
		progressQueue.put(id, new ProgressEntry(ProgressEntry.ERROR));
	}

	public static void render(GuiGraphics context) {
		if (progressQueue.isEmpty()) {return;}

		long now = System.currentTimeMillis();
		Map.Entry<String, ProgressEntry> firstEntry = progressQueue.entrySet().iterator().next();
		String currentId = firstEntry.getKey();
		ProgressEntry entry = firstEntry.getValue();

		if (entry.shouldRemove(now)) {
			stop(currentId);
			return;
		}

		Component progress = switch (entry.state) {
			case INTERRUPTED ->
				Component.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(createProgressText(20, ChatFormatting.RED));
			case TRANSCODING -> {
				int animationStep = (int) ((now - entry.stateChangeTime) / 100) % BAR_SIZE;
				yield Component.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(createProgressText(animationStep, ChatFormatting.GRAY))
					.append(createProgressText(1, ChatFormatting.BLUE))
					.append(createProgressText(BAR_SIZE - 1 - animationStep, ChatFormatting.GRAY));
			}
			default -> {
				int progressBars = BAR_SIZE * entry.progress / 100;
				yield Component.literal(String.format("%d/%d ", batchSize - (progressQueue.size() - 1), batchSize))
					.append(createProgressText(progressBars, ChatFormatting.GREEN))
					.append(createProgressText(BAR_SIZE - progressBars, ChatFormatting.GRAY));
			}
		};

		renderText(context, Component.literal(entry.state.toString()), 72);
		renderText(context, progress, 62);
	}

	private static Component createProgressText(int count, ChatFormatting formatting) {
		return Component.literal("|".repeat(count)).withStyle(formatting);
	}

	private static void renderText(GuiGraphics context, Component text, int offset) {
		context.drawString(CLIENT.font, text,
			(CLIENT.getWindow().getGuiScaledWidth() - CLIENT.font.width(text)) / 2,
			CLIENT.getWindow().getGuiScaledHeight() - offset, 0xFFFFFF);
	}
}