package nl.pascalroeleven.minecraft.mineshotrevived.client.capture;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FramebufferWriter {
	protected static final int HEADER_SIZE = 18;

	protected final FramebufferCapturer fbc;
	protected final Path file;

	public FramebufferWriter(Path file, FramebufferCapturer fbc) {
		this.file = file;
		this.fbc = fbc;
	}

	public void write() throws IOException {
        this.fbc.setFlipColors(true);
        this.fbc.setFlipLines(false);
        this.fbc.capture();

		Dimension dim = this.fbc.getCaptureDimension();
		try (FileChannel fc = FileChannel.open(this.file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			fc.write(this.buildTargaHeader((int) dim.getWidth(), (int) dim.getHeight(), this.fbc.getBytesPerPixel() * 8));
			fc.write(this.fbc.getByteBuffer());
//			STBImageWrite.stbi_write_tga_to_func()
		}
	}

	protected ByteBuffer buildTargaHeader(int width, int height, int bpp) {
		ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(2);
		bb.put((byte) 2); // image type - uncompressed true-color image
		bb.position(12);
		bb.putShort((short) (width & 0xffff));
		bb.putShort((short) (height & 0xffff));
		bb.put((byte) (bpp & 0xff)); // bits per pixel
		bb.rewind();
		return bb;
	}
}
