package fuzs.pixelshot.client.handler;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

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
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.setRenderBlockOutline(!hugeScreenshotMode);
        // need to manually disable held item rendering, panoramic mode does that on its own
        gameRenderer.setRenderHand(!hugeScreenshotMode);
    }

    public EventResult onKeyPress(int keyCode, int scanCode, int action, int modifiers) {
        Minecraft minecraft = Minecraft.getInstance();
        // don't allow this when screen is open, as it does not render the screen layer anyway
        // also means we do not have to deal with the keybindings screen when checking key mapping matches
        if (action == InputConstants.PRESS && minecraft.screen == null) {
            if (KEY_HIGH_RESOLUTION_SCREENSHOT.matches(keyCode, scanCode)) {
                int windowWidth = minecraft.getWindow().getWidth();
                int windowHeight = minecraft.getWindow().getHeight();
                int imageWidth = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.imageWidth;
                int imageHeight = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.imageHeight;
                Consumer<Component> consumer = component -> minecraft.execute(() -> minecraft.gui.getChat()
                        .addMessage(component));

                this.setHugeScreenshotMode(true);
                if (Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.tiledRendering) {
                    // the vanilla method only works properly as we patch LevelRenderer::shouldShowEntityOutlines,
                    // otherwise the first tile is not resized and shows the full frame with the actual resolution
                    // still not ideal as it sometimes causes the game to freeze completely
                    consumer.accept(minecraft.grabHugeScreenshot(minecraft.gameDirectory,
                            windowWidth,
                            windowHeight,
                            imageWidth,
                            imageHeight
                    ));
                } else {
                    this.grabHugeScreenshot(minecraft, windowWidth, windowHeight, imageWidth, imageHeight, consumer);
                }

                this.setHugeScreenshotMode(false);
            }

            if (KEY_PANORAMIC_SCREENSHOT.matches(keyCode, scanCode) && !OrthoViewHandler.INSTANCE.isActive()) {
                int panoramicResolution = Pixelshot.CONFIG.get(ClientConfig.class).highResolutionScreenshots.panoramicResolution;
                Component component = this.grabPanoramicScreenshot(minecraft,
                        minecraft.gameDirectory,
                        panoramicResolution,
                        panoramicResolution
                );

                minecraft.execute(() -> minecraft.gui.getChat().addMessage(component));
            }
        }

        return EventResult.PASS;
    }

    /**
     * Loosely based on {@link Minecraft#grabPanoramixScreenshot(File, int, int)} to allow for changing game resolution
     * before rendering the screenshot.
     * <p>
     * Limited by current system maximum window size, in that case tiled rendering works better.
     */
    private void grabHugeScreenshot(Minecraft minecraft, int windowWidth, int windowHeight, int imageWidth, int imageHeight, Consumer<Component> consumer) {
        Window window = minecraft.getWindow();
        RenderTarget renderTarget = new TextureTarget(imageWidth, imageHeight, true, Minecraft.ON_OSX);
        try {
            minecraft.levelRenderer.graphicsChanged();
            window.setWidth(imageWidth);
            window.setHeight(imageHeight);
            renderTarget.bindWrite(true);
            minecraft.gameRenderer.renderLevel(DeltaTracker.ONE);
            String screenshotName = getFile(minecraft.gameDirectory, "huge_", ".png").getName();
            Screenshot.grab(minecraft.gameDirectory, screenshotName, renderTarget, consumer);
            consumer.accept(COMPONENT_SCREENSHOT_TAKE);
        } finally {
            window.setWidth(windowWidth);
            window.setHeight(windowHeight);
            renderTarget.destroyBuffers();
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }

    private void setPanoramicMode(boolean panoramicMode) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.setRenderBlockOutline(!panoramicMode);
        gameRenderer.setPanoramicMode(panoramicMode);
    }

    /**
     * Copied from {@link Minecraft#grabPanoramixScreenshot(File, int, int)} with adjusted output path.
     */
    private Component grabPanoramicScreenshot(Minecraft minecraft, File gameDirectory, int width, int height) {
        Window window = minecraft.getWindow();
        Player player = minecraft.player;
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        RenderTarget renderTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        float xRot = player.getXRot();
        float yRot = player.getYRot();
        float xRotO = player.xRotO;
        float yRotO = player.yRotO;
        this.setPanoramicMode(true);

        try {
            minecraft.levelRenderer.graphicsChanged();
            window.setWidth(width);
            window.setHeight(height);

            File file = getFile(new File(minecraft.gameDirectory, Screenshot.SCREENSHOT_DIR), "", "");
            file.mkdirs();
            String fileName = file.getName();

            for (int i = 0; i < 6; ++i) {
                switch (i) {
                    case 0:
                        player.setYRot(yRot);
                        player.setXRot(0.0F);
                        break;
                    case 1:
                        player.setYRot((yRot + 90.0F) % 360.0F);
                        player.setXRot(0.0F);
                        break;
                    case 2:
                        player.setYRot((yRot + 180.0F) % 360.0F);
                        player.setXRot(0.0F);
                        break;
                    case 3:
                        player.setYRot((yRot - 90.0F) % 360.0F);
                        player.setXRot(0.0F);
                        break;
                    case 4:
                        player.setYRot(yRot);
                        player.setXRot(-90.0F);
                        break;
                    case 5:
                        player.setYRot(yRot);
                        player.setXRot(90.0F);
                        break;
                }

                player.yRotO = player.getYRot();
                player.xRotO = player.getXRot();
                renderTarget.bindWrite(true);
                minecraft.gameRenderer.renderLevel(DeltaTracker.ONE);

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException ignored) {
                    // NO-OP
                }

                Screenshot.grab(gameDirectory,
                        fileName + File.separator + "panorama_" + i + ".png",
                        renderTarget,
                        component -> {
                            // NO-OP
                        }
                );
            }

            Component component = Component.literal(fileName)
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,
                            file.getAbsolutePath()
                    )));
            return Component.translatable("screenshot.success", component);
        } catch (Exception exception) {
            Pixelshot.LOGGER.error("Couldn't save image", exception);
            return Component.translatable("screenshot.failure", exception.getMessage());
        } finally {
            player.setXRot(xRot);
            player.setYRot(yRot);
            player.xRotO = xRotO;
            player.yRotO = yRotO;
            this.setPanoramicMode(false);
            window.setWidth(windowWidth);
            window.setHeight(windowHeight);
            renderTarget.destroyBuffers();
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }

    /**
     * Copied from {@link Screenshot#getFile(File)}, adjusted to allow for file name alterations.
     */
    private static File getFile(File gameDirectory, String filePrefix, String filePostfix) {
        String fileName = filePrefix + Util.getFilenameFormattedDateTime();
        int i = 1;

        while (true) {
            File file = new File(gameDirectory, fileName + (i == 1 ? "" : "_" + i) + filePostfix);
            if (!file.exists()) {
                return file;
            }

            ++i;
        }
    }
}
