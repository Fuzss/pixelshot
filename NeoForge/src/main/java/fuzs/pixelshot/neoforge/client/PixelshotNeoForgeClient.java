package fuzs.pixelshot.neoforge.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.PixelshotClient;
import fuzs.pixelshot.data.client.ModLanguageProvider;
import fuzs.pixelshot.handler.OrthoViewHandlerV2;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.minecraft.client.Camera;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

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
            if (OrthoViewHandlerV2.getInstance().isActive() && OrthoViewHandlerV2.getInstance().renderPlayerEntity()) {
                try {
                    Field field = Camera.class.getDeclaredField("detached");
                    field.setAccessible(true);
                    MethodHandles.lookup().unreflectSetter(field).invoke(evt.getCamera(), true);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
