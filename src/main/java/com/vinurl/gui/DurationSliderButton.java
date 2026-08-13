package com.vinurl.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DurationSliderButton extends AbstractSliderButton {
	private final int maxDuration;

	public DurationSliderButton(int x, int y, int width, int height, Component message, int maxDuration) {
		super(x, y, width, height, message, 0);
		this.maxDuration = maxDuration;
	}

	public int getDuration() {
		return (int) (value * (double) maxDuration);
	}

	public void setDuration(int duration) {
		double percentage = (double) duration / (double) maxDuration;
		value = Mth.clamp(percentage, 0, 1.0);

		this.applyValue();
		this.updateMessage();
	}

	@Override
	protected void applyValue() {
	}

	@Override
	protected void updateMessage() {
		int duration = getDuration();
		String formatted = "%02d:%02d".formatted(duration / 60, duration % 60);
		setMessage(Component.literal(formatted));
	}
}
