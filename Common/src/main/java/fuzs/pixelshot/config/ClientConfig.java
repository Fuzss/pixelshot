package fuzs.pixelshot.config;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config
    public final OrthographicCamera orthographicCamera = new OrthographicCamera();
    @Config
    public final HighResolutionScreenshots highResolutionScreenshots = new HighResolutionScreenshots();

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
    }

    public static class HighResolutionScreenshots implements ConfigCore {
        @Config(
                description = {
                        "Horizontal amount of pixels for high resolution screenshots.",
                        "Largest allowed value depends on current system and will crash when unsupported. If that happens enable tiled rendering."
                }
        )
        @Config.IntRange(min = 1)
        public int imageWidth = 3840;
        @Config(
                description = {
                        "Vertical amount of pixels for high resolution screenshots.",
                        "Largest allowed value depends on current system and will crash when unsupported. If that happens enable tiled rendering."
                }
        )
        @Config.IntRange(min = 1)
        public int imageHeight = 2160;
//        @Config(
//                description = {
//                        "Alternative high resolution screenshot rendering method, will halt the game while taking the screenshot. Supports higher resolutions than the default rendering.",
//                        "Rarely causes an unexpected indefinite client freeze while capturing. Use with caution."
//                }
//        )
        public boolean tiledRendering = false;
        @Config(description = "Image width and height for panoramic screenshot tiles. Should ideally be two to the power of X.")
        @Config.IntRange(min = 1)
        public int panoramicResolution = 1024;
    }
}
