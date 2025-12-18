package fuzs.pixelshot.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.helper.LegacyScreenshot;
import fuzs.pixelshot.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.Objects;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {

    @ModifyExpressionValue(method = "grabPanoramixScreenshot", at = @At(value = "CONSTANT", args = "intValue=4096"))
    public int grabPanoramixScreenshot(int pixelResolution) {
        // The method applies a downscale factor of 4, so we must multiply by 4.
        return Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.panoramicResolution * 4;
    }

    @Inject(method = "grabPanoramixScreenshot", at = @At(value = "HEAD"))
    public void grabPanoramixScreenshot(File gameDirectory, CallbackInfoReturnable<Component> callback, @Share(
            "screenshot_file") LocalRef<File> screenshotFile) {
        File file = LegacyScreenshot.getFile(new File(gameDirectory, Screenshot.SCREENSHOT_DIR), "", "");
        file.mkdirs();
        screenshotFile.set(file);
    }

    @ModifyArg(method = "grabPanoramixScreenshot",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"))
    public String grabPanoramixScreenshot(String fileName, @Share("screenshot_file") LocalRef<File> screenshotFile) {
        if (screenshotFile.get() != null) {
            return screenshotFile.get().getName() + File.separator + fileName;
        } else {
            return fileName;
        }
    }

    @ModifyVariable(method = "grabPanoramixScreenshot",
                    at = @At("LOAD"),
                    slice = @Slice(from = @At(value = "INVOKE",
                                              target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"),
                                   to = @At(value = "INVOKE",
                                            target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")),
                    argsOnly = true)
    public File grabPanoramixScreenshot(File gameDirectory, @Share("screenshot_file") LocalRef<File> screenshotFile) {
        // Setting an ordinal on the LOAD injection point does not work, so we need to slice instead.
        return Objects.requireNonNullElse(screenshotFile.get(), gameDirectory);
    }
}
