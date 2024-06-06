package fuzs.pixelshot.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.pixelshot.handler.OrthoViewHandlerV2;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void renderClouds(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo callback) {
        if (OrthoViewHandlerV2.getInstance().isActive()) callback.cancel();
    }
}
