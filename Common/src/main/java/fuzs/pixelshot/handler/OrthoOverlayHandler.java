package fuzs.pixelshot.handler;

import net.minecraft.ChatFormatting;

public class OrthoOverlayHandler {
    private static final ChatFormatting TEXT_COLOR = ChatFormatting.WHITE;
    private static final ChatFormatting POSITIVE_COLOR = ChatFormatting.GREEN;
    private static final ChatFormatting NEGATIVE_COLOR = ChatFormatting.RED;
    private static final int OVERLAY_TIME = 40;
    private static final int OVERLAY_FADE_START = 5;

    private int overlayTicks;
    private ChatFormatting zoomColor = TEXT_COLOR;
    private ChatFormatting xRotColor = TEXT_COLOR;
    private ChatFormatting yRotColor = TEXT_COLOR;
}
