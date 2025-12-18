package fuzs.pixelshot.neoforge.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.PixelshotClient;
import fuzs.pixelshot.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Pixelshot.MOD_ID, dist = Dist.CLIENT)
public class PixelshotNeoForgeClient {

    public PixelshotNeoForgeClient() {
        ClientModConstructor.construct(Pixelshot.MOD_ID, PixelshotClient::new);
        DataProviderHelper.registerDataProviders(Pixelshot.MOD_ID, ModLanguageProvider::new);
    }
}
