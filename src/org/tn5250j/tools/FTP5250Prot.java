package org.tn5250j.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.tn5250j.event.FTPStatusEvent;
import org.tn5250j.event.FTPStatusListener;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.tools.filters.FileFieldDef;
import org.tn5250j.tools.filters.OutputFilterInterface;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class FTP5250Prot {

    private FTP5250ProtConnection connection;
    private boolean loggedIn;
    private String hostName;
    private int timeout = 50000;
    private boolean connected;
    private String remoteDir;
    private List<FileFieldDef> ffd;
    private tnvt vt;
    private int recordLength;
    private int recordOutLength;
    private int fileSize;
    private Vector<FTPStatusListener> listeners;
    private FTPStatusEvent status;
    private boolean aborted;
    private char decChar;
    private OutputFilterInterface ofi;
    private List<MemberInfo> members;

    public FTP5250Prot(final tnvt v) {
        vt = v;
        status = new FTPStatusEvent(this);
        // obtain the decimal separator for the machine locale
        final DecimalFormat formatter =
                (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());

        decChar = formatter.getDecimalFormatSymbols().getDecimalSeparator();
    }

    public void setOutputFilter(final OutputFilterInterface o) {
        ofi = o;
    }

    public void setDecimalChar(final char dec) {
        decChar = dec;
    }

    /**
     * Set up ftp sockets and connect to an as400
     *
     * @param host FTP host.
     * @param port FTP port.
     * @return true if successfully connected.
     */
    public boolean connect(final String host, final int port) {

        try {

            hostName = host;
            connection = new FTP5250ProtConnection(host, port, timeout);
            fileSize = 0;

            if (connection.getLastIntResponse() == 220) {
                connected = true;
            } else {
                connected = false;
            }

            return connected;
        } catch (final Exception _ex) {
            return false;
        }

    }

    /**
     * Send quit command to ftp server and close connections
     */
    public void disconnect() {
        if (isConnected()) {
            final FTP5250ProtConnection con = connection;
            AsyncServices.runTask(con::disconnect);
            connected = false;
        }
    }

    /**
     * @return whether or not the system is connected to an AS400 or not
     */
    public boolean isConnected() {

        return connected;
    }

    /**
     * Add a FTPStatusListener to the listener list.
     *
     * @param listener  The FTPStatusListener to be added
     */
    public synchronized void addFTPStatusListener(final FTPStatusListener listener) {

        if (listeners == null) {
            listeners = new java.util.Vector<FTPStatusListener>(3);
        }
        listeners.addElement(listener);

    }

    /**
     * Notify all registered listeners of the FTPStatusEvent.
     *
     */
    private void fireStatusEvent() {

        if (listeners != null) {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                final FTPStatusListener target =
                        listeners.elementAt(i);
                target.statusReceived(status);
            }
        }
    }

    /**
     * Notify all registered listeners of the command status.
     *
     */
    private void fireCommandEvent() {

        if (listeners != null) {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                final FTPStatusListener target =
                        listeners.elementAt(i);
                target.commandStatusReceived(status);
            }
        }
    }

    /**
     * Notify all registered listeners of the file information status.
     *
     */
    private void fireInfoEvent() {

        if (listeners != null) {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                final FTPStatusListener target =
                        listeners.elementAt(i);
                target.fileInfoReceived(status);
            }
        }
    }

    /**
     * Remove a FTPStatusListener from the listener list.
     *
     * @param listener  The FTPStatusListener to be removed
     */
    public synchronized void removeFTPStatusListener(final FTPStatusListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);

    }

    /**
     * Send the user id and password to the connected host
     *
     * @param user  The user name
     * @param password  The password of the user
     * @return true if successfully logged in.
     */
    public boolean login(final String user, final String password) {

        if (connection == null) {
            printFTPInfo("Not connected to any server!");
            return false;
        }
        aborted = false;
        loggedIn = true;

        // send user command to server
        executeCommand("USER", user);

        // send password to server
        final int resp = executeCommand("PASS", password);

        if (resp == 230) {
            loggedIn = true;
        } else {
            loggedIn = false;
            return false;
        }

        // check if the connected to server is an as400 or not
        if (!isConnectedToOS400()) {
            printFTPInfo("Remote server is not an OS/400.  Disconnecting!");
            disconnect();
        }

        getRemoteDirectory();
        return true;
    }

    /**
     * Print out the remote directory of the
     *
     *    not used right now but maybe in the future to obtain a list of
     *    files to select for download
     */
    protected void printDirListing() {

        try {

            // This will create a passive socket and execute the NLST command
            final Socket passSocket = createPassiveSocket("NLST");
            try {

                final BufferedReader br = new BufferedReader(new InputStreamReader(passSocket.getInputStream()));
                String file;
                while ((file = br.readLine()) != null) {
                    System.out.println(file);
                }
            } finally {
                passSocket.close();
            }

            parseResponse();
        } catch (final Exception _ex) {

        }

    }

    /**
     * Checks whether the remote system is an OS400 or not
     */
    private boolean isConnectedToOS400() {

        // get type of system connected to
        executeCommand("SYST");

        // check whether this is an OS/400 system or not
        if (connection.getLastResponse().toUpperCase().indexOf("OS/400") >= 0) return true;
        return false;
    }

    /**
     * @param which field key.
     * @return whether a field is selected for output or not
     */
    public boolean isFieldSelected(final int which) {

        final FileFieldDef ffD = ffd.get(which);
        return ffD.isWriteField();

    }

    /**
     * Select all the fields for output
     */
    protected void selectAll() {

        FileFieldDef f;
        for (int x = 0; x < ffd.size(); x++) {
            f = ffd.get(x);
            f.setWriteField(true);
        }

    }

    /**
     * Unselect all fields for output.  This is a convenience method to unselect
     * all fields for a file that will only need to output a couple of fields
     */
    protected void selectNone() {
        FileFieldDef f;
        for (int x = 0; x < ffd.size(); x++) {
            f = ffd.get(x);
            f.setWriteField(false);
        }

    }

    /**
     * @return whether there are any fields selected or not
     */
    public boolean isFieldsSelected() {

        FileFieldDef f;
        for (int x = 0; x < ffd.size(); x++) {
            f = ffd.get(x);
            if (f.isWriteField())
                return true;
        }
        return false;
    }

    /**
     * Convenience method to select or unselect a field for output
     *
     * @param which field key.
     * @param value field value.
     */
    public void setFieldSelected(final int which, final boolean value) {

        final FileFieldDef ffD = ffd.get(which);
        ffD.setWriteField(value);

    }

    /**
     * Convenience method to return the name of a field
     * @param which field key.
     * @return field value.
     */
    public String getFieldName(final int which) {

        final FileFieldDef ffD = ffd.get(which);
        return ffD.getFieldName();

    }

    /**
     * @return the number of fields in the File Field Definition array of fields
     * returned from the DSPFFD command
     */
    public int getNumberOfFields() {

        return ffd.size();
    }

    /**
     * Returns the remote directy
     */
    private void getRemoteDirectory() {

        executeCommand("PWD");

        final String lastResponse = connection.getLastResponse();
        final int i = lastResponse.indexOf("\"");
        final int j = lastResponse.lastIndexOf("\"");
        if (i != -1 && j != -1)
            remoteDir = lastResponse.substring(i + 1, j);
        else
            remoteDir = "Can't parse remote dir!";
    }

    /**
     * Creates a passive socket to the remote host to allow the transfer of data
     *
     */
    private Socket createPassiveSocket(final String cmd) {

        ServerSocket ss = null;

        try {
            try {

                // The following line does not return the correct address of the
                //    host.  It is a know bug for linux BUG ID 4269403
                //    Since it is a know bug we have to hack the damn thing.
                //    We will use the address from localHost that was obtained
                //    from the ftpConnection socket.

//            byte abyte0[] = InetAddress.getLocalHost().getAddress();
                final byte abyte0[] = connection.getAddress();
                ss = new ServerSocket(0);
                ss.setSoTimeout(timeout);
                final StringBuffer pb = new StringBuffer("PORT ");
                for (int i = 0; i < abyte0.length; i++) {
                    pb.append(abyte0[i] & 0xff);
                    pb.append(",");
                }

                pb.append(ss.getLocalPort() >>> 8 & 0xff);
                pb.append(",");
                pb.append(ss.getLocalPort() & 0xff);
                executeCommand(pb.toString());
                executeCommand(cmd);

                final String lastResponse = connection.getLastResponse();
                if (lastResponse.startsWith("5") || lastResponse.startsWith("4")) {
                    return null;
                }

                final Socket socket = ss.accept();
                socket.setSoTimeout(timeout);
                return socket;
            } catch (final IOException ioexception) {
                printFTPInfo("I/O error while setting up a ServerSocket on the client machine!" + ioexception);
            }
            return null;
        } finally {
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (final IOException ioexception1) {
                printFTPInfo("createPassiveSocket.close() exception!" + ioexception1);
            }
        }
    }

    /**
     * Retrieves the File Field Definitions and Member information for the remote
     *    file to be transferred
     *
     * @param tFile file.
     * @param useInternal use internal.
     * @return true if successfully retrieves file info.
     */
    protected boolean getFileInfo(final String tFile, final boolean useInternal) {

        final int memberOffset = tFile.indexOf(".");
        String file2 = null;
        String member2 = null;

        if (memberOffset > 0) {

//         System.out.println(tFile.substring(0,memberOffset));
            file2 = tFile.substring(0, memberOffset);
            member2 = tFile.substring(memberOffset + 1);
        } else {
            file2 = tFile;
        }

        final String file = file2;
        final String member = member2;
        final boolean internal = useInternal;

        final Runnable getInfo = new Runnable() {

            // set the thread to run.
            @Override
            public void run() {

                executeCommand("RCMD", "dspffd FILE(" + file + ") OUTPUT(*OUTFILE) " +
                        "OUTFILE(QTEMP/FFD) ");

                if (connection.getLastResponse().startsWith("2")) {
                    if (loadFFD(internal)) {
                        if (connection.getLastResponse().startsWith("2")) {
                            if (getMbrInfo(file, member)) {
                                fireInfoEvent();
                            }
                        }
                    }
                }
            }
        };

        final Thread infoThread = new Thread(getInfo);
        infoThread.start();
        return true;

    }

    /**
     * Loads the File Field Definition array with the field information of the
     * remote file
     */
    private boolean loadFFD(final boolean useInternal) {

        Socket socket = null;
        BufferedReader dis = null;
        final String remoteFile = "QTEMP/FFD";
        String recLength = "";
        List<String> allowsNullFields = null;

        try {
            socket = createPassiveSocket("RETR " + remoteFile);
            if (socket == null) {
                return false;
            }


            dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String data;
            if (ffd != null) {
                ffd.clear();
                ffd = null;
            }

            ffd = new ArrayList<>();
            while ((data = dis.readLine()) != null) {
                final FileFieldDef ffDesc = new FileFieldDef(vt, decChar);

                if (useInternal)
                    // WHFLDI  Field name internal
                    ffDesc.setFieldName(data.substring(129, 129 + 10));
                else {

                    String text = "";

                    // first lets check the column headings
                    text = data.substring(261, 261 + 20).trim() + " " +
                            data.substring(281, 281 + 20).trim() + " " +
                            data.substring(301, 301 + 20).trim();

                    if (text.length() > 0)
                        ffDesc.setFieldName(text);
                    else {

                        // WHFLD  Field name text description
                        ffDesc.setFieldName(data.substring(168, 168 + 50).trim());

                        // if the text description is blanks then use the field name
                        if (ffDesc.getFieldName().trim().length() == 0)
                            // WHFLDI  Field name internal
                            ffDesc.setFieldName(data.substring(129, 129 + 10));
                    }
                }


                // WHFOBO  Field starting offset
                ffDesc.setStartOffset(data.substring(149, 149 + 5));
                // WHFLDB  Field length
                ffDesc.setFieldLength(data.substring(159, 159 + 5));
                // WHFLDD  Number of digits
                ffDesc.setNumDigits(data.substring(164, 164 + 2));
                // WHFLDP  Number of decimal positions
                ffDesc.setDecPositions(data.substring(166, 166 + 2));
                // WHFLDT  Field type
                ffDesc.setFieldType(data.substring(321, 321 + 1));
                // WHFTXT  Text description
                ffDesc.setFieldText(data.substring(168, 168 + 50));

                // WHNULL Allow NULL Data
                if (data.substring(503, 503 + 1).equals("Y")) {
                    if (allowsNullFields == null)
                        allowsNullFields = new ArrayList<>(3);
                    allowsNullFields.add(ffDesc.getFieldName());
                    printFTPInfo("Warning -- File allows null fields!!!");
                }

                // set selected
                ffDesc.setWriteField(true);

                recLength = data.substring(124, 124 + 5);

                ffd.add(ffDesc);
            }

            printFTPInfo("Field Information Transfer complete!");

            if (allowsNullFields != null) {
                showNullFieldsWarning(allowsNullFields);
            }

        } catch (final Exception _ex) {
            printFTPInfo("I/O error!");
            return false;
        } finally {
            try {
                socket.close();
            } catch (final Exception _ex) {
            }
            try {
                dis.close();
            } catch (final Exception _ex) {
            }
        }

        int l = 0;
        int o = 0;
        final int r = Integer.parseInt(recLength);
        FileFieldDef f;
        printFTPInfo("<----------------- File Field Information ---------------->");

        for (int x = 0; x < ffd.size(); x++) {
            f = ffd.get(x);
            l += f.getFieldLength();
            o += f.getBufferOutLength();
            printFTPInfo(f.toString());
//         System.out.println(f);
        }
        recordLength = l;
        recordOutLength = o;
        System.out.println(r + " " + l + " " + o);
        parseResponse();
        return true;

    }

    /**
     * @param allowsNullFields list of allowed null fields.
     */
    protected static void showNullFieldsWarning(final List<String> allowsNullFields) {
        final Alert alert = new Alert(AlertType.WARNING, "", ButtonType.OK);
        alert.setTitle("Warning");
        alert.setHeaderText("");

        final BorderPane jp = new BorderPane();
        alert.getDialogPane().setContent(jp);

        final TextArea jta = new TextArea();
        jta.setPrefRowCount(6);
        jta.setPrefColumnCount(30);
        jta.setEditable(false);

        final StringBuilder text = new StringBuilder();

        for (int x = 0; x < allowsNullFields.size(); x++)
            text.append(allowsNullFields.get(x)).append('\n');

        jta.setText(text.toString());

        jp.setTop(new Label(LangTool.getString("messages.nullFieldWarning")));
        jp.setCenter(jta);

        alert.showAndWait();
    }

    /**
     * Executes the command to obtain the member information of the remote file
     *
     * @param file file.
     * @param member member.
     * @return true if successfully gets member info.
     */
    protected boolean getMbrInfo(final String file, final String member) {

        executeCommand("RCMD", "dspfd FILE(" + file + ")" +
                " TYPE(*MBR)" +
                " OUTPUT(*OUTFILE) " +
                "OUTFILE(QTEMP/FML) ");

        if (!connection.getLastResponse().startsWith("2"))
            return false;

        if (getMbrSize(member))

            if (!connection.getLastResponse().startsWith("2"))
                return false;

        return true;
    }

    /**
     * Parses the information obtained by the DSPFD command to obtain the size of
     * the remote file and member.
     */
    private boolean getMbrSize(final String member) {

        boolean flag = true;

        if (connection == null) {
            printFTPInfo("Not connected to any server!");
            return false;
        }
        if (!loggedIn) {
            printFTPInfo("Login was not successful! Aborting!");
            return false;
        }

        Socket socket = null;
        DataInputStream datainputstream = null;
        executeCommand("TYPE", "I");
        final String remoteFile = "QTEMP/FML";
        members = new ArrayList<>(10);

        try {
            socket = createPassiveSocket("RETR " + remoteFile);
            if (socket != null) {
                datainputstream = new DataInputStream(socket.getInputStream());

                final byte abyte0[] = new byte[858];

                int len = 0;
                final StringBuffer sb = new StringBuffer(10);

                printFTPInfo("<----------------- Member Information ---------------->");

                for (int j = 0; j != -1 && !aborted; ) {

                    j = datainputstream.read();
                    if (j == -1)
                        break;

                    abyte0[len++] = (byte) j;

                    if (len == abyte0.length) {
                        sb.setLength(0);

                        // the offset for member name MBNAME is 164 with offset of 1 but
                        //   we have to offset the buffer by 0 which makes it 164 - 1
                        //   or 163
                        for (int f = 0; f < 10; f++) {
                            sb.append(vt.getCodePage().ebcdic2uni(abyte0[163 + f] & 0xff));
                        }

                        printFTPInfo(sb + " " + As400Util.packed2int(abyte0, 345, 5));

                        members.add(new MemberInfo(sb.toString(), As400Util.packed2int(abyte0, 345, 5)));

                        len = 0;

                    }
                }

                printFTPInfo("Member list Transfer complete!");
            } else
                flag = false;

        } catch (final Exception _ex) {
            printFTPInfo("Error! " + _ex);
            return false;
        } finally {
            try {
                socket.close();
            } catch (final Exception _ex) {
            }
        }

        parseResponse();
        return flag;

    }

    /**
     * Convenience method to return the file name and member that is being
     *    transferred
     *  @param tFile file name.
     *  @return full file name from members info.
     */
    public String getFullFileName(final String tFile) {

        final int memberOffset = tFile.indexOf(".");
        String file2 = null;
        String member2 = null;

        if (memberOffset > 0) {
            file2 = tFile.substring(0, memberOffset);
            member2 = tFile.substring(memberOffset + 1);
        } else {
            file2 = tFile;
        }

        if (members != null) {

            if (member2 == null) {
                final MemberInfo mi = members.get(0);
                fileSize = mi.getSize();
                status.setFileLength(mi.getSize());
                member2 = mi.getName();
            } else {

                final Iterator<MemberInfo> i = members.iterator();

                while (i.hasNext()) {
                    final MemberInfo mi = i.next();
                    if (mi.getName().trim().equalsIgnoreCase(member2.trim())) {
                        fileSize = mi.getSize();
                        status.setFileLength(mi.getSize());
//                  System.out.println(" found member " + mi.getName());
                        break;
                    }

                }
            }
        }

        if (member2 != null) return file2.trim() + "." + member2.trim();
        return file2.trim();
    }

    /**
     * Convenience method to return the file size of the file and member that is
     * being transferred
     *
     * @return file size.
     */
    public int getFileSize() {

        return fileSize;
    }

    /**
     * Print output of the help command
     *
     *    Not used just a test method for me
     */
    protected void printHelp() {
        executeCommand("HELP");
    }

    /**
     * Transfer the file information to an output file
     *
     * @param remoteFile remove file.
     * @param localFile local file.
     * @return true if successfully starts file transfer.
     */
    protected boolean getFile(final String remoteFile, final String localFile) {

        if (connection == null) {
            printFTPInfo("Not connected to any server!");
            return false;
        }
        if (!loggedIn) {
            printFTPInfo("Login was not successful! Aborting!");
            return false;
        }

        final String localFileF = localFile;
        final String remoteFileF = remoteFile;

        final Runnable getRun = new Runnable() {

            // set the thread to run.
            @Override
            public void run() {

                Socket socket = null;
                DataInputStream datainputstream = null;
                final String localFileFull = localFileF;
                executeCommand("TYPE", "I");

                try {
                    socket = createPassiveSocket("RETR " + remoteFileF);
                    if (socket != null) {
                        datainputstream = new DataInputStream(socket.getInputStream());

                        writeHeader(localFileFull);

                        final byte abyte0[] = new byte[recordLength];
                        final StringBuffer rb = new StringBuffer(recordOutLength);

                        int c = 0;
                        int len = 0;

                        for (int j = 0; j != -1 && !aborted; ) {

                            j = datainputstream.read();
                            if (j == -1)
                                break;
                            c++;
                            abyte0[len++] = (byte) j;
                            if (len == recordLength) {
                                rb.setLength(0);
                                parseFFD(abyte0, rb);
                                len = 0;

                                status.setCurrentRecord(c / recordLength);
                                fireStatusEvent();
                            }
                            Thread.yield();
                            //            if ((c / recordLength) == 200)
                            //               aborted = true;
                        }
                        if (c == 0) {
                            status.setCurrentRecord(c);
                            fireStatusEvent();
                        } else {
                            if (!aborted)
                                parseResponse();
                        }
                        writeFooter();
                        printFTPInfo("Transfer complete!");
                    }
                } catch (final InterruptedIOException iioe) {
                    printFTPInfo("Interrupted! " + iioe.getMessage());
                } catch (final Exception _ex) {
                    printFTPInfo("Error! " + _ex);
                } finally {
                    try {
                        socket.close();
                    } catch (final Exception _ex) {
                    }
                    try {
                        writeFooter();
                    } catch (final Exception _ex) {
                    }

                    disconnect();
                }
            }
        };

        AsyncServices.runTask(getRun);
        return true;
    }

    /**
     * Parse the field field definition of the data and return a string buffer of
     * the output to be written
     */
    private void parseFFD(final byte[] cByte, final StringBuffer rb) {

        ofi.parseFields(cByte, ffd, rb);
    }

    /**
     * Abort the current file transfer
     */

    public void setAborted() {
        aborted = true;
    }

    public void reSetAborted() {
        aborted = false;
    }

    /**
     * Print ftp command events and responses
     */
    private void printFTPInfo(final String msgText) {

        status.setMessage(msgText);
        fireCommandEvent();

    }

    /**
     * Execute the command without parameters on the remote ftp host
     */

    private int executeCommand(final String cmd) {
        return executeCommand(cmd, null);
    }

    /**
     * Execute a command with parameters on the remote ftp host
     */
    private int executeCommand(final String cmd, final String params) {

        if (connection == null) {
            printFTPInfo("Not connected to any server!");
            return 0;
        }

        if (!loggedIn) {
            printFTPInfo("Login was not successful! Aborting!");
            return 0;
        }

        final int result = connection.executeCommand(cmd, params);

        if (!cmd.equals("PASS"))
            printFTPInfo("SENT: " + cmd + " " + (params != null ? params : ""));
        else
            printFTPInfo("SENT: PASS ****************");

        printFTPInfo(connection.getLastResponse());
        return result;
    }

    private void parseResponse() {
        printFTPInfo(connection.parseResponse());
    }

    /**
     * Write the html header of the output file
     */
    private void writeHeader(final String fileName) throws
            FileNotFoundException {

        ofi.createFileInstance(fileName);

        ofi.writeHeader(fileName, hostName, ffd, decChar);

    }

    /**
     * write the footer of the html output
     */
    private void writeFooter() {
        ofi.writeFooter(ffd);
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    class MemberInfo {

        private String name;
        private int size;

        MemberInfo(final String name, final int size) {

            this.name = name;
            this.size = size;

        }

        public String getName() {
            return name;
        }

        public int getSize() {

            return size;
        }


    }
}
