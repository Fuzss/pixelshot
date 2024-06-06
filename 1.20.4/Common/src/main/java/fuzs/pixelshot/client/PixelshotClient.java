package fuzs.pixelshot.client;

import fuzs.pixelshot.handler.OrthoViewHandlerV2;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.ComputeCameraAnglesCallback;
import fuzs.puzzleslib.api.client.event.v1.renderer.FogEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;

public class PixelshotClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
//        ClientTickEvents.START.register(minecraft -> Mineshot.getOrthoViewHandler().onClientTickEvent());
//        InputEvents.AFTER_KEY_ACTION.register(Mineshot.getOrthoViewHandler()::onKeyEvent);
//        InputEvents.AFTER_KEY_ACTION.register(Mineshot.getScreenshotHandler()::onKeyEvent);
//        GameRenderEvents.BEFORE.register(Mineshot.getScreenshotHandler()::onBeforeGameRender);
//        ComputeCameraAnglesCallback.EVENT.register(Mineshot.getOrthoViewHandler()::onComputeCameraAngles);
//        FogEvents.RENDER.register(Mineshot.getOrthoViewHandler()::onRenderFog);

        // new
        ClientTickEvents.START.register(OrthoViewHandlerV2.getInstance()::onStartClientTick);
        GameRenderEvents.BEFORE.register(OrthoViewHandlerV2.getInstance()::onBeforeGameRender);
        GameRenderEvents.AFTER.register(OrthoViewHandlerV2.getInstance()::onAfterGameRender);
        ComputeCameraAnglesCallback.EVENT.register(OrthoViewHandlerV2.getInstance()::onComputeCameraAngles);
        FogEvents.RENDER.register(OrthoViewHandlerV2.getInstance()::onRenderFog);
        ClientPlayerNetworkEvents.LOGGED_IN.register(OrthoViewHandlerV2.getInstance()::onLoggedIn);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
//        OrthoViewHandler.onRegisterKeyMappings(context);
//        ScreenshotHandler.onRegisterKeyMappings(context);
        OrthoViewHandlerV2.onRegisterKeyMappings(context);
    }
}
