/**
 *
 */
package org.tn5250j.encoding.builtin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tmp {
    static class IndexedChar {
        public final int index;
        public final Character character;

        public IndexedChar(final int index, final Character character) {
            super();
            this.index = index;
            this.character = character;
        }
    }

    public static void main(final String[] args) throws IOException {
        doMain();
//        ConversionMaps.encodingCcsid_.put("cp1399", "1399");
//        final ConvTable table = ConvTable.getTable("cp1399");
//        System.out.println(table);
        System.out.println((0xFF * 0xFF));
    }

    /**
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static void doMain() throws IOException, UnsupportedEncodingException {
        final InputStream in = Tmp.class.getResourceAsStream("JIS2004.txt");
        try {
            process(new InputStreamReader(in, "UTF-16"));
        } finally {
            in.close();
        }

        System.out.println(Short.MAX_VALUE);
    }

    private static void process(final InputStreamReader r) throws IOException {
        int max = 0;

        final LineNumberReader reader = new LineNumberReader(r);
        String line;
        while ((line = reader.readLine()) != null) {
            final String trimed = line.trim();
            if (!trimed.isEmpty()) {
                final IndexedChar ch = processLine(trimed);
                if (ch.index > max) {
                    max = ch.index;
                }
            }
        }

        System.out.println("Max index: " + max);
    }

    private static IndexedChar processLine(final String line) {
        final int index = line.indexOf('=');
        final int num = Integer.parseInt(line.substring(0, index).trim(), 16);
        final Character character = line.substring(index + 1).trim().charAt(0);

//        System.out.println(character.codepoint);

        return new IndexedChar(num, character);
    }
}
