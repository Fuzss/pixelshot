package fuzs.pixelshot.common.client.gui.screens;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.common.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EditBoxCameraScreen extends AbstractCameraScreen {
    public static final WidgetSprites ADD_SMALL_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id(
            "widget/add_small_button"),
            Pixelshot.id("widget/add_small_button_disabled"),
            Pixelshot.id("widget/add_small_button_highlighted"));
    public static final WidgetSprites SUBTRACT_SMALL_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id(
            "widget/subtract_small_button"),
            Pixelshot.id("widget/subtract_small_button_disabled"),
            Pixelshot.id("widget/subtract_small_button_highlighted"));

    private static final MutableComponent COMPONENT_ZOOM = Component.translatable(OrthoOverlayHandler.KEY_ZOOM, "");
    private static final MutableComponent COMPONENT_X_ROT = Component.translatable(OrthoOverlayHandler.KEY_X_ROTATION,
            "");
    private static final MutableComponent COMPONENT_Y_ROT = Component.translatable(OrthoOverlayHandler.KEY_Y_ROTATION,
            "");

    public EditBoxCameraScreen(Component title, OrthoViewHandler handler) {
        super(Type.EDIT_BOX, title, handler);
    }

    @Override
    protected void addControlRow(OrthoComponent component, int rowHeight, Collection<AbstractWidget> widgets) {
        super.addControlRow(component, rowHeight, widgets);
        Consumer<Float> consumer = (Float value) -> component.consumer.accept(this.handler, value);
        Supplier<Float> supplier = () -> component.supplier.apply(this.handler);
        int maxWidth = getMaxComponentWidth(this.font);
        widgets.add(new StringWidget(this.width / 2 - 154 + 67 - maxWidth / 2,
                rowHeight + 5,
                maxWidth,
                this.font.lineHeight,
                component.component,
                this.font));
        EditBox editBox = new EditBox(this.font, this.width / 2 - 20, rowHeight, 150, 20, GameNarrator.NO_TITLE) {
            @Override
            public void setValue(String value) {
                if (this.isValidInput(value)) {
                    super.setValue(value);
                }
            }

            @Override
            public void insertText(String input) {
                if (this.isValidInput(input)) {
                    super.insertText(input);
                }
            }

            private boolean isValidInput(String input) {
                return input.matches("[\\d+\\-.]*");
            }
        };
        editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(supplier.get())));
        editBox.setResponder((String string) -> {
            try {
                consumer.accept(Float.parseFloat(string));
            } catch (NumberFormatException ignored) {
                // NO-OP
            }
        });
        widgets.add(editBox);
        AbstractWidget addWidget = new ImageButton(this.width / 2 + 134,
                rowHeight,
                20,
                10,
                ADD_SMALL_BUTTON_SPRITES,
                (Button button) -> {
                    consumer.accept(supplier.get() + getCurrentIncrement());
                    editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(supplier.get())));
                });
        TooltipBuilder.create().setLines(getCurrentTooltipLines('+')).build(addWidget);
        widgets.add(addWidget);
        AbstractWidget subtractWidget = new ImageButton(this.width / 2 + 134,
                rowHeight + 10,
                20,
                10,
                SUBTRACT_SMALL_BUTTON_SPRITES,
                (Button button) -> {
                    consumer.accept(supplier.get() - getCurrentIncrement());
                    editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(supplier.get())));
                });
        TooltipBuilder.create().setLines(getCurrentTooltipLines('-')).build(subtractWidget);
        widgets.add(subtractWidget);
        widgets.add(this.createResetButton(rowHeight, () -> {
            consumer.accept(component.getDefaultValue());
            editBox.setValue(String.valueOf(OrthoViewHandler.roundValue(component.getDefaultValue())));
        }));
    }

    private static int getMaxComponentWidth(Font font) {
        return Math.max(font.width(COMPONENT_Y_ROT), Math.max(font.width(COMPONENT_ZOOM), font.width(COMPONENT_X_ROT)));
    }
}
