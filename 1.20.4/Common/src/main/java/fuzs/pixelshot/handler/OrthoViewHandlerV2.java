package fuzs.pixelshot.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.shaders.FogShape;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.Connection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;

public class OrthoViewHandlerV2 {
    private static final OrthoViewHandlerV2 INSTANCE = new OrthoViewHandlerV2();
    public static final float ZOOM_DEFAULT = 8.0F;
    public static final float ZOOM_STEP = 0.5F;
    public static final float ZOOM_MIN = 0.05F;
    public static final float ZOOM_MAX = 512.0F;
    public static final int X_ROT_DEFAULT = 30;
    public static final int Y_ROT_DEFAULT = 315;

    public static final KeyMapping KEY_TOGGLE_VIEW = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_toggle"),
            InputConstants.KEY_F7
    );
    public static final KeyMapping KEY_ZOOM_IN = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_zoom_in"),
            InputConstants.KEY_RBRACKET
    );
    public static final KeyMapping KEY_ZOOM_OUT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_zoom_out"),
            InputConstants.KEY_LBRACKET
    );
    public static final KeyMapping KEY_ROTATE_LEFT = KeyMappingHelper.registerKeyMapping(Pixelshot.id(
            "ortho_rotate_left"), InputConstants.KEY_LEFT);
    public static final KeyMapping KEY_ROTATE_RIGHT = KeyMappingHelper.registerKeyMapping(Pixelshot.id(
            "ortho_rotate_right"), InputConstants.KEY_RIGHT);
    public static final KeyMapping KEY_ROTATE_UP = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_up"),
            InputConstants.KEY_UP
    );
    public static final KeyMapping KEY_ROTATE_DOWN = KeyMappingHelper.registerKeyMapping(Pixelshot.id(
            "ortho_rotate_down"), InputConstants.KEY_DOWN);

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
        context.registerKeyMapping(KEY_TOGGLE_VIEW);
        context.registerKeyMapping(KEY_ZOOM_IN);
        context.registerKeyMapping(KEY_ZOOM_OUT);
        context.registerKeyMapping(KEY_ROTATE_LEFT);
        context.registerKeyMapping(KEY_ROTATE_RIGHT);
        context.registerKeyMapping(KEY_ROTATE_UP);
        context.registerKeyMapping(KEY_ROTATE_DOWN);
    }

    public void onStartClientTick(Minecraft minecraft) {
        this.oldZoom = this.zoom;
        this.oldXRot = this.xRot;
        this.oldYRot = this.yRot;

        while (KEY_TOGGLE_VIEW.consumeClick()) {
            this.isActive = !this.isActive;
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

        float rotationStep = Mth.clamp(this.zoom, 8.0F, 24.0F) * multiplier;

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
                !forFrustum && this.nearClipping ? 0.0F : -1000.0F,
                1000.0F
        );
    }

    public static OrthoViewHandlerV2 getInstance() {
        return INSTANCE;
    }
}
