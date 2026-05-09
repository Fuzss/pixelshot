package fuzs.pixelshot.common.config;

import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.common.api.config.v3.Config;
import fuzs.puzzleslib.common.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config
    public final OrthographicCamera orthographicCamera = new OrthographicCamera();
    @Config
    public final HighResolutionScreenshots highResolutionScreenshots = new HighResolutionScreenshots();
    @Config
    public final PanoramicScreenshots panoramicScreenshots = new PanoramicScreenshots();

    public static class OrthographicCamera implements ConfigCore {
        @Config(description = "Default zoom level when opening the orthographic camera.")
        @Config.DoubleRange(min = OrthoViewHandler.ZOOM_MIN, max = OrthoViewHandler.ZOOM_MAX)
        public double initialZoomLevel = OrthoViewHandler.ZOOM_DEFAULT;
        @Config(description = "Default rotation in degrees when opening the orthographic camera for the horizontal axis.")
        @Config.DoubleRange(min = OrthoViewHandler.X_ROTATION_MIN, max = OrthoViewHandler.X_ROTATION_MAX)
        public double initialXRotation = OrthoViewHandler.X_ROTATION_DEFAULT;
        @Config(description = "Default rotation in degrees when opening the orthographic camera for the vertical axis.")
        @Config.DoubleRange(min = OrthoViewHandler.Y_ROTATION_MIN, max = OrthoViewHandler.Y_ROTATION_MAX)
        public double initialYRotation = OrthoViewHandler.Y_ROTATION_DEFAULT;
        @Config(description = "Default setting for player rendering when opening the orthographic camera.")
        public boolean initialPlayerRendering = true;
    }

    public static class HighResolutionScreenshots implements ConfigCore {
        @Config(description = {
                "Horizontal amount of pixels for high resolution screenshots.",
                "Largest allowed value depends on current system and will crash when unsupported. If that happens enable tiled rendering."
        })
        @Config.IntRange(min = 1)
        public int imageWidth = 3840;
        @Config(description = {
                "Vertical amount of pixels for high resolution screenshots.",
                "Largest allowed value depends on current system and will crash when unsupported. If that happens enable tiled rendering."
        })
        @Config.IntRange(min = 1)
        public int imageHeight = 2160;
        @Config(description = {
                "Alternative high-resolution screenshot rendering method will halt the game while taking the screenshot. Supports higher resolutions than the default rendering.",
                "Rarely causes an unexpected indefinite client freeze while capturing. Use with caution."
        })
        public HugeScreenshotMode screenshotMode = HugeScreenshotMode.RESIZE;
    }

    public static class PanoramicScreenshots implements ConfigCore {
        @Config(description = {
                "Sets the width and height of panoramic screenshot tiles as a power of two.",
                "For example, 10 produces 1024x1024 tiles (2^10)."
        })
        @Config.IntRange(min = 1)
        public int tileResolutionScale = 10;
    }
}
