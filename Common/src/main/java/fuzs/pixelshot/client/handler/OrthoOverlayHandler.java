package fuzs.pixelshot.client.handler;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.api.chat.v1.ComponentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class OrthoOverlayHandler {
    public static final OrthoOverlayHandler INSTANCE = new OrthoOverlayHandler();
    private static final ChatFormatting TEXT_COLOR = ChatFormatting.WHITE;
    private static final ChatFormatting POSITIVE_COLOR = ChatFormatting.GREEN;
    private static final ChatFormatting NEGATIVE_COLOR = ChatFormatting.RED;
    private static final int OVERLAY_TIME = 25;
    private static final int OVERLAY_FADE_START = 5;

    private int overlayTicks;
    private ChatFormatting zoomColor = TEXT_COLOR;
    private ChatFormatting xRotColor = TEXT_COLOR;
    private ChatFormatting yRotColor = TEXT_COLOR;

    public void onStartClientTick(Minecraft minecraft) {
        if (this.overlayTicks > 0) this.overlayTicks--;
    }

    public void setZoomOverlay(float newValue, float oldValue) {
        this.overlayTicks = OVERLAY_TIME;
        this.zoomColor = this.getOverlayColor(newValue, oldValue);
        this.xRotColor = this.yRotColor = TEXT_COLOR;
    }

    public void setXRotOverlay(float newValue, float oldValue) {
        this.overlayTicks = OVERLAY_TIME;
        this.xRotColor = this.getOverlayColor(newValue, oldValue);
        this.yRotColor = this.zoomColor = TEXT_COLOR;
    }

    public void setYRotOverlay(float newValue, float oldValue) {
        this.overlayTicks = OVERLAY_TIME;
        this.yRotColor = this.getOverlayColor(newValue, oldValue);
        this.xRotColor = this.zoomColor = TEXT_COLOR;
    }

    private ChatFormatting getOverlayColor(float newValue, float oldValue) {
        return newValue - oldValue > 0.0F ? POSITIVE_COLOR : NEGATIVE_COLOR;
    }

    public void onAfterGameRender(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
        OrthoViewHandler orthoViewHandler = OrthoViewHandler.INSTANCE;
        if (orthoViewHandler.isActive() && this.overlayTicks > 0) {
            // same setup as in GameRenderer::render, so we only render in a level
            if (minecraft.isGameLoadFinished() && minecraft.level != null && minecraft.screen == null) {
                List<String> lines = new ArrayList<>();
                lines.add(this.getDisplayEntry(OrthoViewHandler.KEY_ZOOM,
                        orthoViewHandler.getZoom(1.0F),
                        this.zoomColor
                ));
                float xRot;
                if (orthoViewHandler.followPlayerView()) {
                    xRot = gameRenderer.getMainCamera().getEntity().getViewXRot(1.0F);
                } else {
                    xRot = orthoViewHandler.getXRot(1.0F);
                }
                lines.add(this.getDisplayEntry(OrthoViewHandler.KEY_X_ROT, Mth.wrapDegrees(xRot), this.xRotColor));
                float yRot;
                if (orthoViewHandler.followPlayerView()) {
                    yRot = gameRenderer.getMainCamera().getEntity().getViewYRot(1.0F);
                } else {
                    yRot = orthoViewHandler.getYRot(1.0F);
                }
                lines.add(this.getDisplayEntry(OrthoViewHandler.KEY_Y_ROT, Mth.wrapDegrees(yRot), this.yRotColor));
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.setIdentity();
                posestack.translate(0.0F, 0.0F, -11000.0F);
                RenderSystem.applyModelViewMatrix();
                GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
                this.renderLines(minecraft.font,
                        guiGraphics,
                        lines,
                        Mth.clamp((this.overlayTicks - partialTick) / OVERLAY_FADE_START, 0.0F, 1.0F),
                        true
                );
                guiGraphics.flush();
                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
        }
    }

    private String getDisplayEntry(String translationKey, float value, ChatFormatting color) {
        Component component = Component.translatable(translationKey,
                Component.literal("%.1f".formatted(value)).withStyle(color)
        );
        return ComponentHelper.toString(component);
    }

    /**
     * Copied from
     * {@link net.minecraft.client.gui.components.DebugScreenOverlay#renderLines(GuiGraphics, List, boolean)} with
     * support for transparency.
     */
    private void renderLines(Font font, GuiGraphics guiGraphics, List<String> lines, float alpha, boolean leftSide) {

        for (int j = 0; j < lines.size(); ++j) {
            String s = lines.get(j);
            if (!Strings.isNullOrEmpty(s)) {
                int k = font.width(s);
                int l = leftSide ? 2 : guiGraphics.guiWidth() - 2 - k;
                int i1 = 2 + font.lineHeight * j;
                guiGraphics.fill(l - 1,
                        i1 - 1,
                        l + k + 1,
                        i1 + font.lineHeight - 1,
                        0x505050 | (int) (0x90 * alpha) << 24
                );
            }
        }

        for (int j1 = 0; j1 < lines.size(); ++j1) {
            String s1 = lines.get(j1);
            if (!Strings.isNullOrEmpty(s1)) {
                int k1 = font.width(s1);
                int l1 = leftSide ? 2 : guiGraphics.guiWidth() - 2 - k1;
                int i2 = 2 + font.lineHeight * j1;
                // don't go below 4 as alpha will be set to 100% by the font renderer then
                guiGraphics.drawString(font, s1, l1, i2, 0xE0E0E0 | Math.max(5, (int) (0xFF * alpha)) << 24, false);
            }
        }
    }
}
