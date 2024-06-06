package nl.pascalroeleven.minecraft.mineshotrevived;

import nl.pascalroeleven.minecraft.mineshotrevived.client.OrthoViewHandler;
import nl.pascalroeleven.minecraft.mineshotrevived.client.ScreenshotHandler;

public class Mineshot {
	private static OrthoViewHandler ovh = new OrthoViewHandler();
	private static ScreenshotHandler ssh = new ScreenshotHandler();
	
	public static OrthoViewHandler getOrthoViewHandler() {
		return ovh;
	}
	
	public static ScreenshotHandler getScreenshotHandler() {
		return ssh;
	}
}
