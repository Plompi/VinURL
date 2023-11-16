package urlmusicdiscs.mixin;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Identifier.class)
public class IdentifierMixin {
	@Inject(at = @At("HEAD"), method = "validatePath", cancellable = true)
	private static void validatePath(String namespace, String path, CallbackInfoReturnable<String> cir) {
		if (namespace.equals("urlmusicdiscs")) {
			cir.setReturnValue(path);
			cir.cancel();
		}
	}
}