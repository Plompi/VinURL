package com.vinurl.gui;

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
		this.stateChangeTime = System.currentTimeMillis();
		this.state = switch(this.progress) {
			case ERROR -> ProgressState.INTERRUPTED;
			case MAX -> ProgressState.TRANSCODING;
			default -> ProgressState.DOWNLOADING;
		};
	}

	boolean shouldRemove() {
		return state == ProgressState.INTERRUPTED && System.currentTimeMillis() - stateChangeTime >= ERROR_TIMEOUT_MILLIS;
	}

	public enum ProgressState {
		DOWNLOADING,
		TRANSCODING,
		INTERRUPTED;

		@Override
		public String toString() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}
	}
}
