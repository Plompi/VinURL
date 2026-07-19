package com.vinurl.gui;

import net.minecraft.Util;

public class ProgressEntry {
	public static final int MIN = 0;
	public static final int MAX = 100;
	public static final int ERROR = -1;
	public static final long ERROR_TIMEOUT_MILLIS = 2000;
	public int progress;
	public long stateChangeTime;
	public ProgressState state;

	public ProgressEntry(int progress) {
		this.updateProgress(progress);
	}

	void updateProgress(int progress) {
		this.progress = (progress >= MIN && progress <= MAX) ? progress : ERROR;
		this.stateChangeTime = Util.getMillis();
		this.state = switch(this.progress) {
			case ERROR -> ProgressState.INTERRUPTED;
			case MAX -> ProgressState.TRANSCODING;
			default -> ProgressState.DOWNLOADING;
		};
	}

	boolean shouldRemove() {
		return state == ProgressState.INTERRUPTED && Util.getMillis() - stateChangeTime >= ERROR_TIMEOUT_MILLIS;
	}

	public enum ProgressState {
		DOWNLOADING,
		TRANSCODING,
		INTERRUPTED;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}
