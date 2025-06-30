package com.vinurl.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Inject(method = "setRecordPlayingOverlay", at = @At("HEAD"), cancellable = true)
	private void disableRecordOverlay(Text description, CallbackInfo cir) {
		if (description.getString().equals("No Song")) {
			cir.cancel();
		}
	}
}
