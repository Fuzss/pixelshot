package fuzs.pixelshot.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.InputEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.ComputeCameraAnglesCallback;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;
import nl.pascalroeleven.minecraft.mineshotrevived.Mineshot;
import nl.pascalroeleven.minecraft.mineshotrevived.client.OrthoViewHandler;
import nl.pascalroeleven.minecraft.mineshotrevived.client.ScreenshotHandler;

public class PixelshotClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientTickEvents.START.register(minecraft -> Mineshot.getOrthoViewHandler().onClientTickEvent());
        InputEvents.AFTER_KEY_ACTION.register(Mineshot.getOrthoViewHandler()::onKeyEvent);
        InputEvents.AFTER_KEY_ACTION.register(Mineshot.getScreenshotHandler()::onKeyEvent);
        GameRenderEvents.BEFORE.register(Mineshot.getScreenshotHandler()::onBeforeGameRender);
        ComputeCameraAnglesCallback.EVENT.register(Mineshot.getOrthoViewHandler()::onComputeCameraAngles);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        OrthoViewHandler.onRegisterKeyMappings(context);
        ScreenshotHandler.onRegisterKeyMappings(context);
    }
}
