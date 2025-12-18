package fuzs.pixelshot.mixin.client;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.pixelshot.client.handler.ScreenshotHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "renderLevel",
                    at = @At(value = "FIELD",
                             target = "Lnet/minecraft/client/renderer/GameRenderer;levelProjectionMatrixBuffer:Lnet/minecraft/client/renderer/PerspectiveProjectionMatrixBuffer;",
                             opcode = Opcodes.GETFIELD),
                    ordinal = 0)
    public Matrix4f renderLevel$0(Matrix4f matrix4f, DeltaTracker deltaTracker) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            return OrthoViewHandler.INSTANCE.getProjectionMatrix(this.minecraft,
                    deltaTracker.getGameTimeDeltaPartialTick(true),
                    false);
        } else {
            return matrix4f;
        }
    }

    @Inject(method = "getProjectionMatrixForCulling", at = @At("HEAD"), cancellable = true)
    private void getProjectionMatrixForCulling(CallbackInfoReturnable<Matrix4f> callback) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            callback.setReturnValue(OrthoViewHandler.INSTANCE.getProjectionMatrix(this.minecraft, 1.0F, true));
        }
    }

    @Inject(method = "isPanoramicMode", at = @At("HEAD"), cancellable = true)
    public void isPanoramicMode(CallbackInfoReturnable<Boolean> callback) {
        // without this most render buffers are not properly resized
        // only change this method, not the GameRenderer#panoramicMode flag which also controls field of view and held item rendering
        if (ScreenshotHandler.INSTANCE.isHugeScreenshotMode()) {
            callback.setReturnValue(true);
        }
    }
}
