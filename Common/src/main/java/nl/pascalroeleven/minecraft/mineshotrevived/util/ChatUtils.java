package nl.pascalroeleven.minecraft.mineshotrevived.util;

import static net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE;

import java.io.File;
import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatUtils {
	private static final Minecraft MC = Minecraft.getInstance();

	public static void print(String msg, ChatFormatting format, Object... args) {
		if (MC.gui == null) {
			return;
		}

		ChatComponent chat = MC.gui.getChat();
		MutableComponent ret = Component.translatable(msg, args);
		ret.getStyle().withColor(format);

		chat.addMessage(ret);
	}

	public static void print(String msg, Object... args) {
		print(msg, null, args);
	}

	public static void printFileLink(String msg, File file) {
		MutableComponent text = Component.translatable(file.getName());
		String path;

		try {
			path = file.getAbsoluteFile().getCanonicalPath();
		} catch (IOException ex) {
			path = file.getAbsolutePath();
		}

		text.getStyle().withClickEvent(new ClickEvent(OPEN_FILE, path));
		text.getStyle().withUnderlined(true);

		print(msg, text);
	}
}
