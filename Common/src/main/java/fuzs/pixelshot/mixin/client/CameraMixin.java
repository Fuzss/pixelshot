package fuzs.pixelshot.mixin.client;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.pixelshot.client.handler.ScreenshotHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
abstract class CameraMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "createProjectionMatrixForCulling", at = @At("HEAD"), cancellable = true)
    private void createProjectionMatrixForCulling(CallbackInfoReturnable<Matrix4f> callback) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            callback.setReturnValue(OrthoViewHandler.INSTANCE.getProjectionMatrix(this.minecraft, 1.0F, true));
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    public void extractRenderState(CameraRenderState cameraState, float cameraEntityPartialTicks, CallbackInfo callback) {
        // Without this most render buffers are not properly resized
        // Only change this method, not the corresponding field in this instance as it also controls field of view.
        if (ScreenshotHandler.INSTANCE.isHugeScreenshotMode()) {
            cameraState.isPanoramicMode = true;
        }
    }
}
