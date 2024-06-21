package fuzs.pixelshot.client;

import fuzs.pixelshot.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.pixelshot.client.handler.ScreenshotHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.InputEvents;
import fuzs.puzzleslib.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.ComputeFieldOfViewCallback;
import fuzs.puzzleslib.api.client.event.v1.renderer.FogEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.GameRenderEvents;

public class PixelshotClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientTickEvents.START.register(OrthoViewHandler.INSTANCE::onStartClientTick);
        GameRenderEvents.BEFORE.register(OrthoViewHandler.INSTANCE::onBeforeGameRender);
        GameRenderEvents.AFTER.register(OrthoViewHandler.INSTANCE::onAfterGameRender);
        ComputeFieldOfViewCallback.EVENT.register(OrthoViewHandler.INSTANCE::onComputeFieldOfView);
        FogEvents.RENDER.register(OrthoViewHandler.INSTANCE::onRenderFog);
        ClientPlayerNetworkEvents.LOGGED_IN.register(OrthoViewHandler.INSTANCE::onLoggedIn);
        ClientTickEvents.START.register(OrthoOverlayHandler.INSTANCE::onStartClientTick);
        GameRenderEvents.AFTER.register(OrthoOverlayHandler.INSTANCE::onAfterGameRender);
        InputEvents.AFTER_KEY_ACTION.register(ScreenshotHandler.INSTANCE::onAfterKeyAction);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        OrthoViewHandler.onRegisterKeyMappings(context);
        ScreenshotHandler.onRegisterKeyMappings(context);
    }
}
