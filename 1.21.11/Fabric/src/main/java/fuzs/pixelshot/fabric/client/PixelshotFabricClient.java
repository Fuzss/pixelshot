package fuzs.pixelshot.fabric.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.PixelshotClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class PixelshotFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(Pixelshot.MOD_ID, PixelshotClient::new);
    }
}
