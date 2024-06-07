package fuzs.pixelshot.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.shaders.FogShape;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.client.helper.DirectionHelper;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.data.MutableDouble;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.Connection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OrthoViewHandler {
    public static final OrthoViewHandler INSTANCE = new OrthoViewHandler();
    public static final float ZOOM_DEFAULT = 8.0F;
    public static final float ZOOM_STEP = 0.5F;
    public static final float ZOOM_MIN = 0.1F;
    public static final float ZOOM_MAX = 500.0F;
    public static final int X_ROT_DEFAULT = 30;
    public static final int Y_ROT_DEFAULT = 135;
    private static final float CLIPPING_DISTANCE = 1000.0F;
    private static final float ROTATION_STEP_MIN = 8.0F;
    private static final float ROTATION_STEP_MAX = 24.0F;
    private static final float STEP_MULTIPLIER = 0.25F;
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

    private boolean tmpHideGui;
    private CameraType tmpCameraType;

    public static void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(KEY_TOGGLE_VIEW, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_OPEN_MENU, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_ZOOM_IN, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_ZOOM_OUT, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_ROTATE_LEFT, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_ROTATE_RIGHT, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_ROTATE_UP, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_ROTATE_DOWN, KeyActivationContext.GAME);
        context.registerKeyMapping(KEY_SWITCH_PRESET, KeyActivationContext.GAME);
    }

    public void onComputeFieldOfView(GameRenderer renderer, Camera camera, float partialTick, MutableDouble fieldOfView) {
        // just a random event that fires in-between Camera::setup and Camera::isDetached being called,
        // so we can force the player to render despite not being in third-person view
        if (this.isActive && this.renderPlayerEntity) {
            camera.detached = true;
        }
    }

    public void onStartClientTick(Minecraft minecraft) {

        if (this.oldZoom != this.zoom) {
            OrthoOverlayHandler.INSTANCE.setZoomOverlay(this.zoom, this.oldZoom);
        }
        if (this.oldXRot != this.xRot) {
            OrthoOverlayHandler.INSTANCE.setXRotOverlay(this.xRot, this.oldXRot);
        }
        if (this.oldYRot != this.yRot) {
            OrthoOverlayHandler.INSTANCE.setYRotOverlay(this.yRot, this.oldYRot);
        }

        // do not fix up rotation values (like Mth::wrapDegrees), it will mess with the presets which require value identity
        this.setOldValues();

        while (KEY_TOGGLE_VIEW.consumeClick()) {
            this.isActive = !this.isActive;
            if (this.isActive && Screen.hasControlDown()) {
                this.reloadCameraSettings(this.isActive);
            }
        }
        while (KEY_SWITCH_PRESET.consumeClick()) {
            if (this.isActive && !this.followPlayerView) {
                Vector3f vector3f = DirectionHelper.cycle(this.xRot, this.yRot, !Screen.hasControlDown());
                this.setXRot(vector3f.x());
                this.setYRot(vector3f.z());
            }
        }
        while (KEY_OPEN_MENU.consumeClick()) {
            // TODO implement screen
            if (this.isActive) {
                minecraft.setScreen(null);
            }
        }

        this.updateZoomAndRotation();
    }

    private void updateZoomAndRotation() {

        if (!this.isActive) return;

        if (KEY_ZOOM_IN.isDown()) {
            this.setZoom(this.zoom / (1.0F + ZOOM_STEP * STEP_MULTIPLIER));
        }
        if (KEY_ZOOM_OUT.isDown()) {
            this.setZoom(this.zoom * (1.0F + ZOOM_STEP * STEP_MULTIPLIER));
        }

        if (this.followPlayerView) return;

        float rotationStep = Mth.clamp(this.zoom, ROTATION_STEP_MIN, ROTATION_STEP_MAX) * STEP_MULTIPLIER;

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
    }

    public void onRenderFog(GameRenderer gameRenderer, Camera camera, float partialTick, FogRenderer.FogMode fogMode, FogType fogType, MutableFloat fogStart, MutableFloat fogEnd, MutableValue<FogShape> fogShape) {
        if (this.isActive && !this.renderSky) {
            // this hides the fog, we could alternatively set the fog color with an alpha of zero via RenderSystem::setShaderFogColor
            fogStart.accept(Float.MAX_VALUE);
            fogEnd.accept(Float.MAX_VALUE);
        }
    }

    public void onComputeCameraAngles(GameRenderer renderer, Camera camera, float partialTick, MutableFloat pitch, MutableFloat yaw, MutableFloat roll) {
        if (this.isActive && !this.followPlayerView) {
            pitch.accept(this.getXRot(partialTick));
            yaw.accept(this.getYRot(partialTick));
        }
    }

    public void onLoggedIn(LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) {
        this.reloadCameraSettings(false);
    }

    public void reloadCameraSettings(boolean isActive) {
        this.isActive = isActive;
        this.followPlayerView = this.nearClipping = this.renderSky = false;
        this.renderPlayerEntity = true;
        this.setZoom(ZOOM_DEFAULT);
        this.setXRot(Pixelshot.CONFIG.get(ClientConfig.class).defaultXRotation);
        this.setYRot(Pixelshot.CONFIG.get(ClientConfig.class).defaultYRotation);
        this.setOldValues();
    }

    private void setOldValues() {
        this.oldZoom = this.zoom;
        this.oldXRot = this.xRot;
        this.oldYRot = this.yRot;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean followPlayerView() {
        return this.followPlayerView;
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
        return partialTick == 1.0F ? this.zoom : Mth.lerp(partialTick, this.oldZoom, this.zoom);
    }

    public float getXRot(float partialTick) {
        return Mth.wrapDegrees(partialTick == 1.0F ? this.xRot : Mth.lerp(partialTick, this.oldXRot, this.xRot));
    }

    public float getYRot(float partialTick) {
        return Mth.wrapDegrees(partialTick == 1.0F ? this.yRot : Mth.lerp(partialTick, this.oldYRot, this.yRot));
    }

    public Matrix4f getProjectionMatrix(Minecraft minecraft, float partialTick, boolean forFrustum) {
        // thanks to OrthoCamera mod for this trick with offsetting the zoom level for frustum
        // otherwise game often completely freezes when frustum and projection matrix match
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
}
