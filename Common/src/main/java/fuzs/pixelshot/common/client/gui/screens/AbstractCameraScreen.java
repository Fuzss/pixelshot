package fuzs.pixelshot.common.client.gui.screens;

import fuzs.pixelshot.common.Pixelshot;
import fuzs.pixelshot.common.client.handler.OrthoOverlayHandler;
import fuzs.pixelshot.common.client.handler.OrthoViewHandler;
import fuzs.pixelshot.common.config.ClientConfig;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractCameraScreen extends Screen {
    public static final WidgetSprites FOCUS_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id("widget/focus_button"),
            Pixelshot.id("widget/focus_button_disabled"),
            Pixelshot.id("widget/focus_button_highlighted"));
    public static final WidgetSprites EXIT_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id("widget/exit_button"),
            Pixelshot.id("widget/exit_button_disabled"),
            Pixelshot.id("widget/exit_button_highlighted"));
    public static final WidgetSprites RESET_BUTTON_SPRITES = new WidgetSprites(Pixelshot.id("widget/reset_button"),
            Pixelshot.id("widget/reset_button_disabled"),
            Pixelshot.id("widget/reset_button_highlighted"));
    private static final Component COMPONENT_ON = Component.empty()
            .append(CommonComponents.OPTION_ON)
            .withStyle(ChatFormatting.GREEN);
    private static final Component COMPONENT_OFF = Component.empty()
            .append(CommonComponents.OPTION_OFF)
            .withStyle(ChatFormatting.RED);
    public static final Component COMPONENT_TITLE = Component.translatable("screen.orthographic_camera.title");
    public static final String KEY_FOLLOW_VIEW = "screen.orthographic_camera.follow_view";
    public static final String KEY_NEAR_CLIPPING = "screen.orthographic_camera.near_clipping";
    public static final String KEY_RENDER_SKY = "screen.orthographic_camera.render_sky";
    public static final String KEY_RENDER_PLAYER = "screen.orthographic_camera.render_player";
    protected static final float DEFAULT_INCREMENT = 1.0F;
    protected static final float LARGE_INCREMENT = 10.0F;
    protected static final float SMALL_INCREMENT = 0.1F;

    private static Type lastType = Type.EDIT_BOX;

    protected final Type type;
    protected final OrthoViewHandler handler;
    private boolean focusModeActive;

    AbstractCameraScreen(Type type, Component title, OrthoViewHandler handler) {
        super(title);
        this.type = lastType = type;
        this.handler = handler;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (Button button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height / 6 + 160, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("<<"), (Button button) -> {
            this.minecraft.setScreen(this.type.cycle().factory.apply(this.getTitle(), this.handler));
        }).bounds(this.width / 2 - 136, this.height / 6 - 20, 50, 20).build()).active = !this.type.isLeft;
        this.addRenderableWidget(Button.builder(Component.literal(">>"), (Button button) -> {
            this.minecraft.setScreen(this.type.cycle().factory.apply(this.getTitle(), this.handler));
        }).bounds(this.width / 2 + 86, this.height / 6 - 20, 50, 20).build()).active = this.type.isLeft;

        Collection<AbstractWidget> rotationWidgets = new ArrayList<>();
        this.addRenderableWidget(Button.builder(createOptionComponent(KEY_FOLLOW_VIEW, this.handler.followPlayerView()),
                (Button button) -> {
                    this.handler.flipFollowPlayerView();
                    button.setMessage(createOptionComponent(KEY_FOLLOW_VIEW, this.handler.followPlayerView()));
                    rotationWidgets.forEach(abstractWidget -> abstractWidget.active = !this.handler.followPlayerView());
                }).bounds(this.width / 2 - 154, this.height / 6 + 100, 150, 20).build());
        this.addRenderableWidget(Button.builder(createOptionComponent(KEY_NEAR_CLIPPING, this.handler.nearClipping()),
                (Button button) -> {
                    this.handler.flipNearClipping();
                    button.setMessage(createOptionComponent(KEY_NEAR_CLIPPING, this.handler.nearClipping()));
                }).bounds(this.width / 2 + 4, this.height / 6 + 100, 150, 20).build());
        this.addRenderableWidget(Button.builder(createOptionComponent(KEY_RENDER_SKY, this.handler.renderSky()),
                (Button button) -> {
                    this.handler.flipRenderSky();
                    button.setMessage(createOptionComponent(KEY_RENDER_SKY, this.handler.renderSky()));
                }).bounds(this.width / 2 - 154, this.height / 6 + 126, 150, 20).build());
        this.addRenderableWidget(Button.builder(createOptionComponent(KEY_RENDER_PLAYER,
                this.handler.renderPlayerEntity()), (Button button) -> {
            this.handler.flipRenderPlayerEntity();
            button.setMessage(createOptionComponent(KEY_RENDER_PLAYER, this.handler.renderPlayerEntity()));
        }).bounds(this.width / 2 + 4, this.height / 6 + 126, 150, 20).build());

        for (int i = 0; i < OrthoComponent.VALUES.length; i++) {
            OrthoComponent component = OrthoComponent.VALUES[i];
            Collection<AbstractWidget> widgets = new ArrayList<>();
            this.addControlRow(component, this.height / 6 + 20 + i * 25, widgets);
            widgets.forEach(this::addRenderableWidget);
            if (component.disableWhenFollowingPlayerView()) {
                rotationWidgets.addAll(widgets);
            }
        }

        rotationWidgets.forEach((AbstractWidget abstractWidget) -> {
            abstractWidget.active = !this.handler.followPlayerView();
        });
    }

    protected void addControlRow(OrthoComponent component, int rowHeight, Collection<AbstractWidget> widgets) {
        AbstractWidget[] focusModeWidgets = new AbstractWidget[2];
        focusModeWidgets[0] = new ImageButton(this.width / 2 + 182,
                rowHeight,
                20,
                20,
                FOCUS_BUTTON_SPRITES,
                (Button button) -> {
                    this.setFocusModeActive(true, widgets, focusModeWidgets);
                });
        focusModeWidgets[1] = new ImageButton(this.width / 2 + 182,
                rowHeight,
                20,
                20,
                EXIT_BUTTON_SPRITES,
                (Button button) -> {
                    this.setFocusModeActive(false, widgets, focusModeWidgets);
                });
        widgets.addAll(Arrays.asList(focusModeWidgets));
        this.setFocusModeButtons(focusModeWidgets);
    }

    private void setFocusModeActive(boolean isFocusModeActive, Collection<AbstractWidget> widgets, AbstractWidget[] focusModeWidgets) {
        this.focusModeActive = isFocusModeActive;
        for (GuiEventListener guiEventListener : this.children()) {
            if (guiEventListener instanceof AbstractWidget abstractWidget && !widgets.contains(abstractWidget)) {
                abstractWidget.visible = !isFocusModeActive;
            }
        }

        this.setFocusModeButtons(focusModeWidgets);
    }

    private void setFocusModeButtons(AbstractWidget[] focusModeWidgets) {
        focusModeWidgets[0].visible = !this.focusModeActive;
        focusModeWidgets[1].visible = this.focusModeActive;
    }

    protected AbstractWidget createResetButton(int rowHeight, Runnable runnable) {
        return new ImageButton(this.width / 2 + 158, rowHeight, 20, 20, RESET_BUTTON_SPRITES, (Button button) -> {
            runnable.run();
        });
    }

    protected static Component createOptionComponent(String translationKey, boolean onOffState) {
        return Component.translatable(translationKey, onOffState ? COMPONENT_ON : COMPONENT_OFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.focusModeActive) {
            guiGraphics.centeredText(this.font, COMPONENT_TITLE, this.width / 2, this.height / 6 - 15, -1);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.focusModeActive) {
            super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static Screen openScreen() {
        return lastType.factory.apply(COMPONENT_TITLE, OrthoViewHandler.INSTANCE);
    }

    static float getCurrentIncrement() {
        if (CommonHelper.hasShiftDown()) {
            return LARGE_INCREMENT;
        } else if (CommonHelper.hasAltDown()) {
            return SMALL_INCREMENT;
        } else {
            return DEFAULT_INCREMENT;
        }
    }

    static Supplier<List<? extends FormattedText>> getCurrentTooltipLines(char sign) {
        return () -> Collections.singletonList(FormattedText.of(String.valueOf(sign) + getCurrentIncrement()));
    }

    protected enum Type {
        SLIDER(SliderCameraScreen::new, false),
        EDIT_BOX(EditBoxCameraScreen::new, true);

        static final Type[] VALUES = values();

        final BiFunction<Component, OrthoViewHandler, Screen> factory;
        final boolean isLeft;

        Type(BiFunction<Component, OrthoViewHandler, Screen> factory, boolean isLeft) {
            this.factory = factory;
            this.isLeft = isLeft;
        }

        public Type cycle() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }
    }

    protected enum OrthoComponent {
        ZOOM(OrthoOverlayHandler.KEY_ZOOM,
                OrthoViewHandler.ZOOM_MIN,
                OrthoViewHandler.ZOOM_MAX,
                OrthoViewHandler::getZoom,
                OrthoViewHandler::setZoom),
        X_ROTATION(OrthoOverlayHandler.KEY_X_ROTATION,
                OrthoViewHandler.X_ROTATION_MIN,
                OrthoViewHandler.X_ROTATION_MAX,
                OrthoViewHandler::getXRot,
                OrthoViewHandler::setXRot),
        Y_ROTATION(OrthoOverlayHandler.KEY_Y_ROTATION,
                OrthoViewHandler.Y_ROTATION_MIN,
                OrthoViewHandler.Y_ROTATION_MAX,
                OrthoViewHandler::getYRot,
                OrthoViewHandler::setYRot);

        private static final OrthoComponent[] VALUES = values();

        public final String translationKey;
        public final Component component;
        public final float minValue;
        public final float maxValue;
        public final Function<OrthoViewHandler, Float> supplier;
        public final BiConsumer<OrthoViewHandler, Float> consumer;

        OrthoComponent(String translationKey, float minValue, float maxValue, Function<OrthoViewHandler, Float> supplier, BiConsumer<OrthoViewHandler, Float> consumer) {
            this.translationKey = translationKey;
            this.component = Component.translatable(translationKey, "");
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.supplier = supplier;
            this.consumer = consumer;
        }

        public boolean disableWhenFollowingPlayerView() {
            return this != ZOOM;
        }

        public boolean supportsLogarithmicScale() {
            return this == ZOOM;
        }

        public float getDefaultValue() {
            return (float) switch (this) {
                case ZOOM -> Pixelshot.CONFIG.get(ClientConfig.class).orthographicCamera.initialZoomLevel;
                case X_ROTATION -> Pixelshot.CONFIG.get(ClientConfig.class).orthographicCamera.initialXRotation;
                case Y_ROTATION -> Pixelshot.CONFIG.get(ClientConfig.class).orthographicCamera.initialYRotation;
            };
        }
    }
}
