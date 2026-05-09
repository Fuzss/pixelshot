package fuzs.pixelshot.fabric.client;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.PixelshotClient;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class PixelshotFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(Pixelshot.MOD_ID, PixelshotClient::new);
    }
}
