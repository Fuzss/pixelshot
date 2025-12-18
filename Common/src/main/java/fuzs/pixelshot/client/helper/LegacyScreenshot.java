package fuzs.pixelshot.client.helper;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Screenshot;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Copied from Minecraft 1.21.4's {@code Screenshot} class.
 */
public class LegacyScreenshot {
    public static final String SCREENSHOT_DIR = "screenshots";
    private int rowHeight;
    private final DataOutputStream outputStream;
    private final byte[] bytes;
    private final int width;
    private final int height;
    private File file;

    public LegacyScreenshot(File gameDirectory, int width, int height, int rowHeight) throws IOException {
        this.width = width;
        this.height = height;
        this.rowHeight = rowHeight;
        File file = new File(gameDirectory, SCREENSHOT_DIR);
        file.mkdir();
        String string = "huge_" + Util.getFilenameFormattedDateTime();
        int i = 1;

        while ((this.file = new File(file, string + (i == 1 ? "" : "_" + i) + ".tga")).exists()) {
            i++;
        }

        byte[] bs = new byte[18];
        bs[2] = 2;
        bs[12] = (byte) (width % 256);
        bs[13] = (byte) (width / 256);
        bs[14] = (byte) (height % 256);
        bs[15] = (byte) (height / 256);
        bs[16] = 24;
        this.bytes = new byte[width * rowHeight * 3];
        this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
        this.outputStream.write(bs);
    }

    public static ByteBuffer allocateMemory(int size) {
        return MemoryUtil.memAlloc(size);
    }

    public static void freeMemory(Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }

    public static void pixelStore(int i, int j) {
        GlStateManager._pixelStore(i, j);
    }

    public static void readPixels(int x, int y, int width, int height, int format, int type, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        _readPixels(x, y, width, height, format, type, byteBuffer);
    }

    public static void _readPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void addRegion(ByteBuffer buffer, int width, int height, int rowWidth, int rowHeight) {
        int i = rowWidth;
        int j = rowHeight;
        if (rowWidth > this.width - width) {
            i = this.width - width;
        }

        if (rowHeight > this.height - height) {
            j = this.height - height;
        }

        this.rowHeight = j;

        for (int k = 0; k < j; k++) {
            buffer.position((rowHeight - j) * rowWidth * 3 + k * rowWidth * 3);
            int l = (width + k * this.width) * 3;
            buffer.get(this.bytes, l, i * 3);
        }
    }

    public void saveRow() throws IOException {
        this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
    }

    public File close() throws IOException {
        this.outputStream.close();
        return this.file;
    }

    /**
     * Adjusted to allow for a custom file name.
     *
     * @see Screenshot#getFile(File)
     */
    public static File getFile(File gameDirectory, String fileNamePrefix, String fileExtension) {
        String fileName = fileNamePrefix + Util.getFilenameFormattedDateTime();
        int i = 1;

        while (true) {
            File file = new File(gameDirectory, fileName + (i == 1 ? "" : "_" + i) + fileExtension);
            if (!file.exists()) {
                return file;
            }

            ++i;
        }
    }
}
