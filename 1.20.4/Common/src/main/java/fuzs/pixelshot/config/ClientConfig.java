package fuzs.pixelshot.config;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config(category = "high_resolution_screenshots", description = "Horizontal amount of pixels for high resolution screenshots.")
    public int viewCaptureWidth = 3840;
	@Config(category = "high_resolution_screenshots", description = "Vertical amount of pixels for high resolution screenshots.")
    public int viewCaptureHeight = 2160;
	@Config(name = "hide_hud", category = "high_resolution_screenshots", description = "Hide the in-game gui when taking high resolution screenshots, similar to Minecraft's F1 mode.")
	public boolean hideHudForHugeScreenshots = true;
	@Config(category = "orthographic_camera", description = "Preset rotation in degrees when opening the orthographic camera for the horizontal axis.")
	@Config.IntRange(min = 0, max = 360)
    public int defaultXRotation = OrthoViewHandler.X_ROT_DEFAULT;
	@Config(category = "orthographic_camera", description = "Preset rotation in degrees when opening the orthographic camera for the vertical axis.")
	@Config.IntRange(min = 0, max = 360)
    public int defaultYRotation = OrthoViewHandler.Y_ROT_DEFAULT;
	@Config(name = "hide_hud", category = "orthographic_camera", description = "Hide the in-game gui in orthographic camera mode, similar to Minecraft's F1 mode.")
	public boolean hideHudForOrthographicCamera = true;
	@Config(category = "orthographic_camera", description = "Hide the clouds in orthographic camera mode.")
	public boolean hideClouds = true;
}
