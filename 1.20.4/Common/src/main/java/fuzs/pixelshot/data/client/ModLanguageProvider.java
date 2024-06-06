package fuzs.pixelshot.data.client;

import fuzs.pixelshot.Pixelshot;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.addKeyCategory(Pixelshot.MOD_ID, Pixelshot.MOD_NAME);
    }
}
