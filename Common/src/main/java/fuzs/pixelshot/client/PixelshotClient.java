package fuzs.pixelshot.client;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.pixelshot.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.pixelshot.client.handler.ScreenshotHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public class PixelshotClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientTickEvents.START.register(OrthoViewHandler.INSTANCE::onStartClientTick);
        GameRenderEvents.BEFORE.register(OrthoViewHandler.INSTANCE::onBeforeGameRender);
        GameRenderEvents.AFTER.register(OrthoViewHandler.INSTANCE::onAfterGameRender);
        ComputeCameraAnglesCallback.EVENT.register();
        FogEvents.RENDER.register(OrthoViewHandler.INSTANCE::onRenderFog);
        ClientPlayerEvents.LOGGED_IN.register(OrthoViewHandler.INSTANCE::onLoggedIn);
        ClientTickEvents.START.register(OrthoOverlayHandler.INSTANCE::onStartClientTick);
        GameRenderEvents.AFTER.register(OrthoOverlayHandler.INSTANCE::onAfterGameRender);
        InputEvents.AFTER_KEY_ACTION.register(ScreenshotHandler.INSTANCE::onAfterKeyAction);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        OrthoViewHandler.onRegisterKeyMappings(context);
        ScreenshotHandler.onRegisterKeyMappings(context);
    }

    @Deprecated
    public static KeyMapping registerUnboundKeyMapping(ResourceLocation identifier) {
        return registerKeyMapping(identifier, InputConstants.UNKNOWN.getValue());
    }

    @Deprecated
    public static KeyMapping registerKeyMapping(ResourceLocation identifier, int keyCode) {
        return new KeyMapping(identifier.toLanguageKey("key"),
                keyCode,
                identifier.withPath("main").toLanguageKey("key.category"));
    }
}
