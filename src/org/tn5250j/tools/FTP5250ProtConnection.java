/**
 *
 */
package org.tn5250j.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FTP5250ProtConnection {

    private final Socket ftpConnectionSocket;
    private final BufferedReader ftpInputStream;
    private final PrintStream ftpOutputStream;
    private final InetAddress localHost;
    private String lastResponse;
    private int lastIntResponse;

    /**
     * @param host host
     * @param port port
     * @param timeout connection time out.
     * @throws IOException
     *
     */
    public FTP5250ProtConnection(final String host, final int port, final int timeout) throws IOException {
        ftpConnectionSocket = new Socket(host, port);
        ftpConnectionSocket.setSoTimeout(timeout);
        localHost = ftpConnectionSocket.getLocalAddress();
        ftpInputStream = new BufferedReader(new InputStreamReader(ftpConnectionSocket.getInputStream()));
        ftpOutputStream = new PrintStream(ftpConnectionSocket.getOutputStream());

        parseResponse();
    }

    public void disconnect() {
        try {
            try {
                executeCommand("QUIT", null);
            } finally {
                ftpConnectionSocket.close();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return local address.
     */
    public byte[] getAddress() {
        return localHost.getAddress();
    }

    /**
     * @param cmd command.
     * @param params parameters.
     * @return FTP response.
     */
    public int executeCommand(final String cmd, final String params) {
        if (params != null)
            ftpOutputStream.print(cmd + " " + params + "\r\n");
        else
            ftpOutputStream.print(cmd + "\r\n");

        parseResponse();
        return lastIntResponse;
    }
    /**
     * Parse the response returned from the remote host to be used for success
     * or failure of a command
     */
    public String parseResponse() {
        try {

            if (ftpInputStream == null)
                return "0000 Response Invalid";

            String response = ftpInputStream.readLine();
            String response2 = response;
            boolean append = true;

            // we loop until we get a valid numeric response
            while (response2 == null ||
                    response2.length() < 4 ||
                    !Character.isDigit(response2.charAt(0)) ||
                    !Character.isDigit(response2.charAt(1)) ||
                    !Character.isDigit(response2.charAt(2)) ||
                    response2.charAt(3) != ' ') {
                if (append) {
                    response += "\n";
                    append = false;
                }
                response2 = ftpInputStream.readLine();
                response += response2 + "\n";
            }

            // convert the numeric response to an int for testing later
            lastIntResponse = Integer.parseInt(response.substring(0, 3));
            // save off for printing later
            lastResponse = response;
            // print out the response
            return lastResponse;
        } catch (final Exception exception) {
            System.out.println(exception);
            exception.printStackTrace();
            return "0000 Response Invalid";
        }
    }

    /**
     * @return last integer response.
     */
    public int getLastIntResponse() {
        return lastIntResponse;
    }

    /**
     * @return last response.
     */
    public String getLastResponse() {
        return lastResponse;
    }
}
