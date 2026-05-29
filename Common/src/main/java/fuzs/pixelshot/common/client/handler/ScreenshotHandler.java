package fuzs.pixelshot.common.client.handler;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.config.ClientConfig;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.common.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

import java.io.File;
import java.util.function.Consumer;

public class ScreenshotHandler {
    public static final ScreenshotHandler INSTANCE = new ScreenshotHandler();
    public static final MutableComponent COMPONENT_SCREENSHOT_TAKE = Component.translatable("screenshot.take");
    public static final KeyMapping KEY_HIGH_RESOLUTION_SCREENSHOT = KeyMappingHelper.registerKeyMapping(Pixelshot.id(
            "high_resolution_screenshot"), InputConstants.KEY_F9);
    public static final KeyMapping KEY_PANORAMIC_SCREENSHOT = KeyMappingHelper.registerUnboundKeyMapping(Pixelshot.id(
            "panoramic_screenshot"));

    private boolean hugeScreenshotMode;

    public static void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(KEY_HIGH_RESOLUTION_SCREENSHOT, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_PANORAMIC_SCREENSHOT, KeyActivationContext.GAME);
    }

    public boolean isHugeScreenshotMode() {
        return this.hugeScreenshotMode;
    }

    private void setHugeScreenshotMode(boolean hugeScreenshotMode) {
        this.hugeScreenshotMode = hugeScreenshotMode;
        Minecraft.getInstance().gameRenderer.setRenderBlockOutline(!hugeScreenshotMode);
    }

    public EventResult onKeyPress(KeyEvent keyEvent, int action) {
        Minecraft minecraft = Minecraft.getInstance();
        // Don't allow this when any screen is open, as it does not render the screen layer anyway.
        // Also means we do not have to deal with the keybindings screen when checking key mapping matches.
        if (action == InputConstants.PRESS && minecraft.screen == null) {
            if (KeyMappingHelper.isKeyActiveAndMatches(KEY_HIGH_RESOLUTION_SCREENSHOT, keyEvent)) {
                int originalWidth = minecraft.getWindow().getWidth();
                int originalHeight = minecraft.getWindow().getHeight();
                int imageWidth = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.imageWidth;
                int imageHeight = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.imageHeight;
                this.setHugeScreenshotMode(true);
                this.grabHugeScreenshot(minecraft,
                        originalWidth,
                        originalHeight,
                        imageWidth,
                        imageHeight,
                        (Component component) -> minecraft.execute(() -> {
                            minecraft.gui.getChat().addClientSystemMessage(component);
                        }));
                this.setHugeScreenshotMode(false);
            }

            if (KeyMappingHelper.isKeyActiveAndMatches(KEY_PANORAMIC_SCREENSHOT, keyEvent)
                    && !OrthoViewHandler.INSTANCE.isActive()) {
                Component component = minecraft.grabPanoramixScreenshot(minecraft.gameDirectory);
                minecraft.execute(() -> {
                    minecraft.gui.getChat().addClientSystemMessage(component);
                });
            }
        }

        return EventResult.PASS;
    }

    /**
     * Loosely based on {@link Minecraft#grabPanoramixScreenshot(File)} to allow for changing game resolution before
     * rendering the screenshot.
     * <p>
     * Limited by the current system maximum window size, in that case tiled rendering works better.
     */
    private void grabHugeScreenshot(Minecraft minecraft, int originalWidth, int originalHeight, int imageWidth, int imageHeight, Consumer<Component> consumer) {
        Window window = minecraft.getWindow();
        RenderTarget target = minecraft.getMainRenderTarget();
        try {
            window.setWidth(imageWidth);
            window.setHeight(imageHeight);
            target.resize(imageWidth, imageHeight);
            minecraft.gameRenderer.update(DeltaTracker.ONE, true);
            minecraft.gameRenderer.extract(DeltaTracker.ONE, true);
            minecraft.gameRenderer.renderLevel(DeltaTracker.ONE);
            String fileName = getFile(minecraft.gameDirectory, "huge_", ".png").getName();
            Screenshot.grab(minecraft.gameDirectory, fileName, target, 1, consumer);
            consumer.accept(COMPONENT_SCREENSHOT_TAKE);
        } finally {
            window.setWidth(originalWidth);
            window.setHeight(originalHeight);
            target.resize(originalWidth, originalHeight);
        }
    }

    /**
     * Adjusted to allow for a custom file name.
     *
     * @see Screenshot#getFile(File)
     */
    public static File getFile(File gameDirectory, String fileNamePrefix, String fileExtension) {
        String fileName = fileNamePrefix + Util.getFilenameFormattedDateTime();
        int fileCount = 1;
        while (true) {
            File file = new File(gameDirectory, fileName + (fileCount == 1 ? "" : "_" + fileCount) + fileExtension);
            if (!file.exists()) {
                return file;
            }

            ++fileCount;
        }
    }
}
