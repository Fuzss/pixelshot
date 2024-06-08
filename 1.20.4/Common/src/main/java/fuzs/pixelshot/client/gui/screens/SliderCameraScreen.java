package fuzs.pixelshot.client.gui.screens;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import net.minecraft.network.chat.Component;

public class SliderCameraScreen extends AbstractCameraScreen {

    public SliderCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.SLIDER, title, handler);
    }
}
