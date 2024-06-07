package fuzs.pixelshot.data.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.handler.OrthoViewHandlerV2;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.addKeyCategory(Pixelshot.MOD_ID, Pixelshot.MOD_NAME);
        builder.add(OrthoViewHandlerV2.KEY_ZOOM, "Zoom: %s");
        builder.add(OrthoViewHandlerV2.KEY_X_ROT, "X-Rotation: %s");
        builder.add(OrthoViewHandlerV2.KEY_Y_ROT, "Y-Rotation: %s");
        builder.add(OrthoViewHandlerV2.KEY_TOGGLE_VIEW, "Orthographic Camera");
        builder.add(OrthoViewHandlerV2.KEY_OPEN_MENU, "Camera Configuration");
        builder.add(OrthoViewHandlerV2.KEY_ZOOM_IN, "Zoom Camera In");
        builder.add(OrthoViewHandlerV2.KEY_ZOOM_OUT, "Zoom Camera Out");
        builder.add(OrthoViewHandlerV2.KEY_ROTATE_UP, "Rotate Camera Up");
        builder.add(OrthoViewHandlerV2.KEY_ROTATE_DOWN, "Rotate Camera Down");
        builder.add(OrthoViewHandlerV2.KEY_ROTATE_LEFT, "Rotate Camera Left");
        builder.add(OrthoViewHandlerV2.KEY_ROTATE_RIGHT, "Rotate Camera Right");
        builder.add(OrthoViewHandlerV2.KEY_SWITCH_PRESET, "Switch Camera Preset");
    }
}
