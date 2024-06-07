package fuzs.pixelshot.fabric.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.PixelshotClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.renderer.ComputeFieldOfViewCallback;
import fuzs.puzzleslib.fabric.api.client.event.v1.FabricRendererEvents;
import fuzs.puzzleslib.fabric.api.event.v1.core.FabricEventInvokerRegistry;
import net.fabricmc.api.ClientModInitializer;

public class PixelshotFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(Pixelshot.MOD_ID, PixelshotClient::new);
        FabricEventInvokerRegistry.INSTANCE.register(ComputeFieldOfViewCallback.class, FabricRendererEvents.COMPUTE_FIELD_OF_VIEW);
    }
}
