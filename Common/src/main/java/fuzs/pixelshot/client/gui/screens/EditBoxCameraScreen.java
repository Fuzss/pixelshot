package fuzs.pixelshot.client.gui.screens;

import fuzs.pixelshot.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EditBoxCameraScreen extends AbstractCameraScreen {
    static final String VALID_NUMBER_PATTERN = "[\\d+\\-.]*";
    static final MutableComponent COMPONENT_ZOOM = Component.translatable(OrthoOverlayHandler.KEY_ZOOM, "");
    static final MutableComponent COMPONENT_X_ROT = Component.translatable(OrthoOverlayHandler.KEY_X_ROTATION, "");
    static final MutableComponent COMPONENT_Y_ROT = Component.translatable(OrthoOverlayHandler.KEY_Y_ROTATION, "");

    public EditBoxCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.EDIT_BOX, title, handler);
    }

    @Override
    void addControlRow(OrthoComponent component, int rowHeight, Collection<AbstractWidget> widgets) {
        super.addControlRow(component, rowHeight, widgets);
        Consumer<Float> consumer = (Float value) -> component.consumer.accept(this.handler, value);
        Supplier<Float> supplier = () -> component.supplier.apply(this.handler);
        int maxWidth = getMaxComponentWidth(this.font);
        widgets.add(new StringWidget(this.width / 2 - 154 + 67 - maxWidth / 2,
                rowHeight + 5,
                maxWidth,
                this.font.lineHeight,
                component.component,
                this.font
        ));
        EditBox editBox = new EditBox(this.font,
                this.width / 2 - 20,
                rowHeight,
                150,
                20,
                GameNarrator.NO_TITLE
        );
        editBox.setFilter((String string) -> {
            return string.matches(VALID_NUMBER_PATTERN);
        });
        editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(supplier.get())));
        editBox.setResponder((String string) -> {
            try {
                consumer.accept(Float.parseFloat(string));
            } catch (NumberFormatException ignored) {
                // NO-OP
            }
        });
        widgets.add(editBox);
        SpritelessImageButton plusButton = new SpritelessImageButton(this.width / 2 + 134,
                rowHeight,
                20,
                10,
                0,
                0,
                WIDGETS_LOCATION,
                (Button button) -> {
                    consumer.accept(supplier.get() + getCurrentIncrement());
                    editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(supplier.get())));
                }
        ).setDrawBackground().setTextureLayout(SpritelessImageButton.SINGLE_TEXTURE_LAYOUT);
        plusButton.setTooltip(new DynamicTooltip(plusButton, '+'));
        widgets.add(plusButton);
        SpritelessImageButton minusButton = new SpritelessImageButton(this.width / 2 + 134,
                rowHeight + 10,
                20,
                10,
                20,
                0,
                WIDGETS_LOCATION,
                (Button button) -> {
                    consumer.accept(supplier.get() - getCurrentIncrement());
                    editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(supplier.get())));
                }
        ).setDrawBackground().setTextureLayout(SpritelessImageButton.SINGLE_TEXTURE_LAYOUT);
        minusButton.setTooltip(new DynamicTooltip(minusButton, '-'));
        widgets.add(minusButton);
        widgets.add(this.getResetButton(rowHeight, () -> {
            consumer.accept(component.getDefaultValue());
            editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(component.getDefaultValue())));
        }));
    }

    private static int getMaxComponentWidth(Font font) {
        return Math.max(font.width(COMPONENT_Y_ROT),
                Math.max(font.width(COMPONENT_ZOOM), font.width(COMPONENT_X_ROT))
        );
    }
}
