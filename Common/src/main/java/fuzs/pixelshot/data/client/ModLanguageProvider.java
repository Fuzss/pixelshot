package fuzs.pixelshot.data.client;

import fuzs.pixelshot.Pixelshot;
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
        builder.add(OrthoViewHandler.KEY_ZOOM, "Zoom: %s");
        builder.add(OrthoViewHandler.KEY_X_ROT, "Pitch: %s");
        builder.add(OrthoViewHandler.KEY_Y_ROT, "Yaw: %s");
        builder.add(OrthoViewHandler.KEY_TOGGLE_VIEW, "Orthographic Camera");
        builder.add(OrthoViewHandler.KEY_OPEN_MENU, "Camera Configuration");
        builder.add(OrthoViewHandler.KEY_ZOOM_IN, "Zoom Camera In");
        builder.add(OrthoViewHandler.KEY_ZOOM_OUT, "Zoom Camera Out");
        builder.add(OrthoViewHandler.KEY_ROTATE_UP, "Rotate Camera Up");
        builder.add(OrthoViewHandler.KEY_ROTATE_DOWN, "Rotate Camera Down");
        builder.add(OrthoViewHandler.KEY_ROTATE_LEFT, "Rotate Camera Left");
        builder.add(OrthoViewHandler.KEY_ROTATE_RIGHT, "Rotate Camera Right");
        builder.add(OrthoViewHandler.KEY_SWITCH_PRESET, "Switch Camera Preset");
    }
}
