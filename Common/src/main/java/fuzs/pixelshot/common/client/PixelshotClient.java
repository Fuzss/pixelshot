package fuzs.pixelshot.common.client;

import fuzs.pixelshot.common.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import fuzs.pixelshot.common.client.handler.ScreenshotHandler;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.event.v1.ClientInputEvents;
import fuzs.puzzleslib.common.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.common.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.common.api.client.event.v1.gui.RenderGuiEvents;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.ComputeFieldOfViewCallback;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.FogEvents;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.RenderBlockOverlayCallback;

public class PixelshotClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientTickEvents.START.register(OrthoViewHandler.INSTANCE::onStartClientTick);
        ComputeFieldOfViewCallback.EVENT.register(OrthoViewHandler.INSTANCE::onComputeFieldOfView);
        FogEvents.SETUP.register(OrthoViewHandler.INSTANCE::onSetupFog);
        ClientPlayerNetworkEvents.JOIN.register(OrthoViewHandler.INSTANCE::onPlayerJoin);
        RenderBlockOverlayCallback.EVENT.register(OrthoViewHandler.INSTANCE::onRenderBlockOverlay);
        ClientTickEvents.START.register(OrthoOverlayHandler.INSTANCE::onStartClientTick);
        RenderGuiEvents.AFTER.register(OrthoOverlayHandler.INSTANCE::onAfterRenderGui);
        ClientInputEvents.KEY_PRESS.register(ScreenshotHandler.INSTANCE::onKeyPress);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        OrthoViewHandler.onRegisterKeyMappings(context);
        ScreenshotHandler.onRegisterKeyMappings(context);
    }
}
