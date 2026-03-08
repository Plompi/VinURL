package com.vinurl.exe;

public enum Platform {
	WIN_X64, WIN_ARM64,
	MAC_X64, MAC_ARM64,
	LIN_X64, LIN_ARM64,
	NOT_SUPPORTED;

	public static final Platform PLATFORM = detect();

	public static Platform detect() {
		if (com.sun.jna.Platform.isWindows()) {
			return com.sun.jna.Platform.isARM() ? WIN_ARM64 : WIN_X64;
		} else if (com.sun.jna.Platform.isMac()) {
			return com.sun.jna.Platform.isARM() ? MAC_ARM64 : MAC_X64;
		} else if (com.sun.jna.Platform.isLinux()) {
			return com.sun.jna.Platform.isARM() ? LIN_ARM64 : LIN_X64;
		} else {
			return NOT_SUPPORTED;
		}
	}
}
