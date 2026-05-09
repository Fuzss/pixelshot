package fuzs.pixelshot.common.client.handler;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.helper.LegacyScreenshot;
import fuzs.pixelshot.common.config.ClientConfig;
import fuzs.pixelshot.common.config.HugeScreenshotMode;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.common.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL12;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class ScreenshotHandler {
    public static final ScreenshotHandler INSTANCE = new ScreenshotHandler();
    public static final MutableComponent COMPONENT_SCREENSHOT_TAKE = Component.translatable("screenshot.take");
    public static final KeyMapping KEY_HIGH_RESOLUTION_SCREENSHOT = KeyMappingHelper.registerKeyMapping(Pixelshot.id(
            "high_resolution_screenshot"), InputConstants.KEY_F9);
    public static final KeyMapping KEY_PANORAMIC_SCREENSHOT = KeyMappingHelper.registerUnboundKeyMapping(Pixelshot.id(
            "panoramic_screenshot"));

    @Nullable
    private HugeScreenshotMode mode;
    private float zoom = 1.0F;
    private float zoomX;
    private float zoomY;

    public static void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(KEY_HIGH_RESOLUTION_SCREENSHOT, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_PANORAMIC_SCREENSHOT, KeyActivationContext.GAME);
    }

    public boolean isHugeScreenshotMode() {
        return this.mode != null;
    }

    public void prepareProjectionMatrix(Matrix4f projection) {
        if (this.mode == HugeScreenshotMode.ZOOM && this.zoom != 1.0F) {
            projection.translate(this.zoomX, -this.zoomY, 0.0F);
            projection.scale(this.zoom, this.zoom, 1.0F);
        }
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
                Consumer<Component> consumer = (Component component) -> minecraft.execute(() -> {
                    minecraft.gui.getChat().addClientSystemMessage(component);
                });
                HugeScreenshotMode mode = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.screenshotMode;
                this.setHugeScreenshotMode(mode);
                switch (mode) {
                    case RESIZE -> this.grabHugeScreenshot(minecraft,
                            originalWidth,
                            originalHeight,
                            imageWidth,
                            imageHeight,
                            consumer);
                    case ZOOM ->
                        // The vanilla method only works properly as we patch LevelRenderer::shouldShowEntityOutlines,
                        // otherwise the first tile is not resized and shows the full frame with the actual resolution.
                        // Still not ideal as it sometimes causes the game to freeze completely.
                            consumer.accept(this.grabHugeScreenshot(minecraft,
                                    originalWidth,
                                    originalHeight,
                                    imageWidth,
                                    imageHeight));
                }

                this.setHugeScreenshotMode(null);
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

    private void setHugeScreenshotMode(@Nullable HugeScreenshotMode mode) {
        this.mode = mode;
        Minecraft.getInstance().gameRenderer.setRenderBlockOutline(mode == null);
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
            String screenshotName = LegacyScreenshot.getFile(minecraft.gameDirectory, "huge_", ".png").getName();
            Screenshot.grab(minecraft.gameDirectory, screenshotName, target, 1, consumer);
            consumer.accept(COMPONENT_SCREENSHOT_TAKE);
        } finally {
            window.setWidth(originalWidth);
            window.setHeight(originalHeight);
            target.resize(originalWidth, originalHeight);
        }
    }

    /**
     * Copied from Minecraft 1.21.4's {@code Minecraft#grabHugeScreenshot(File, int, int, int, int)}.
     */
    private Component grabHugeScreenshot(Minecraft minecraft, int columnWidth, int rowHeight, int imageWidth, int imageHeight) {
        try {
            LegacyScreenshot screenshot = new LegacyScreenshot(minecraft.gameDirectory,
                    imageWidth,
                    imageHeight,
                    rowHeight);
            float relativeWidth = (float) imageWidth / (float) columnWidth;
            float relativeHeight = (float) imageHeight / (float) rowHeight;
            float zoom = Math.max(relativeWidth, relativeHeight);
            for (int height = (imageHeight - 1) / rowHeight * rowHeight; height >= 0; height -= rowHeight) {
                ByteBuffer byteBuffer = LegacyScreenshot.allocateMemory(columnWidth * rowHeight * 3);
                for (int width = 0; width < imageWidth; width += columnWidth) {
                    float zoomX = (float) (imageWidth - columnWidth) / 2.0F * 2.0F - (float) (width * 2);
                    float zoomY = (float) (imageHeight - rowHeight) / 2.0F * 2.0F - (float) (height * 2);
                    zoomX /= (float) columnWidth;
                    zoomY /= (float) rowHeight;
                    this.renderZoomed(minecraft.gameRenderer, zoom, zoomX, zoomY);
                    LegacyScreenshot.pixelStore(GL12.GL_PACK_ALIGNMENT, 1);
                    LegacyScreenshot.pixelStore(GL12.GL_UNPACK_ALIGNMENT, 1);
                    byteBuffer.clear();
                    LegacyScreenshot.readPixels(0,
                            0,
                            columnWidth,
                            rowHeight,
                            GL12.GL_BGR,
                            GL12.GL_UNSIGNED_BYTE,
                            byteBuffer);
                    screenshot.addRegion(byteBuffer, width, height, columnWidth, rowHeight);
                }

                screenshot.saveRow();
                LegacyScreenshot.freeMemory(byteBuffer);
            }

            File file = screenshot.close();
            Component component = Component.literal(file.getName())
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle((Style style) -> style.withClickEvent(new ClickEvent.OpenFile(file.getAbsoluteFile())));
            return Component.translatable("screenshot.success", component);
        } catch (Exception exception) {
            Pixelshot.LOGGER.warn("Couldn't save screenshot", exception);
            return Component.translatable("screenshot.failure", exception.getMessage());
        }
    }

    private void renderZoomed(GameRenderer gameRenderer, float zoom, float zoomX, float zoomY) {
        this.zoom = zoom;
        this.zoomX = zoomX;
        this.zoomY = zoomY;
        gameRenderer.update(DeltaTracker.ONE, true);
        gameRenderer.extract(DeltaTracker.ONE, true);
        gameRenderer.renderLevel(DeltaTracker.ONE);
        this.zoom = 1.0F;
    }
}
