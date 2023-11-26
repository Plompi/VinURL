package urlmusicdiscs.mixin;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Identifier.class)
public class IdentifierMixin {
	@Inject(at = @At("HEAD"), method = "isPathValid", cancellable = true)
	private static void isPathValid(String path, CallbackInfoReturnable<Boolean> cir) {
		if (path.startsWith("customsound") || path.startsWith("sounds/customsound")) {
			cir.setReturnValue(true);
			cir.cancel();
		}
	}
}