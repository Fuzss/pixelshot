package fuzs.pixelshot.client.gui.screens;

import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.client.gui.v2.components.RangedSliderButton;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.TooltipBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderCameraScreen extends AbstractCameraScreen {

    public SliderCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.SLIDER, title, handler);
    }

    @Override
    void addControlRow(OrthoComponent component, int rowHeight, Collection<AbstractWidget> widgets) {
        super.addControlRow(component, rowHeight, widgets);
        Consumer<Float> consumer = (Float value) -> component.consumer.accept(this.handler, value);
        Supplier<Float> supplier = () -> component.supplier.apply(this.handler);
        RangedSliderButton sliderButton = new RangedSliderButton(this.width / 2 - 130,
                rowHeight,
                260,
                20,
                supplier.get(),
                component.minValue,
                component.maxValue
        ) {
            static final double LOGARITHMIC_SCALE = 4.0;
            static final double LOGARITHMIC_SCALE_POW = Math.pow(10.0, -LOGARITHMIC_SCALE);

            @Override
            public double getScaledValue() {
                if (component.supportsLogarithmicScale()) {
                    double value = Math.pow(10.0, this.getValue() * LOGARITHMIC_SCALE - LOGARITHMIC_SCALE) -
                            LOGARITHMIC_SCALE_POW;
                    return value * (this.maxValue - this.minValue) + this.minValue;
                } else {
                    return super.getScaledValue();
                }
            }

            @Override
            public void setScaledValue(double value) {
                if (component.supportsLogarithmicScale()) {
                    value = (value - this.minValue) / (this.maxValue - this.minValue);
                    this.setValue((Math.log10(value + LOGARITHMIC_SCALE_POW) + LOGARITHMIC_SCALE) / LOGARITHMIC_SCALE);
                } else {
                    super.setScaledValue(value);
                }
            }

            @Override
            protected Component getMessageFromValue(double value) {
                return Component.translatable(component.translationKey, OrthoViewHandler.roundValue((float) value));
            }

            @Override
            protected void applyValue(double value) {
                consumer.accept((float) value);
            }
        };
        widgets.add(sliderButton);
        SpritelessImageButton plusButton = new SpritelessImageButton(this.width / 2 + 134,
                rowHeight,
                20,
                20,
                60,
                0,
                WIDGETS_LOCATION,
                (Button button) -> {
                    consumer.accept(supplier.get() + getCurrentIncrement());
                    sliderButton.setScaledValue(supplier.get());
                }
        ).setDrawBackground().setTextureLayout(SpritelessImageButton.SINGLE_TEXTURE_LAYOUT);
        TooltipBuilder.create().setLines(getCurrentTooltipLines('+')).build(plusButton);
        widgets.add(plusButton);
        SpritelessImageButton minusButton = new SpritelessImageButton(this.width / 2 - 154,
                rowHeight,
                20,
                20,
                40,
                0,
                WIDGETS_LOCATION,
                (Button button) -> {
                    consumer.accept(supplier.get() - getCurrentIncrement());
                    sliderButton.setScaledValue(supplier.get());
                }
        ).setDrawBackground().setTextureLayout(SpritelessImageButton.SINGLE_TEXTURE_LAYOUT);
        TooltipBuilder.create().setLines(getCurrentTooltipLines('-')).build(minusButton);
        widgets.add(minusButton);
        widgets.add(this.getResetButton(rowHeight, () -> {
            consumer.accept(component.getDefaultValue());
            sliderButton.setScaledValue(component.getDefaultValue());
        }));
    }
}
