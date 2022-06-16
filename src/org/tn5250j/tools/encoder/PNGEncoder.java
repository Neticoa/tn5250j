/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,202,2003
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.tools.encoder;

import java.io.IOException;
import java.util.zip.Deflater;

import javafx.scene.image.PixelFormat;

/**
 * This class encodes a Png file from an Image to an OutputStream.  No color
 * depths except 8 and 16 bpp are supported.  No additional Png blocks except
 * those absolutely necessary for Png encoding are included.
 */
public class PNGEncoder extends AbstractImageEncoder {

    @Override
    public void saveImage() throws IOException, EncoderException {
        if (img == null)
            error("PNG encoding error: Image is NULL.");

        final int width = (int) Math.round(img.getWidth());
        final int height = (int) Math.round(img.getHeight());

        final int[] pixelarray = new int[width * height];
        img.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbPreInstance(), pixelarray, 0, width);

        ofile.write(byteFromInt(137));
        ofile.write(byteFromInt(80));
        ofile.write(byteFromInt(78));
        ofile.write(byteFromInt(71));
        ofile.write(byteFromInt(13));
        ofile.write(byteFromInt(10));
        ofile.write(byteFromInt(26));
        ofile.write(byteFromInt(10));

        // IHDR

        long crc = start_crc();

        // length

        ofile.write(bytesFromLong(13));
        ofile.write(byteFromChar('I'));
        ofile.write(byteFromChar('H'));
        ofile.write(byteFromChar('D'));
        ofile.write(byteFromChar('R'));

        crc = update_crc(crc, byteFromChar('I'));
        crc = update_crc(crc, byteFromChar('H'));
        crc = update_crc(crc, byteFromChar('D'));
        crc = update_crc(crc, byteFromChar('R'));
        crc = update_crc(crc, bytesFromLong(width));
        crc = update_crc(crc, bytesFromLong(height));
        crc = update_crc(crc, byteFromInt(8));
        crc = update_crc(crc, byteFromInt(2));
        crc = update_crc(crc, byteFromInt(0));
        crc = update_crc(crc, byteFromInt(0));
        crc = update_crc(crc, byteFromInt(0));

        ofile.write(bytesFromLong(width));
        ofile.write(bytesFromLong(height));
        ofile.write(byteFromInt(8));
        ofile.write(byteFromInt(2)); // Color type
        ofile.write(byteFromInt(0)); // Compression type
        ofile.write(byteFromInt(0)); // Filter method
        ofile.write(byteFromInt(0)); // Interlace method
        ofile.write(bytesFromLong(end_crc(crc)));

        // IDAT
        final byte[] outarray = new byte[(pixelarray.length * 3) + height];
        final int size = compress(outarray, pixelarray, width, height);

        crc = start_crc();

        ofile.write(bytesFromLong(size));
        ofile.write(byteFromChar('I'));
        ofile.write(byteFromChar('D'));
        ofile.write(byteFromChar('A'));
        ofile.write(byteFromChar('T'));

        crc = update_crc(crc, byteFromChar('I'));
        crc = update_crc(crc, byteFromChar('D'));
        crc = update_crc(crc, byteFromChar('A'));
        crc = update_crc(crc, byteFromChar('T'));

        ofile.write(outarray, 0, size);

        for (int i = 0; i < (size); i++) {
            crc = update_crc(crc, outarray[i]);
        }

        ofile.write(bytesFromLong(end_crc(crc)));

        // IEND
        crc = start_crc();

        ofile.write(bytesFromLong(0));
        ofile.write(byteFromChar('I'));
        ofile.write(byteFromChar('E'));
        ofile.write(byteFromChar('N'));
        ofile.write(byteFromChar('D'));

        crc = update_crc(crc, byteFromChar('I'));
        crc = update_crc(crc, byteFromChar('E'));
        crc = update_crc(crc, byteFromChar('N'));
        crc = update_crc(crc, byteFromChar('D'));

        ofile.write(bytesFromLong(end_crc(crc)));

    }

    /**
     * @param outarray target array.
     * @param pixelarray pixel array.
     * @param width width.
     * @param height height.
     * @return total number compressed.
     * @throws EncoderException encoding exception
     */
    public int compress(final byte[] outarray, final int[] pixelarray, final int width, final int height) throws EncoderException {
        final byte[] inarray = new byte[(pixelarray.length * 3) + height];
        for (int i = 0; i < height; i++) {
            inarray[i * ((width * 3) + 1)] = byteFromInt(0);
            for (int j = 0; j < (width * 3); j += 3) {
                final int pixel = pixelarray[i * width + (int) Math.floor(j / 3)];
                final int red   = ((pixel >> 16) & 0xff);
                final int green = ((pixel >>  8) & 0xff);
                final int blue  = ((pixel      ) & 0xff);

                inarray[(i * ((width * 3) + 1)) + j + 1] = (byte) red;
                inarray[(i * ((width * 3) + 1)) + j + 2] = (byte) green;
                inarray[(i * ((width * 3) + 1)) + j + 3] = (byte) blue;
            }
        }
        return compressInternal(outarray, inarray);
    }

    /**
     * @param outarray target array.
     * @param pixelarray pixel array.
     * @param width width.
     * @param height height.
     * @return total number compressed.
     * @throws EncoderException encoding exception.
     */
    public int compress(final byte[] outarray, final byte[] pixelarray, final int width, final int height) throws EncoderException {
        final byte[] inarray = new byte[pixelarray.length + height];
        for (int i = 0; i < height; i++) {
            inarray[i * (width + 1)] = byteFromInt(0);
            for (int j = 0; j < width; j++) {
                inarray[(i * (width + 1)) + j + 1] = pixelarray[(i * width) + j];
            }
        }
        return compressInternal(outarray, inarray);
    }

    /**
     * @param outarray output array.
     * @param inarray input array.
     * @return total number compressed.
     * @throws EncoderException encoding exception.
     */
    private int compressInternal(final byte[] outarray, final byte[] inarray) throws EncoderException {
        final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try {
            deflater.setInput(inarray, 0, inarray.length);
            deflater.finish();
            deflater.deflate(outarray);
            if (!deflater.finished()) {
                error("PNG encoding error: Deflater could not compress image data.");
            }
            return (deflater.getTotalOut());
        } finally {
            deflater.end();
        }
    }

    private long crc_table[] = null;

    private void make_crc_table() {
        crc_table = new long[256];
        long c;
        int n;
        int k;
        for (n = 0; n < 256; n++) {
            c = n;
            for (k = 0; k < 8; k++) {
                if ((c & 1) != 0)
                    c = 0xedb88320L ^ (c >> 1);
                else
                    c = c >> 1;

            }
            crc_table[n] = c;

        }
    }

    private final static long start_crc() {
        return 0xffffffffL;
    }

    private final static long end_crc(final long crc) {
        return crc ^ 0xffffffffL;
    }

    private long update_crc(final long crc, final byte[] buf) {

        long c = crc;
        for (int i = 0; i < buf.length; i++) {
            c = update_crc(c, buf[i]);
        }

        return c;
    }

    private long update_crc(final long crc, final byte buf) {
        if (crc_table == null) {
            make_crc_table();
        }
        return crc_table[(int) ((crc ^ buf) & 0xff)] ^ (crc >> 8);
    }

}
