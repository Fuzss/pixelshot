package nl.pascalroeleven.minecraft.mineshotrevived.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;

public class OrthoViewHandler {
	private static final Minecraft MC = Minecraft.getInstance();
	private static final float ZOOM_STEP = 2f;
	private static final float ROTATE_STEP = 15;
	private static final float ROTATE_SPEED = 4;
	private static final float SECONDS_PER_TICK = 1f / 20f;

	public static final KeyMapping KEY_TOGGLE_VIEW = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_toggle"),
			InputConstants.KEY_F7
	);
	public static final KeyMapping KEY_ZOOM_IN = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_zoom_in"),
			InputConstants.KEY_RBRACKET);
	public static final KeyMapping KEY_ZOOM_OUT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_zoom_out"),
			InputConstants.KEY_LBRACKET);
	public static final KeyMapping KEY_ROTATE_LEFT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_left"),
			InputConstants.KEY_LEFT);
	public static final KeyMapping KEY_ROTATE_RIGHT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_right"),
			InputConstants.KEY_RIGHT);
	public static final KeyMapping KEY_ROTATE_UP = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_up"),
			InputConstants.KEY_UP);
	public static final KeyMapping KEY_ROTATE_DOWN = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_down"),
			InputConstants.KEY_DOWN);
	
	public static final KeyMapping keyRotateT = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_t"),
			GLFW_KEY_KP_7);
	public static final KeyMapping keyRotateF = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_f"),
			GLFW_KEY_KP_1);
	public static final KeyMapping keyRotateS = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_rotate_s"),
			GLFW_KEY_KP_3);
	public static final KeyMapping keyClip = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_clip"),
			GLFW_KEY_KP_MULTIPLY);
	public static final KeyMapping keyMod = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_mod"),
			GLFW_KEY_LEFT_ALT);
	public static final KeyMapping key360 = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_render360"),
			GLFW_KEY_KP_DIVIDE);
	public static final KeyMapping keyBackground = KeyMappingHelper.registerKeyMapping(Pixelshot.id("ortho_background"),
			GLFW_KEY_KP_DECIMAL);

	public boolean enabled;
	private boolean render360;
	private boolean frustumUpdate;
	private boolean freeCam;
	private boolean clip;
	private int background;

	private float zoom;
	private float xRot;
	private float yRot;

	private int tick;
	private int tickPrevious;
	private double partialPrevious;

	public OrthoViewHandler() {
        this.reset();
	}

	public static void onRegisterKeyMappings(KeyMappingsContext context) {
		context.registerKeyMapping(KEY_TOGGLE_VIEW);
		context.registerKeyMapping(KEY_ZOOM_IN);
		context.registerKeyMapping(KEY_ZOOM_OUT);
		context.registerKeyMapping(KEY_ROTATE_LEFT);
		context.registerKeyMapping(KEY_ROTATE_RIGHT);
		context.registerKeyMapping(KEY_ROTATE_UP);
		context.registerKeyMapping(KEY_ROTATE_DOWN);
		context.registerKeyMapping(keyRotateT);
		context.registerKeyMapping(keyRotateF);
		context.registerKeyMapping(keyRotateS);
		context.registerKeyMapping(keyClip);
		context.registerKeyMapping(keyMod);
		context.registerKeyMapping(key360);
		context.registerKeyMapping(keyBackground);
	}

	// Called by CameraMixin
	public void onComputeCameraAngles(GameRenderer renderer, Camera camera, float partialTick, MutableFloat pitch, MutableFloat yaw, MutableFloat roll) {
		if (!this.enabled) {
			return;
		}

		if (!this.freeCam) {
			pitch.accept(this.xRot);
			yaw.accept(this.yRot + 180.0F);
		}
	}

	// Registered to ClientTickEvents
	public void onClientTickEvent() {
		if (!this.enabled) {
			return;
		}

        this.tick++;
	}

	// Called by WorldRendererMixin
	public Matrix4f onWorldRenderer(float tickDelta) {
		if (!this.enabled) {
			return null;
		}

		// Update zoom and rotation
		if (!this.modifierKeyPressed()) {
			int ticksElapsed = this.tick - this.tickPrevious;
            double elapsed = ticksElapsed + ((double) tickDelta - this.partialPrevious);
			elapsed *= SECONDS_PER_TICK * ROTATE_SPEED;
            this.updateZoomAndRotation(elapsed);

            this.tickPrevious = this.tick;
            this.partialPrevious = tickDelta;
		}

		float width = this.zoom * (MC.getWindow().getWidth()
				/ (float) MC.getWindow().getHeight());
		float height = this.zoom;

		// Override projection matrix
		Matrix4f matrix4f = new Matrix4f().setOrtho(-width, width, -height, height, this.clip ? 0 : -9999, 9999);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		return matrix4f;
	}

	// Called by WorldRendererMixin
	public Matrix4f onSetupFrustum() {
		if (this.frustumUpdate) {
			MC.levelRenderer.needsUpdate();
            this.frustumUpdate = false;
		}

		if (!this.enabled || !this.render360) {
			return null;
		}

		float width = this.zoom * (MC.getWindow().getWidth()
				/ (float) MC.getWindow().getHeight());
		float height = this.zoom;

		// Override projection matrix
		// FIXME: For some reason the client crashes now when clipping too much here.
        return new Matrix4f().setOrtho(-Math.max(10, width), Math.max(10, width), -Math.max(10, height), Math.max(10, height), -9999, 9999);
	}

	public int getBackground() {
		// TODO disable clouds via game option, but better not, so we don't leave it disabled
		return this.background;
	}

	// Called by KeyboardMixin
	public void onKeyEvent(int key, int scanCode, int action, int modifiers) {
		boolean mod = this.modifierKeyPressed();

		// Change perspectives, using modifier key for opposite sides
		if (KEY_TOGGLE_VIEW.isDown()) {
			if (mod) {
                this.freeCam = !this.freeCam;
			} else {
                this.toggle();
			}
		} else if (keyBackground.isDown()) {
            this.cycleBackground();
		} else if (!this.enabled) {
			return;
		} else if (keyClip.isDown()) {
            this.clip = !this.clip;
		} else if (keyRotateT.isDown()) {
            this.xRot = mod ? -90 : 90;
            this.yRot = 0;
		} else if (keyRotateF.isDown()) {
            this.xRot = 0;
            this.yRot = mod ? -90 : 90;
		} else if (keyRotateS.isDown()) {
            this.xRot = 0;
            this.yRot = mod ? 180 : 0;
		} else if (key360.isDown()) {
            this.render360 = !this.render360;
            this.frustumUpdate = true;
		}

		// Update stepped rotation/zoom controls
		// Note: the smooth controls are handled in onWorldRenderer, since they need to be
		// executed on every frame
		if (mod) {
            this.updateZoomAndRotation(1);
			// Snap values to step units
            this.xRot = Math.round(this.xRot / ROTATE_STEP) * ROTATE_STEP;
            this.yRot = Math.round(this.yRot / ROTATE_STEP) * ROTATE_STEP;
            this.zoom = (float) Math.pow(ZOOM_STEP, Math.round(Math.log10(this.zoom) / Math.log10(ZOOM_STEP)));
		}
	}

	private void reset() {
        this.freeCam = false;
        this.clip = false;
        this.render360 = false;

        this.zoom = (float) Math.pow(ZOOM_STEP, 3);
        this.xRot = Pixelshot.CONFIG.get(ClientConfig.class).defaultRotationX;
        this.yRot = Pixelshot.CONFIG.get(ClientConfig.class).defaultRotationY;
        this.tick = 0;
        this.tickPrevious = 0;
        this.partialPrevious = 0;
	}

	private void enable() {
		if (!this.enabled) {
            this.reset();
		}

        this.enabled = true;
	}

	private void disable() {
        this.enabled = false;
	}

	private void toggle() {
		if (this.enabled) {
            this.disable();
		} else {
            this.enable();
		}
	}

	private void cycleBackground() {
		if (this.background == 2) {
            this.background = 0;
		} else {
            this.background++;
		}
  }
  
	private void setZoom(float zoom) {
        this.zoom = zoom;
		// Because zooming is not a native game mechanic, it doesn't trigger a terrain
		// update
		if (this.render360)
			MC.levelRenderer.needsUpdate();
	}

	private boolean modifierKeyPressed() {
		return keyMod.isDown();
	}

	private void updateZoomAndRotation(double multi) {
		if (KEY_ZOOM_IN.isDown()) {
            this.setZoom((float) Math.max(1E-7, (this.zoom / (1 + ((ZOOM_STEP - 1) * multi)))));
		}
		if (KEY_ZOOM_OUT.isDown()) {
            this.setZoom((float) (this.zoom * (1 + ((ZOOM_STEP - 1) * multi))));
		}

		if (KEY_ROTATE_LEFT.isDown()) {
            this.yRot += ROTATE_STEP * multi;
		}
		if (KEY_ROTATE_RIGHT.isDown()) {
            this.yRot -= ROTATE_STEP * multi;
		}

		if (KEY_ROTATE_UP.isDown()) {
            this.xRot += ROTATE_STEP * multi;
		}
		if (KEY_ROTATE_DOWN.isDown()) {
            this.xRot -= ROTATE_STEP * multi;
		}
	}
}
