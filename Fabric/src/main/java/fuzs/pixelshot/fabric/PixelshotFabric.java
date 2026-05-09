package fuzs.pixelshot.fabric;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class PixelshotFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(Pixelshot.MOD_ID, Pixelshot::new);
    }
}
