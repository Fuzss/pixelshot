package fuzs.pixelshot.neoforge;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(Pixelshot.MOD_ID)
public class PixelshotNeoForge {

    public PixelshotNeoForge() {
        ModConstructor.construct(Pixelshot.MOD_ID, Pixelshot::new);
    }
}
