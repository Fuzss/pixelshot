package fuzs.pixelshot.common.client.gui.screens;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.common.api.client.gui.v2.components.RangedSliderButton;
import fuzs.puzzleslib.common.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderCameraScreen extends AbstractCameraScreen {
    public static final WidgetSprites ADD_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id("widget/add_button"),
            Pixelshot.id("widget/add_button_disabled"),
            Pixelshot.id("widget/add_button_highlighted"));
    public static final WidgetSprites SUBTRACT_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id("widget/subtract_button"),
            Pixelshot.id("widget/subtract_button_disabled"),
            Pixelshot.id("widget/subtract_button_highlighted"));

    public SliderCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.SLIDER, title, handler);
    }

    @Override
    protected void addControlRow(OrthoComponent component, int rowHeight, Collection<AbstractWidget> widgets) {
        super.addControlRow(component, rowHeight, widgets);
        Consumer<Float> consumer = (Float value) -> component.consumer.accept(this.handler, value);
        Supplier<Float> supplier = () -> component.supplier.apply(this.handler);
        var sliderButton = new RangedSliderButton(this.width / 2 - 130,
                rowHeight,
                260,
                20,
                supplier.get(),
                component.minValue,
                component.maxValue) {
            static final double LOGARITHMIC_SCALE = 4.0;
            static final double LOGARITHMIC_SCALE_POW = Math.pow(10.0, -LOGARITHMIC_SCALE);

            @Override
            public double getAbsoluteValue() {
                if (component.supportsLogarithmicScale()) {
                    double value = Math.pow(10.0, this.getRelativeValue() * LOGARITHMIC_SCALE - LOGARITHMIC_SCALE)
                            - LOGARITHMIC_SCALE_POW;
                    return Mth.lerp(value, this.minValue, this.maxValue);
                } else {
                    return super.getAbsoluteValue();
                }
            }

            @Override
            public void setAbsoluteValue(double value) {
                if (component.supportsLogarithmicScale()) {
                    value = Mth.inverseLerp(value, this.minValue, this.maxValue);
                    this.setRelativeValue(
                            (Math.log10(value + LOGARITHMIC_SCALE_POW) + LOGARITHMIC_SCALE) / LOGARITHMIC_SCALE);
                } else {
                    super.setAbsoluteValue(value);
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
        AbstractWidget addWidget = new ImageButton(this.width / 2 + 134,
                rowHeight,
                20,
                20,
                ADD_BUTTON_SPRITES,
                (Button button) -> {
                    consumer.accept(supplier.get() + getCurrentIncrement());
                    sliderButton.setAbsoluteValue(supplier.get());
                });
        TooltipBuilder.create().setLines(getCurrentTooltipLines('+')).build(addWidget);
        widgets.add(addWidget);
        AbstractWidget subtractWidget = new ImageButton(this.width / 2 - 154,
                rowHeight,
                20,
                20,
                SUBTRACT_BUTTON_SPRITES,
                (Button button) -> {
                    consumer.accept(supplier.get() - getCurrentIncrement());
                    sliderButton.setAbsoluteValue(supplier.get());
                });
        TooltipBuilder.create().setLines(getCurrentTooltipLines('-')).build(subtractWidget);
        widgets.add(subtractWidget);
        widgets.add(this.createResetButton(rowHeight, () -> {
            consumer.accept(component.getDefaultValue());
            sliderButton.setAbsoluteValue(component.getDefaultValue());
        }));
    }
}
