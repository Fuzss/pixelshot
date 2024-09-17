package fuzs.pixelshot.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.pixelshot.client.handler.ScreenshotHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @ModifyVariable(
            method = "renderLevel", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V",
            shift = At.Shift.BEFORE
    ), ordinal = 0
    )
    public Matrix4f renderLevel$0(Matrix4f matrix4f, DeltaTracker deltaTracker) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            return OrthoViewHandler.INSTANCE.getProjectionMatrix(this.minecraft, deltaTracker.getGameTimeDeltaPartialTick(true), false);
        } else {
            return matrix4f;
        }
    }

    @WrapOperation(
            method = "renderLevel", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;getProjectionMatrix(D)Lorg/joml/Matrix4f;"
    ), slice = @Slice(
            from = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"
            )
    )
    )
    public Matrix4f renderLevel$1(GameRenderer gameRenderer, double fov, Operation<Matrix4f> operation) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            return OrthoViewHandler.INSTANCE.getProjectionMatrix(this.minecraft, 1.0F, true);
        } else {
            return operation.call(gameRenderer, fov);
        }
    }

    @Inject(method = "isPanoramicMode", at = @At("HEAD"), cancellable = true)
    public void isPanoramicMode(CallbackInfoReturnable<Boolean> callback) {
        // without this most render buffers are not properly resized
        // only change this method, not the GameRenderer#panoramicMode flag which also controls field of view and held item rendering
        if (ScreenshotHandler.INSTANCE.isHugeScreenshotMode()) callback.setReturnValue(true);
    }
}
