package com.vinurl.gui;

public class ProgressEntry {
	int progress;
	long stateChangeTime;
	ProgressState state;

	public ProgressEntry(int progress) {
		this.updateProgress(progress);
	}

	void updateProgress(int progress) {
		this.progress = Math.clamp(progress, -1, 100);
		this.stateChangeTime = System.currentTimeMillis();
		this.state = switch(this.progress) {
			case -1 -> ProgressState.INTERRUPTED;
			case 100 -> ProgressState.TRANSCODING;
			default -> ProgressState.DOWNLOADING;
		};
	}

	boolean shouldRemove(long now) {
		return state == ProgressState.INTERRUPTED && now - stateChangeTime >= 2000;
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
