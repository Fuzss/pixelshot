package fuzs.pixelshot.mixin.client;

import fuzs.pixelshot.client.handler.ScreenshotHandler;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
abstract class FrustumMixin {

    @Inject(method = "offsetToFullyIncludeCameraCube", at = @At("HEAD"), cancellable = true)
    public void offsetToFullyIncludeCameraCube(int offset, CallbackInfoReturnable<Frustum> callback) {
        if (ScreenshotHandler.INSTANCE.isHugeScreenshotMode()) {
            callback.setReturnValue(Frustum.class.cast(this));
        }
    }
}
