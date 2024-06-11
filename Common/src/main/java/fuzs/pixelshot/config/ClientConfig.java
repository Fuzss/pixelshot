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
		@Config(description = "Hide the in-game gui in orthographic camera mode, similar to Minecraft's F1 mode.")
		public boolean hideHud = true;
		@Config(description = "Hide the clouds in orthographic camera mode.")
		public boolean hideClouds = true;
	}
	
	public static class HighResolutionScreenshots implements ConfigCore {
		@Config(description = "Horizontal amount of pixels for high resolution screenshots.")
		public int viewCaptureWidth = 3840;
		@Config(description = "Vertical amount of pixels for high resolution screenshots.")
		public int viewCaptureHeight = 2160;
		@Config(description = "Hide the in-game gui when taking high resolution screenshots, similar to Minecraft's F1 mode.")
		public boolean hideHud = true;
	}
}
