package fuzs.pixelshot.client.gui.screens;

import fuzs.pixelshot.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import net.minecraft.network.chat.Component;

public class SliderCameraScreen extends AbstractCameraScreen {

    public SliderCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.SLIDER, title, handler);
    }

    @Override
    protected void init() {
        super.init();

        RangedSliderButton textZoom = this.addRenderableWidget(new RangedSliderButton(this.width / 2 - 130,
                this.height / 6 + 20,
                260,
                20,
                this.handler.getZoom(),
                OrthoViewHandler.ZOOM_MIN,
                OrthoViewHandler.ZOOM_MAX
        ) {
            static final double SCALE = 4.0;
            static final double SCALE_POW = Math.pow(10.0, -SCALE);

            @Override
            public double getValue() {
                double value = Math.pow(10.0, this.getScaledValue() * SCALE - SCALE) - SCALE_POW;
                return value * (this.maxValue - this.minValue) + this.minValue;
            }

            @Override
            public void setValue(double value) {
                value = (value - this.minValue) / (this.maxValue - this.minValue);
                this.setScaledValue((Math.log10(value + SCALE_POW) + SCALE) / SCALE);
            }

            @Override
            protected Component getMessageFromValue(double value) {
                return Component.translatable(OrthoOverlayHandler.KEY_ZOOM, OrthoViewHandler.roundValue((float) value));
            }

            @Override
            protected void applyValue(double value) {
                SliderCameraScreen.this.handler.setZoom((float) value);
            }
        });

        RangedSliderButton textXRot = this.addRenderableWidget(new RangedSliderButton(this.width / 2 - 130,
                this.height / 6 + 45,
                260,
                20,
                this.handler.getXRot(),
                OrthoViewHandler.X_ROTATION_MIN,
                OrthoViewHandler.X_ROTATION_MAX
        ) {

            @Override
            protected Component getMessageFromValue(double value) {
                return Component.translatable(OrthoOverlayHandler.KEY_X_ROTATION, OrthoViewHandler.roundValue((float) value));
            }

            @Override
            protected void applyValue(double value) {
                SliderCameraScreen.this.handler.setXRot((float) value);
            }
        });

        RangedSliderButton textYRot = this.addRenderableWidget(new RangedSliderButton(this.width / 2 - 130,
                this.height / 6 + 70,
                260,
                20,
                this.handler.getYRot(),
                OrthoViewHandler.Y_ROTATION_MIN,
                OrthoViewHandler.Y_ROTATION_MAX
        ) {

            @Override
            protected Component getMessageFromValue(double value) {
                return Component.translatable(OrthoOverlayHandler.KEY_Y_ROTATION, OrthoViewHandler.roundValue((float) value));
            }

            @Override
            protected void applyValue(double value) {
                SliderCameraScreen.this.handler.setYRot((float) value);
            }
        });

        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134,
                this.height / 6 + 20,
                20,
                20,
                60,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setZoom(this.handler.getZoom() + getCurrentIncrement());
                    textZoom.setValue(this.handler.getZoom());
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(positiveTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 - 154,
                this.height / 6 + 20,
                20,
                20,
                40,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setZoom(this.handler.getZoom() - getCurrentIncrement());
                    textZoom.setValue(this.handler.getZoom());
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(negativeTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134,
                this.height / 6 + 45,
                20,
                20,
                60,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setXRot(this.handler.getXRot() + getCurrentIncrement());
                    textXRot.setValue(this.handler.getXRot());
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(positiveTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 - 154,
                this.height / 6 + 45,
                20,
                20,
                40,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setXRot(this.handler.getXRot() - getCurrentIncrement());
                    textXRot.setValue(this.handler.getXRot());
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(negativeTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 + 134,
                this.height / 6 + 70,
                20,
                20,
                60,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setYRot(this.handler.getYRot() + getCurrentIncrement());
                    textYRot.setValue(this.handler.getYRot());
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(positiveTooltip());
        this.addRenderableWidget(new SpritelessImageButton(this.width / 2 - 154,
                this.height / 6 + 70,
                20,
                20,
                40,
                0,
                WIDGETS_LOCATION,
                button -> {
                    this.handler.setYRot(this.handler.getYRot() - getCurrentIncrement());
                    textYRot.setValue(this.handler.getYRot());
                }
        )).setDrawBackground().setTextureLayout(SINGLE_TEXTURE_LAYOUT).setTooltip(negativeTooltip());
    }
}
