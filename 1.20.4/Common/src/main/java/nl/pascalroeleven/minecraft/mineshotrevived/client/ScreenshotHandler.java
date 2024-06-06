package nl.pascalroeleven.minecraft.mineshotrevived.client;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.pixelshot.Pixelshot;
import fuzs.pixelshot.config.ClientConfig;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import nl.pascalroeleven.minecraft.mineshotrevived.client.capture.task.CaptureTask;
import nl.pascalroeleven.minecraft.mineshotrevived.client.capture.task.RenderTickTask;
import nl.pascalroeleven.minecraft.mineshotrevived.util.ChatUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHandler {
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

	public static final KeyMapping keyCapture = KeyMappingHelper.registerKeyMapping(Pixelshot.id("capture"), InputConstants.KEY_F9);

	private Path taskFile;
	private RenderTickTask task;

	private boolean previousHud;

	public static void onRegisterKeyMappings(KeyMappingsContext context) {
		context.registerKeyMapping(keyCapture);
	}

	// Called by KeyboardMixin
	public void onKeyEvent(int key, int scanCode, int action, int modifiers) {
		// Don't poll keys when there's an active task
		if (task != null) {
			return;
		}

		if (keyCapture.isDown()) {
			Minecraft minecraft = Minecraft.getInstance();
			if (Pixelshot.CONFIG.get(ClientConfig.class).hideHudForHugeScreenshots) {
				previousHud = minecraft.options.hideGui;
				minecraft.options.hideGui = true;
			}
			taskFile = getScreenshotFile();
			task = new CaptureTask(minecraft.getWindow(), taskFile);
		}
	}

	// Called by BackgroundRendererMixin
	public void onBeforeGameRender(Minecraft minecraft, GameRenderer gameRenderer, float partialTick) {
		if (task == null) {
			return;
		}

		try {
			if (task.onRenderTick()) {
				task = null;
				ChatUtils.printFileLink("screenshot.success", taskFile.toFile());
				if (Pixelshot.CONFIG.get(ClientConfig.class).hideHudForHugeScreenshots) {
					minecraft.options.hideGui = previousHud;
				}
			}
		} catch (Exception ex) {
			Pixelshot.LOGGER.error("Screenshot capture failed", ex);
			ChatUtils.print("screenshot.failure", ex.getMessage());
			task = null;
			if (Pixelshot.CONFIG.get(ClientConfig.class).hideHudForHugeScreenshots) {
				minecraft.options.hideGui = previousHud;
			}
		}
	}

	private Path getScreenshotFile() {
		Path dir = FabricLoader.getInstance().getGameDir().resolve("screenshots");

		try {
			if (!Files.exists(dir)) {
				Files.createDirectories(dir);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		int i = 0;
		Path file;
		do {
			file = dir.resolve(
					String.format("huge_%s_%04d.tga", DATE_FORMAT.format(new Date()), i++));
		} while (Files.exists(file));

		return file;
	}
}
