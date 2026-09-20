package fuzs.pixelshot.common.mixin.client;

import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
abstract class CloudRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/CloudStatus;Lcom/mojang/renderpearl/api/commands/RenderPass;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void render(CallbackInfo callback) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderOit", at = @At("HEAD"), cancellable = true)
    public void renderOit(CallbackInfo callback) {
        if (OrthoViewHandler.INSTANCE.isActive()) {
            callback.cancel();
        }
    }
}
