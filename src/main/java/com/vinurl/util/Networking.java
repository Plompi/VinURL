package com.vinurl.util;

import net.minecraft.util.math.BlockPos;

public class Networking {

	public record PlaySoundRecord(BlockPos position, String url, boolean loop) {}

	public record SetURLRecord(String url, boolean loop) {}

	public record GUIRecord(String url, boolean loop) {}
}
