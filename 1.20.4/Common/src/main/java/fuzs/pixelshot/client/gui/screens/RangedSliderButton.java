package fuzs.pixelshot.client.gui.screens;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class RangedSliderButton extends AbstractSliderButton {
    protected final double minValue;
    protected final double maxValue;

    public RangedSliderButton(int x, int y, int width, int height, double value, double minValue, double maxValue) {
        super(x, y, width, height, CommonComponents.EMPTY, 0.0);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setValue(value);
    }

    public double getValue() {
        return this.getScaledValue() * (this.maxValue - this.minValue) + this.minValue;
    }

    public void setValue(double value) {
        this.setScaledValue((value - this.minValue) / (this.maxValue - this.minValue));
    }

    public double getScaledValue() {
        return this.value;
    }

    public void setScaledValue(double value) {
        // TODO make this an overload for AbstractSliderButton::setValue with access widener
        double d = this.value;
        this.value = Mth.clamp(value, 0.0, 1.0);
        if (d != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.getMessageFromValue(this.getValue()));
    }

    @Override
    protected void applyValue() {
        this.applyValue(this.getValue());
    }

    protected abstract Component getMessageFromValue(double value);

    protected abstract void applyValue(double value);
}
