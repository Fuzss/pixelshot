package fuzs.pixelshot.client.gui.screens;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditBoxCameraScreen extends AbstractCameraScreen {

    public EditBoxCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.EDIT_BOX, title, handler);
    }
}
