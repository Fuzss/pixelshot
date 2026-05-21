package fuzs.pixelshot.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.event.v1.data.DefaultedDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
abstract class GameRendererFabricMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @ModifyReturnValue(method = "getFov", at = @At("TAIL"))
    private double getFov(double fieldOfViewValue, Camera camera, float partialTicks, boolean useFOVSetting) {
        DefaultedDouble fieldOfView = DefaultedDouble.fromValue(fieldOfViewValue);
        OrthoViewHandler.INSTANCE.onComputeFieldOfView(GameRenderer.class.cast(this),
                this.mainCamera,
                partialTicks,
                fieldOfView);
        return fieldOfView.getAsOptionalDouble().orElse(fieldOfViewValue);
    }
}
