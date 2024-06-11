package fuzs.pixelshot.client.gui.screens;

import fuzs.pixelshot.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class EditBoxCameraScreen extends AbstractCameraScreen {
    static final String VALID_NUMBER_PATTERN = "[\\d+\\-.]*";
    static final MutableComponent COMPONENT_ZOOM = Component.translatable(OrthoOverlayHandler.KEY_ZOOM, "");
    static final MutableComponent COMPONENT_X_ROT = Component.translatable(OrthoOverlayHandler.KEY_X_ROTATION, "");
    static final MutableComponent COMPONENT_Y_ROT = Component.translatable(OrthoOverlayHandler.KEY_Y_ROTATION, "");

    public EditBoxCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.EDIT_BOX, title, handler);
    }

    @Override
    protected void init() {
        super.init();

        int maxWidth = Math.max(this.font.width(COMPONENT_Y_ROT),
                Math.max(this.font.width(COMPONENT_ZOOM), this.font.width(COMPONENT_X_ROT))
        );
        this.addRenderableWidget(new StringWidget(this.width / 2 - 154 + 67 - maxWidth / 2,
                this.height / 6 + 25,
                maxWidth,
                9,
                COMPONENT_ZOOM,
                this.font
        ));
        this.addRenderableWidget(new StringWidget(this.width / 2 - 154 + 67 - maxWidth / 2,
                this.height / 6 + 50,
                maxWidth,
                9,
                COMPONENT_X_ROT,
                this.font
        ));
        this.addRenderableWidget(new StringWidget(this.width / 2 - 154 + 67 - maxWidth / 2,
                this.height / 6 + 75,
                maxWidth,
                9,
                COMPONENT_Y_ROT,
                this.font
        ));

        EditBox textZoom = this.addRenderableWidget(new EditBox(this.font,
                this.width / 2 - 20,
                this.height / 6 + 20,
                150,
                20,
                GameNarrator.NO_TITLE
        ));
        EditBox textXRot = this.addRenderableWidget(new EditBox(this.font,
                this.width / 2 - 20,
                this.height / 6 + 45,
                150,
                20,
                GameNarrator.NO_TITLE
        ));
        EditBox textYRot = this.addRenderableWidget(new EditBox(this.font,
                this.width / 2 - 20,
                this.height / 6 + 70,
                150,
                20,
                GameNarrator.NO_TITLE
        ));
        textZoom.setFilter(string -> {
            return string.matches(VALID_NUMBER_PATTERN);
        });
        textXRot.setFilter(string -> {
            return string.matches(VALID_NUMBER_PATTERN);
        });
        textYRot.setFilter(string -> {
            return string.matches(VALID_NUMBER_PATTERN);
        });
        textZoom.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getZoom())));
        textXRot.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getXRot())));
        textYRot.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getYRot())));
        textZoom.setResponder(string -> {
            try {
                this.handler.setZoom(Float.parseFloat(string));
            } catch (NumberFormatException ignored) {
                // NO-OP
            }
        });
        textXRot.setResponder(string -> {
            try {
                this.handler.setXRot(Float.parseFloat(string));
            } catch (NumberFormatException ignored) {
                // NO-OP
            }
        });
        textYRot.setResponder(string -> {
            try {
                this.handler.setYRot(Float.parseFloat(string));
            } catch (NumberFormatException ignored) {
                // NO-OP
            }
        });

        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134, this.height / 6 + 20,
                20,
                10,
                0,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setZoom(this.handler.getZoom() + getCurrentIncrement());
                    textZoom.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getZoom())));
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(positiveTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134, this.height / 6 + 30,
                20,
                10,
                20,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setZoom(this.handler.getZoom() - getCurrentIncrement());
                    textZoom.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getZoom())));
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(negativeTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134, this.height / 6 + 45,
                20,
                10,
                0,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setXRot(this.handler.getXRot() + getCurrentIncrement());
                    textXRot.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getXRot())));
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(positiveTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134, this.height / 6 + 55,
                20,
                10,
                20,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setXRot(this.handler.getXRot() - getCurrentIncrement());
                    textXRot.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getXRot())));
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(negativeTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134, this.height / 6 + 70,
                20,
                10,
                0,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setYRot(this.handler.getYRot() + getCurrentIncrement());
                    textYRot.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getYRot())));
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(positiveTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134, this.height / 6 + 80,
                20,
                10,
                20,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setYRot(this.handler.getYRot() - getCurrentIncrement());
                    textYRot.setValue(String.valueOf(OrthoViewHandler.roundValue(this.handler.getYRot())));
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(negativeTooltip());
    }
}
