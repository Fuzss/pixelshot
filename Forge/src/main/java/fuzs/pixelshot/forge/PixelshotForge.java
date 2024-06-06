package fuzs.pixelshot.forge;

import fuzs.pixelshot.Pixelshot;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(Pixelshot.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PixelshotForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(Pixelshot.MOD_ID, Pixelshot::new);
    }
}
