package fuzs.pixelshot.neoforge.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.PixelshotClient;
import fuzs.pixelshot.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import nl.pascalroeleven.minecraft.mineshotrevived.Mineshot;

@Mod.EventBusSubscriber(modid = Pixelshot.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PixelshotNeoForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(Pixelshot.MOD_ID, PixelshotClient::new);
        DataProviderHelper.registerDataProviders(Pixelshot.MOD_ID, ModLanguageProvider::new);
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        NeoForge.EVENT_BUS.addListener((final ViewportEvent.ComputeFov evt) -> {
            if (Mineshot.getOrthoViewHandler().enabled) {
                evt.setFOV(360.0F);
            }
        });
    }
}
