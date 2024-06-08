package fuzs.pixelshot.client.gui.screens;

import com.google.common.collect.Sets;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.handler.OrthoViewHandler;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class AbstractCameraScreen extends Screen {
    static final ResourceLocation WIDGETS_LOCATION = Pixelshot.id("textures/gui/widgets.png");
    static final Component COMPONENT_ON = Component.empty()
            .append(CommonComponents.OPTION_ON)
            .withStyle(ChatFormatting.GREEN);
    static final Component COMPONENT_OFF = Component.empty()
            .append(CommonComponents.OPTION_OFF)
            .withStyle(ChatFormatting.RED);
    public static final Component COMPONENT_TITLE = Component.translatable("screen.orthographic_camera.title");
    public static final String KEY_FOLLOW_VIEW = "screen.orthographic_camera.follow_view";
    public static final String KEY_NEAR_CLIPPING = "screen.orthographic_camera.near_clipping";
    public static final String KEY_RENDER_SKY = "screen.orthographic_camera.render_sky";
    public static final String KEY_RENDER_PLAYER = "screen.orthographic_camera.render_player";

    private static Type lastType = Type.EDIT_BOX;

    final Type type;
    final OrthoViewHandler handler;
    boolean focusModeActive;

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
        this.addRenderableWidget(Button.builder(getOptionComponent(KEY_FOLLOW_VIEW, this.handler.followPlayerView()),
                (Button button) -> {
                    this.handler.flipFollowPlayerView();
                    button.setMessage(getOptionComponent(KEY_FOLLOW_VIEW, this.handler.followPlayerView()));
                }
        ).bounds(this.width / 2 - 156, this.height / 6 + 100, 154, 20).build());
        this.addRenderableWidget(Button.builder(getOptionComponent(KEY_NEAR_CLIPPING, this.handler.nearClipping()),
                (Button button) -> {
                    this.handler.flipNearClipping();
                    button.setMessage(getOptionComponent(KEY_NEAR_CLIPPING, this.handler.nearClipping()));
                }
        ).bounds(this.width / 2 + 2, this.height / 6 + 100, 154, 20).build());
        this.addRenderableWidget(Button.builder(getOptionComponent(KEY_RENDER_SKY, this.handler.renderSky()),
                (Button button) -> {
                    this.handler.flipRenderSky();
                    button.setMessage(getOptionComponent(KEY_RENDER_SKY, this.handler.renderSky()));
                }
        ).bounds(this.width / 2 - 156, this.height / 6 + 126, 154, 20).build());
        this.addRenderableWidget(Button.builder(getOptionComponent(KEY_RENDER_PLAYER,
                this.handler.renderPlayerEntity()
        ), (Button button) -> {
            this.handler.flipRenderPlayerEntity();
            button.setMessage(getOptionComponent(KEY_RENDER_PLAYER, this.handler.renderPlayerEntity()));
        }).bounds(this.width / 2 + 2, this.height / 6 + 126, 154, 20).build());
        for (int i = 0; i < 3; i++) {
            Set<AbstractWidget> widgets = Sets.newIdentityHashSet();
            this.addControlRow(this.height / 6 + 20 + i * 25, widgets);
            widgets.forEach(this::addRenderableWidget);
        }
    }

    void addControlRow(int rowHeight, Collection<AbstractWidget> widgets) {
        widgets.add(new SpritelessImageButton(this.width / 2 + 158, rowHeight, 20, 20, 4 * 20, 0, WIDGETS_LOCATION, (Button button) -> {
            this.focusModeActive = !this.focusModeActive;
            for (Renderable renderable : this.renderables) {
                if (renderable instanceof AbstractWidget abstractWidget && !widgets.contains(abstractWidget)) {
                    abstractWidget.visible = !this.focusModeActive;
                }
            }
        }).setDrawBackground().setTextureLayout(value -> 0));
    }

    static Component getOptionComponent(String translationKey, boolean onOffState) {
        return Component.translatable(translationKey, onOffState ? COMPONENT_ON : COMPONENT_OFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.focusModeActive) {
            guiGraphics.drawCenteredString(this.font, COMPONENT_TITLE, this.width / 2, this.height / 6 - 15, 0XFFFFFF);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.focusModeActive) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static Screen openScreen() {
        return lastType.factory.apply(COMPONENT_TITLE, OrthoViewHandler.INSTANCE);
    }

    enum Type {
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
}
