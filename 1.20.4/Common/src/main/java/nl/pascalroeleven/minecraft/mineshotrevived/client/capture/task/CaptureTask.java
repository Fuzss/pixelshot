package nl.pascalroeleven.minecraft.mineshotrevived.client.capture.task;

import com.mojang.blaze3d.platform.Window;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.config.ClientConfig;
import nl.pascalroeleven.minecraft.mineshotrevived.client.capture.FramebufferCapturer;
import nl.pascalroeleven.minecraft.mineshotrevived.client.capture.FramebufferWriter;

import java.nio.file.Path;

public class CaptureTask implements RenderTickTask {
	private final Window window;
	private final Path output;

	private int currentFrame;
	private int displayWidth;
	private int displayHeight;

	public CaptureTask(Window window, Path output) {
		this.window = window;
		this.output = output;
	}

	@Override
	public boolean onRenderTick() throws Exception {

        if (this.currentFrame == 0) {

			// Override viewport size (the following frame will be black)
			this.displayWidth = this.window.getWidth();
            this.displayHeight = this.window.getHeight();

            int width = Pixelshot.CONFIG.get(ClientConfig.class).viewCaptureWidth;
            int height = Pixelshot.CONFIG.get(ClientConfig.class).viewCaptureHeight;

            // Resize viewport/framebuffer
            this.window.onFramebufferResize(this.window.getWindow(), width, height);

        } else if (this.currentFrame == 3) {

			// Capture screenshot and restore viewport size
			try {
                FramebufferCapturer fbc = new FramebufferCapturer();
                FramebufferWriter fbw = new FramebufferWriter(this.output, fbc);
                fbw.write();
            } finally {
                // Restore viewport/framebuffer
                this.window.onFramebufferResize(this.window.getWindow(), this.displayWidth, this.displayHeight);
            }
        }

        this.currentFrame++;
		return this.currentFrame > 3;
	}
}
