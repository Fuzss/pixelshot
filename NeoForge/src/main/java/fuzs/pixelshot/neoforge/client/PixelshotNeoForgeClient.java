package fuzs.pixelshot.neoforge.client;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.PixelshotClient;
import fuzs.pixelshot.common.data.client.ModLanguageProvider;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v3.core.DataProviderBuilder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Pixelshot.MOD_ID, dist = Dist.CLIENT)
public class PixelshotNeoForgeClient {

    public PixelshotNeoForgeClient() {
        ClientModConstructor.construct(Pixelshot.MOD_ID, PixelshotClient::new);
        DataProviderBuilder.of(Pixelshot.MOD_ID).addProvider(ModLanguageProvider::new);
    }
}
