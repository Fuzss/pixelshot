package fuzs.pixelshot.handler;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.data.MutableDouble;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class OrthoViewHandlerV2 {
    private static final OrthoViewHandlerV2 INSTANCE = new OrthoViewHandlerV2();

    public static final float ZOOM_DEFAULT = 8.0F;
    public static final float ZOOM_STEP = 0.5F;
    public static final float ZOOM_MIN = 0.005F;
    public static final float ZOOM_MAX = 500.0F;
    public static final int X_ROT_DEFAULT = 30;
    public static final int Y_ROT_DEFAULT = 315;
    private static final float CLIPPING_DISTANCE = 1000.0F;
    private static final float ROTATION_STEP_MIN = 8.0F;
    private static final float ROTATION_STEP_MAX = 24.0F;

    public static final KeyMapping KEY_TOGGLE_VIEW = KeyMappingHelper.registerKeyMapping(Pixelshot.id(
            "orthographic_camera"), InputConstants.KEY_F7);
    public static final KeyMapping KEY_OPEN_MENU = KeyMappingHelper.registerKeyMapping(Pixelshot.id("open_menu"),
            InputConstants.KEY_F8
    );
    public static final KeyMapping KEY_ZOOM_IN = KeyMappingHelper.registerKeyMapping(Pixelshot.id("zoom_in"),
            InputConstants.KEY_RBRACKET
    );
    public static final KeyMapping KEY_ZOOM_OUT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("zoom_out"),
            InputConstants.KEY_BACKSLASH
    );
    public static final KeyMapping KEY_ROTATE_LEFT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("rotate_left"),
            InputConstants.KEY_LEFT
    );
    public static final KeyMapping KEY_ROTATE_RIGHT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("rotate_right"),
            InputConstants.KEY_RIGHT
    );
    public static final KeyMapping KEY_ROTATE_UP = KeyMappingHelper.registerKeyMapping(Pixelshot.id("rotate_up"),
            InputConstants.KEY_UP
    );
    public static final KeyMapping KEY_ROTATE_DOWN = KeyMappingHelper.registerKeyMapping(Pixelshot.id("rotate_down"),
            InputConstants.KEY_DOWN
    );
    public static final KeyMapping KEY_SWITCH_PRESET = KeyMappingHelper.registerUnboundKeyMapping(Pixelshot.id(
            "switch_preset"));
    public static final String KEY_ZOOM = Pixelshot.MOD_ID + ".zoom";
    public static final String KEY_X_ROT = Pixelshot.MOD_ID + ".x_rot";
    public static final String KEY_Y_ROT = Pixelshot.MOD_ID + ".y_rot";

    private float zoom;
    private float xRot;
    private float yRot;
    private float oldZoom;
    private float oldXRot;
    private float oldYRot;

    private boolean isActive;
    private boolean followPlayerView;
    private boolean nearClipping;
    private boolean renderSky;
    private boolean renderPlayerEntity;

    private int overlayTicks;
    private ChatFormatting zoomColor = ChatFormatting.WHITE;
    private ChatFormatting xRotColor = ChatFormatting.WHITE;
    private ChatFormatting yRotColor = ChatFormatting.WHITE;
    private boolean tmpHideGui;
    private CameraType tmpCameraType;

    public static void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(KEY_TOGGLE_VIEW);
        context.registerKeyMapping(KEY_OPEN_MENU);
        context.registerKeyMapping(KEY_ZOOM_IN);
        context.registerKeyMapping(KEY_ZOOM_OUT);
        context.registerKeyMapping(KEY_ROTATE_LEFT);
        context.registerKeyMapping(KEY_ROTATE_RIGHT);
        context.registerKeyMapping(KEY_ROTATE_UP);
        context.registerKeyMapping(KEY_ROTATE_DOWN);
    }

    public void onRenderGui(Minecraft minecraft, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {

    }

    private void renderLines(Font font, GuiGraphics guiGraphics, List<String> lines, float alpha, boolean leftSide) {

        for (int j = 0; j < lines.size(); ++j) {
            String s = lines.get(j);
            if (!Strings.isNullOrEmpty(s)) {
                int k = font.width(s);
                int l = leftSide ? 2 : guiGraphics.guiWidth() - 2 - k;
                int i1 = 2 + font.lineHeight * j;
                int ll = 144;
                int ll1 = 0x7f;
                guiGraphics.fill(l - 1, i1 - 1, l + k + 1, i1 + font.lineHeight - 1, 0x505050 | (int) (0x90 * alpha) << 24);
            }
        }

        for (int j1 = 0; j1 < lines.size(); ++j1) {
            String s1 = lines.get(j1);
            if (!Strings.isNullOrEmpty(s1)) {
                int k1 = font.width(s1);
                int l1 = leftSide ? 2 : guiGraphics.guiWidth() - 2 - k1;
                int i2 = 2 + font.lineHeight * j1;
                guiGraphics.drawString(font, s1, l1, i2, 0xe0e0e0 | (int) (0xFF * alpha) << 24, false);
            }
        }
    }

    public void onComputeFieldOfView(GameRenderer renderer, Camera camera, float partialTick, MutableDouble fieldOfView) {
        // just a random event that fires in-between Camera::setup and Camera::isDetached being called,
        // so we can force the player to render despite not being in third-person view
        if (this.isActive && this.renderPlayerEntity) {
            camera.detached = true;
        }
    }

    public void onStartClientTick(Minecraft minecraft) {

        if (this.overlayTicks > 0) {
            this.overlayTicks--;
        }

        if (this.oldZoom != this.zoom) {
            this.overlayTicks = 40;
            this.zoomColor = this.zoom - this.oldZoom > 0.0F ? ChatFormatting.GREEN : ChatFormatting.RED;
            this.xRotColor = this.yRotColor = ChatFormatting.WHITE;
            this.oldZoom = this.zoom;
        }

        if (this.oldXRot != this.xRot) {
            this.overlayTicks = 40;
            this.xRotColor = this.xRot - this.oldXRot > 0.0F ? ChatFormatting.GREEN : ChatFormatting.RED;
            this.zoomColor = this.yRotColor = ChatFormatting.WHITE;
        } else {
            this.setXRot(Mth.wrapDegrees(this.xRot));
        }
        this.oldXRot = this.xRot;

        if (this.oldYRot != this.yRot) {
            this.overlayTicks = 40;
            this.yRotColor = this.yRot - this.oldYRot > 0.0F ? ChatFormatting.GREEN : ChatFormatting.RED;
            this.xRotColor = this.zoomColor = ChatFormatting.WHITE;
        } else {
            this.setYRot(Mth.wrapDegrees(this.yRot));
        }
        this.oldYRot = this.yRot;

        while (KEY_TOGGLE_VIEW.consumeClick()) {
            this.isActive = !this.isActive;
            if (this.isActive && Screen.hasControlDown()) {
                this.reloadCameraSettings();
            }
        }

        while (KEY_OPEN_MENU.consumeClick()) {
            // TODO implement screen
            minecraft.setScreen(null);
        }

        this.updateZoomAndRotation(0.25F);
    }

    private void updateZoomAndRotation(float multiplier) {

        if (KEY_ZOOM_IN.isDown()) {
            this.setZoom(this.zoom / (1.0F + ZOOM_STEP * multiplier));
        }
        if (KEY_ZOOM_OUT.isDown()) {
            this.setZoom(this.zoom * (1.0F + ZOOM_STEP * multiplier));
        }

        float rotationStep = Mth.clamp(this.zoom, ROTATION_STEP_MIN, ROTATION_STEP_MAX) * multiplier;

        if (KEY_ROTATE_LEFT.isDown()) {
            this.setYRot(this.yRot + rotationStep);
        }
        if (KEY_ROTATE_RIGHT.isDown()) {
            this.setYRot(this.yRot - rotationStep);
        }
        if (KEY_ROTATE_UP.isDown()) {
            this.setXRot(this.xRot + rotationStep);
        }
        if (KEY_ROTATE_DOWN.isDown()) {
            this.setXRot(this.xRot - rotationStep);
        }
    }

    public void onBeforeGameRender(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        if (this.isActive) {
            this.tmpHideGui = minecraft.options.hideGui;
            minecraft.options.hideGui = true;
            this.tmpCameraType = minecraft.options.getCameraType();
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    public void onAfterGameRender(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        if (this.isActive) {
            minecraft.options.hideGui = this.tmpHideGui;
            minecraft.options.setCameraType(this.tmpCameraType);
        }

        if (this.isActive && this.overlayTicks > 0) {
            if (minecraft.isGameLoadFinished() && minecraft.level != null && minecraft.screen == null) {
                List<String> lines = new ArrayList<>();
                lines.add(this.getDisplayEntry(KEY_ZOOM, this.zoom, this.zoomColor));
                lines.add(this.getDisplayEntry(KEY_X_ROT, Mth.wrapDegrees(this.xRot), this.xRotColor));
                lines.add(this.getDisplayEntry(KEY_Y_ROT, Mth.wrapDegrees(this.yRot), this.yRotColor));
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.setIdentity();
                posestack.translate(0.0F, 0.0F, -11000.0F);
                RenderSystem.applyModelViewMatrix();
                GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
                this.renderLines(minecraft.font, guiGraphics, lines, Mth.clamp((this.overlayTicks - partialTick) / 5.0F, 0.0F, 1.0F), true);
                guiGraphics.flush();
                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
        }
    }

    private String getDisplayEntry(String translationKey, float value, ChatFormatting color) {
        Component component = Component.translatable(translationKey,
                Component.literal("%.3f".formatted(value)).withStyle(color)
        );
        return new FormattedContentSink(component).getString();
    }

    public void onRenderFog(GameRenderer gameRenderer, Camera camera, float partialTick, FogRenderer.FogMode fogMode, FogType fogType, MutableFloat fogStart, MutableFloat fogEnd, MutableValue<FogShape> fogShape) {
        if (this.isActive && !this.renderSky) {
            fogStart.accept(Float.MAX_VALUE);
            fogEnd.accept(Float.MAX_VALUE);
        }
    }

    public void onComputeCameraAngles(GameRenderer renderer, Camera camera, float partialTick, MutableFloat pitch, MutableFloat yaw, MutableFloat roll) {
        if (this.isActive && !this.followPlayerView) {
            pitch.accept(this.getXRot(partialTick));
            yaw.accept(this.getYRot(partialTick) + 180.0F);
        }
    }

    public void onLoggedIn(LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) {
        this.reloadCameraSettings();
    }

    public void reloadCameraSettings() {
        this.isActive = this.followPlayerView = this.nearClipping = this.renderSky = false;
        this.renderPlayerEntity = true;
        this.setZoom(ZOOM_DEFAULT);
        this.setXRot(Pixelshot.CONFIG.get(ClientConfig.class).defaultXRotation);
        this.setYRot(Pixelshot.CONFIG.get(ClientConfig.class).defaultYRotation);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean renderPlayerEntity() {
        return this.renderPlayerEntity;
    }

    public void setZoom(float zoom) {
        zoom = Mth.clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        if (zoom > this.zoom) {
            Minecraft.getInstance().levelRenderer.needsUpdate();
        }
        this.zoom = zoom;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }

    public float getZoom(float partialTick) {
        return Mth.lerp(partialTick, this.oldZoom, this.zoom);
    }

    public float getXRot(float partialTick) {
        return Mth.wrapDegrees(Mth.lerp(partialTick, this.oldXRot, this.xRot));
    }

    public float getYRot(float partialTick) {
        return Mth.wrapDegrees(Mth.lerp(partialTick, this.oldYRot, this.yRot));
    }

    public Matrix4f getProjectionMatrix(Minecraft minecraft, float partialTick, boolean forFrustum) {
        // thanks to OrthoCamera mod for this trick with offsetting the zoom level for frustum
        // source at https://github.com/DimasKama/OrthoCamera/tree/master
        float height = this.getZoom(partialTick) + (forFrustum ? 20.0F : 0.0F);
        float width = height * (minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight());
        return new Matrix4f().setOrtho(-width,
                width,
                -height,
                height,
                !forFrustum && this.nearClipping ? 0.0F : -CLIPPING_DISTANCE,
                CLIPPING_DISTANCE
        );
    }

    public static OrthoViewHandlerV2 getInstance() {
        return INSTANCE;
    }
}
