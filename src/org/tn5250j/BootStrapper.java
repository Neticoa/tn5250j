package org.tn5250j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import org.tn5250j.event.BootEvent;
import org.tn5250j.event.BootListener;
import org.tn5250j.tools.AsyncServices;

public class BootStrapper extends Thread {

    private ServerSocket serverSocket = null;
    boolean listening = true;
    private Vector<BootListener> listeners;

    public BootStrapper() {
        super("BootStrapper");
        try {
            serverSocket = new ServerSocket(3036);
        } catch (final IOException e) {
            System.err.println("Could not listen on port: 3036.");
        }

    }

    @Override
    public void run() {

        System.out.println("BootStrapper listening");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final Socket socket = serverSocket.accept();
                AsyncServices.startTask(() -> {
                    try {
                        final BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()));

                        final BootEvent bootEvent = new BootEvent(this, in.readLine());
                        System.out.println(bootEvent.getNewSessionOptions());
                        return bootEvent;
                    } finally {
                        socket.close();
                    }
                }, this::fireBootEvent, Throwable::printStackTrace);

            } catch (final IOException e) {
                e.printStackTrace();
            }
            System.out.println("got one");
        }
    }

    /**
     * Add a BootListener to the listener list.
     *
     * @param listener The BootListener to be added
     */
    public synchronized void addBootListener(final BootListener listener) {

        if (listeners == null) {
            listeners = new java.util.Vector<BootListener>(3);
        }
        listeners.addElement(listener);

    }

    /**
     * Notify all registered listeners of the BootEvent.
     */
    private void fireBootEvent(final BootEvent bootEvent) {

        if (listeners != null) {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                final BootListener target =
                        listeners.elementAt(i);
                target.bootOptionsReceived(bootEvent);
            }
        }
    }
}
