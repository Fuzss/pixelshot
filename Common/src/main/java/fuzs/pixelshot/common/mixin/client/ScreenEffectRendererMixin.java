package fuzs.pixelshot.common.mixin.client;

import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectRendererMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    public void submit(CallbackInfo callback) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            callback.cancel();
        }
    }
}
