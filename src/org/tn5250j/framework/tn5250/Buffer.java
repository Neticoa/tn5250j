/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.framework.tn5250;

import java.io.ByteArrayOutputStream;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class Buffer extends ByteArrayOutputStream {

    public Buffer() {
        super();
    }

    public Buffer(final int size) {
        super(size);
    }

    public synchronized void reset(final int toPos) {
        this.count = toPos;
    }

    @Override
    public void write(final byte[] b) {
        write(b, 0, b.length);
    }
}
