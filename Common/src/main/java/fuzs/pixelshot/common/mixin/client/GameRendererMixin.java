package fuzs.pixelshot.common.mixin.client;

import com.mojang.blaze3d.ProjectionType;
import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "renderLevel",
                    at = @At(value = "FIELD",
                             target = "Lnet/minecraft/client/renderer/GameRenderer;levelProjectionMatrixBuffer:Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;",
                             opcode = Opcodes.GETFIELD),
                    ordinal = 0)
    public Matrix4f renderLevel(Matrix4f projectionMatrix, DeltaTracker deltaTracker) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            return OrthoViewHandler.INSTANCE.createProjectionMatrix(this.minecraft,
                    deltaTracker.getGameTimeDeltaPartialTick(true),
                    false);
        } else {
            return projectionMatrix;
        }
    }

    @ModifyArg(method = "renderLevel",
               at = @At(value = "INVOKE",
                        target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V",
                        ordinal = 0),
               index = 1)
    private static ProjectionType renderLevelProjectionType(ProjectionType projectionType) {
        return OrthoViewHandler.INSTANCE.isActive() ? ProjectionType.ORTHOGRAPHIC : projectionType;
    }
}
