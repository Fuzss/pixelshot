package fuzs.pixelshot.data.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.gui.screens.AbstractCameraScreen;
import fuzs.pixelshot.client.handler.ScreenshotHandler;
import fuzs.pixelshot.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.addKeyCategory(Pixelshot.MOD_ID, Pixelshot.MOD_NAME);
        builder.add(OrthoOverlayHandler.KEY_ZOOM, "Zoom: %s");
        builder.add(OrthoOverlayHandler.KEY_X_ROTATION, "Pitch: %s");
        builder.add(OrthoOverlayHandler.KEY_Y_ROTATION, "Yaw: %s");
        builder.add(OrthoViewHandler.KEY_TOGGLE_VIEW, "Toggle Orthographic Camera");
        builder.add(OrthoViewHandler.KEY_OPEN_MENU, "Open Camera Configuration");
        builder.add(OrthoViewHandler.KEY_ZOOM_IN, "Zoom Camera In");
        builder.add(OrthoViewHandler.KEY_ZOOM_OUT, "Zoom Camera Out");
        builder.add(OrthoViewHandler.KEY_ROTATE_UP, "Rotate Camera Up");
        builder.add(OrthoViewHandler.KEY_ROTATE_DOWN, "Rotate Camera Down");
        builder.add(OrthoViewHandler.KEY_ROTATE_LEFT, "Rotate Camera Left");
        builder.add(OrthoViewHandler.KEY_ROTATE_RIGHT, "Rotate Camera Right");
        builder.add(OrthoViewHandler.KEY_SWITCH_PRESET, "Switch Camera Preset");
        builder.add(AbstractCameraScreen.COMPONENT_TITLE, "Orthographic Camera");
        builder.add(AbstractCameraScreen.KEY_FOLLOW_VIEW, "Follow View: %s");
        builder.add(AbstractCameraScreen.KEY_NEAR_CLIPPING, "Near Clipping: %s");
        builder.add(AbstractCameraScreen.KEY_RENDER_SKY, "Render Sky: %s");
        builder.add(AbstractCameraScreen.KEY_RENDER_PLAYER, "Render Player: %s");
        builder.add(ScreenshotHandler.KEY_HIGH_RESOLUTION_SCREENSHOT, "Take High Resolution Screenshot");
        builder.add(ScreenshotHandler.KEY_PANORAMIC_SCREENSHOT, "Take Panoramic Screenshot");
        builder.add(ScreenshotHandler.COMPONENT_SCREENSHOT_TAKE, "Taking screenshot...");
    }
}
