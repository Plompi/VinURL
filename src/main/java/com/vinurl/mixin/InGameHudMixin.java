package com.vinurl.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

	@Inject(method = "setNowPlaying", at = @At("HEAD"), cancellable = true)
	private void disableRecordOverlay(Component component, CallbackInfo ci) {
		if (component.equals(Component.translatable("item.vinurl.custom_record.desc"))) {
			ci.cancel();
		}
	}
}
