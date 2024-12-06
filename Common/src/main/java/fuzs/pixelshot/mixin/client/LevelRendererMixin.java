package fuzs.pixelshot.mixin.client;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {

    @Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
    public void addCloudsPass(CallbackInfo callback) {
        if (OrthoViewHandler.INSTANCE.isActive()) callback.cancel();
    }
}
