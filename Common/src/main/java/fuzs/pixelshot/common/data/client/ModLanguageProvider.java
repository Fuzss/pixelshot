package fuzs.pixelshot.common.data.client;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.gui.screens.AbstractCameraScreen;
import fuzs.pixelshot.common.client.handler.ScreenshotHandler;
import fuzs.pixelshot.common.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.common.api.client.data.v3.language.AbstractLanguageProvider;
import fuzs.puzzleslib.common.api.data.v3.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations() {
        this.addKeyCategory(Pixelshot.MOD_ID, Pixelshot.MOD_NAME);
        this.add(OrthoOverlayHandler.KEY_ZOOM, "Zoom: %s");
        this.add(OrthoOverlayHandler.KEY_X_ROTATION, "Pitch: %s");
        this.add(OrthoOverlayHandler.KEY_Y_ROTATION, "Yaw: %s");
        this.add(OrthoViewHandler.KEY_TOGGLE_VIEW, "Toggle Orthographic Camera");
        this.add(OrthoViewHandler.KEY_OPEN_MENU, "Open Camera Configuration");
        this.add(OrthoViewHandler.KEY_ZOOM_IN, "Zoom Camera In");
        this.add(OrthoViewHandler.KEY_ZOOM_OUT, "Zoom Camera Out");
        this.add(OrthoViewHandler.KEY_ROTATE_UP, "Rotate Camera Up");
        this.add(OrthoViewHandler.KEY_ROTATE_DOWN, "Rotate Camera Down");
        this.add(OrthoViewHandler.KEY_ROTATE_LEFT, "Rotate Camera Left");
        this.add(OrthoViewHandler.KEY_ROTATE_RIGHT, "Rotate Camera Right");
        this.add(OrthoViewHandler.KEY_SWITCH_PRESET, "Switch Camera Preset");
        this.add(AbstractCameraScreen.COMPONENT_TITLE, "Orthographic Camera");
        this.add(AbstractCameraScreen.KEY_FOLLOW_VIEW, "Follow View: %s");
        this.add(AbstractCameraScreen.KEY_NEAR_CLIPPING, "Near Clipping: %s");
        this.add(AbstractCameraScreen.KEY_RENDER_SKY, "Render Sky: %s");
        this.add(AbstractCameraScreen.KEY_RENDER_PLAYER, "Render Player: %s");
        this.add(ScreenshotHandler.KEY_HIGH_RESOLUTION_SCREENSHOT, "Take High Resolution Screenshot");
        this.add(ScreenshotHandler.KEY_PANORAMIC_SCREENSHOT, "Take Panoramic Screenshot");
        this.add(ScreenshotHandler.COMPONENT_SCREENSHOT_TAKE, "Taking screenshot...");
    }
}
