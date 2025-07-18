package fuzs.pixelshot.neoforge;

import fuzs.pixelshot.Pixelshot;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(Pixelshot.MOD_ID)
public class PixelshotNeoForge {

    public PixelshotNeoForge() {
        ModConstructor.construct(Pixelshot.MOD_ID, Pixelshot::new);
    }
}
