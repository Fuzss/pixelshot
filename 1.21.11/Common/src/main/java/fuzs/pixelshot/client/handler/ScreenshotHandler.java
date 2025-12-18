package fuzs.pixelshot.client.handler;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.helper.LegacyScreenshot;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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
        // don't allow this when screen is open, as it does not render the screen layer anyway
        // also means we do not have to deal with the keybindings screen when checking key mapping matches
        if (action == InputConstants.PRESS && minecraft.screen == null) {
            if (KeyMappingHelper.isKeyActiveAndMatches(KEY_HIGH_RESOLUTION_SCREENSHOT, keyEvent)) {
                int windowWidth = minecraft.getWindow().getWidth();
                int windowHeight = minecraft.getWindow().getHeight();
                int imageWidth = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.imageWidth;
                int imageHeight = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.imageHeight;
                Consumer<Component> consumer = (Component component) -> minecraft.execute(() -> minecraft.gui.getChat()
                        .addMessage(component));
                this.setHugeScreenshotMode(true);
                if (Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.tiledRendering) {
                    // the vanilla method only works properly as we patch LevelRenderer::shouldShowEntityOutlines,
                    // otherwise the first tile is not resized and shows the full frame with the actual resolution
                    // still not ideal as it sometimes causes the game to freeze completely
                    consumer.accept(this.grabHugeScreenshot(minecraft,
                            windowWidth,
                            windowHeight,
                            imageWidth,
                            imageHeight));
                } else {
                    this.grabHugeScreenshot(minecraft, windowWidth, windowHeight, imageWidth, imageHeight, consumer);
                }

                this.setHugeScreenshotMode(false);
            }

            if (KeyMappingHelper.isKeyActiveAndMatches(KEY_PANORAMIC_SCREENSHOT, keyEvent)
                    && !OrthoViewHandler.INSTANCE.isActive()) {
                Component component = minecraft.grabPanoramixScreenshot(minecraft.gameDirectory);
                minecraft.execute(() -> minecraft.gui.getChat().addMessage(component));
            }
        }

        return EventResult.PASS;
    }

    /**
     * Copied from Minecraft 1.21.4's {@code Minecraft#grabHugeScreenshot(File, int, int, int, int)}.
     */
    private Component grabHugeScreenshot(Minecraft minecraft, int columnWidth, int rowHeight, int width, int height) {
        try {
            LegacyScreenshot screenshot = new LegacyScreenshot(minecraft.gameDirectory, width, height, rowHeight);
            float f = (float) width / (float) columnWidth;
            float g = (float) height / (float) rowHeight;
            float h = f > g ? f : g;

            for (int i = (height - 1) / rowHeight * rowHeight; i >= 0; i -= rowHeight) {
                ByteBuffer byteBuffer = LegacyScreenshot.allocateMemory(columnWidth * rowHeight * 3);
                for (int j = 0; j < width; j += columnWidth) {
                    float k = (float) (width - columnWidth) / 2.0F * 2.0F - (float) (j * 2);
                    float l = (float) (height - rowHeight) / 2.0F * 2.0F - (float) (i * 2);
                    k /= (float) columnWidth;
                    l /= (float) rowHeight;
//                    minecraft.gameRenderer.renderZoomed(h, k, l);
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
                    screenshot.addRegion(byteBuffer, j, i, columnWidth, rowHeight);
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

    /**
     * Loosely based on {@link Minecraft#grabPanoramixScreenshot(File)} to allow for changing game resolution before
     * rendering the screenshot.
     * <p>
     * Limited by the current system maximum window size, in that case tiled rendering works better.
     */
    private void grabHugeScreenshot(Minecraft minecraft, int windowWidth, int windowHeight, int imageWidth, int imageHeight, Consumer<Component> consumer) {
        Window window = minecraft.getWindow();
        RenderTarget renderTarget = minecraft.getMainRenderTarget();
        try {
            window.setWidth(imageWidth);
            window.setHeight(imageHeight);
            renderTarget.resize(imageWidth, imageHeight);
            minecraft.gameRenderer.renderLevel(DeltaTracker.ONE);
            String screenshotName = LegacyScreenshot.getFile(minecraft.gameDirectory, "huge_", ".png").getName();
            Screenshot.grab(minecraft.gameDirectory, screenshotName, renderTarget, 1, consumer);
            consumer.accept(COMPONENT_SCREENSHOT_TAKE);
        } finally {
            window.setWidth(windowWidth);
            window.setHeight(windowHeight);
            renderTarget.resize(windowWidth, windowHeight);
        }
    }
}
