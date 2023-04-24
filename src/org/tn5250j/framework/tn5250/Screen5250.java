/**
 * Title: Screen5250.java
 * Copyright:   Copyright (c) 2001 - 2004
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j.framework.tn5250;

import static org.tn5250j.TN5250jConstants.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.encoding.ICodePage;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicResolver;
import static org.tn5250j.framework.tn5250.ByteExplainer.SHIFT_IN;
import static org.tn5250j.framework.tn5250.ByteExplainer.SHIFT_OUT;


import javafx.geometry.Rectangle2D;

public class Screen5250 implements Screen5250Facade {

    private static final Logger log = LoggerFactory.getLogger(Screen5250.class);

    // error codes to be sent to the host on an error
    private final static int ERR_CURSOR_PROTECTED = 0x05;
    private final static int ERR_INVALID_SIGN = 0x11;
    private final static int ERR_NO_ROOM_INSERT = 0x12;
    private final static int ERR_NUMERIC_ONLY = 0x09;
    private final static int ERR_DUP_KEY_NOT_ALLOWED = 0x19;
    private final static int ERR_NUMERIC_09 = 0x10;
    private final static int ERR_FIELD_MINUS = 0x16;
    private final static int ERR_FIELD_EXIT_INVALID = 0x18;
    private final static int ERR_ENTER_NO_ALLOWED = 0x20;
    private final static int ERR_MANDITORY_ENTER = 0x21;

    final static byte STATUS_SYSTEM = 1;
    final static byte STATUS_ERROR_CODE = 2;
    final static byte STATUS_VALUE_ON = 1;
    final static byte STATUS_VALUE_OFF = 2;

    final static int initAttr = 32;
    final static char initChar = 0;

    private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();

    private ScreenFields screenFields;
    private int lastAttr;
    private int lastPos;
    private int lenScreen;
    private KeyStrokenizer strokenizer;
    private tnvt sessionVT;
    private int numRows = 0;
    private int numCols = 0;

    public boolean cursorActive = false;
    public boolean cursorShown = false;
    private boolean keyProcessed = false;
    private Rect dirtyScreen = new Rect();

    public int homePos = 0;
    private int saveHomePos = 0;
    private String bufferedKeys;
    private boolean pendingInsert = false;

    private StringBuffer hsMore = new StringBuffer("More...");
    private StringBuffer hsBottom = new StringBuffer("Bottom");

    private boolean guiInterface = false;
    private boolean resetRequired = true;
    private boolean backspaceError = true;
    private boolean feError;

    // vector of listeners for changes to the screen.
    private Vector<ScreenListener> screenListeners = null;

    // Operator Information Area
    private ScreenOIA oia;

    // screen planes
    protected ScreenPlanes planes;

    //Added by Barry
    private StringBuffer keybuf;

    public Screen5250() {

        //Added by Barry
        this.keybuf = new StringBuffer();

        try {

            lastAttr = 32;

            // default number of rows and columns
            numRows = 24;
            numCols = 80;

            setCursor(1, 1); // set initial cursor position

            oia = new ScreenOIA(this);
            oia.setKeyBoardLocked(true);

            lenScreen = numRows * numCols;

            planes = new ScreenPlanes(this, numRows);

            screenFields = new ScreenFields(this);
            strokenizer = new KeyStrokenizer();
        } catch (final Exception ex) {
            log.warn("In constructor: ", ex);
        }
    }

    protected ScreenPlanes getPlanes() {
        return planes;
    }

    public final ScreenOIA getOIA() {
        return oia;
    }

    @Override
    public final void setRowsCols(final int rows, final int cols) {

        final int oldRows = numRows;
        final int oldCols = numCols;

        // default number of rows and columns
        numRows = rows;
        numCols = cols;

        lenScreen = numRows * numCols;

        planes.setSize(rows);

        //  If they are not the same then we need to inform the listeners that
        //  the size changed.
        if (oldRows != numRows || oldCols != numCols)
            fireScreenSizeChanged();

    }


    public boolean isCursorActive() {
        return cursorActive;

    }

    public boolean isCursorShown() {
        return cursorShown;
    }

    @Override
    public void setUseGUIInterface(final boolean gui) {
        guiInterface = gui;
    }

    @Override
    public void toggleGUIInterface() {
        guiInterface = !guiInterface;
    }

    @Override
    public void setResetRequired(final boolean reset) {
        resetRequired = reset;
    }

    @Override
    public void setBackspaceError(final boolean onError) {
        backspaceError = onError;
    }

    /**
     * Copy &amp; Paste support
     *
     * @see #pasteText(String, boolean)
     * @see #copyTextField(int)
     */
    @Override
    public final String copyText(final Rect workR) {
        final StringBuilder sb = new StringBuilder();
        log.debug("Copying " + workR);

        if (workR.width > 0 && workR.height > 0) {
            // loop through all the screen characters to send them to the clip board
            final int maxX = workR.x + workR.width;
            final int maxY = workR.y + workR.height;

            boolean firstLine = true;
            for (int row = workR.y; row <= maxY; row++) {
                if (!firstLine) {
                    sb.append('\n');
                } else {
                    firstLine = false;
                }

                for (int col = workR.x; col <= maxX; col++) {
                    final char c = planes.getChar(getPos(row, col));
                    if (c >= ' ' && (planes.screenExtended[getPos(row, col)] & EXTENDED_5250_NON_DSP)
                            == 0)
                        sb.append(c);
                    else
                        sb.append(' ');
                }
            }
        }

        return sb.toString();
    }

    /**
     * Copy &amp; Paste support
     *
     * @param content text content.
     * @param special is special character text.
     */
    @Override
    public final void pasteText(final String content, final boolean special) {
        if (log.isDebugEnabled()) {
            log.debug("Pasting, special:" + special);
        }
        setCursorActive(false);

        final StringBuilder sb = new StringBuilder(content);
        final StringBuilder pd = new StringBuilder();

        // character counters within the string to be pasted.
        final int nextChar = 0;
        final int nChars = sb.length();

        final int lr = getRow(lastPos);
        final int lc = getCol(lastPos);
        resetDirty(lastPos);

        int cpos = lastPos;
        final int length = getScreenLength();

        char c = 0;
        boolean setIt;

        // save our current place within the FFT.
        screenFields.saveCurrentField();

        for (int x = nextChar; x < nChars; x++) {

            c = sb.charAt(x);

            if ((c == '\n') || (c == '\r')) {

                log.info("pasted cr-lf>" + pd + "<");
                pd.setLength(0);
                // if we read in a cr lf in the data stream we need to go
                // to the starting column of the next row and start from there
                cpos = getPos(getRow(cpos) + 1, lc);

                // If we go paste the end of the screen then let's start over from
                //   the beginning of the screen space.
                if (cpos > length)
                    cpos = 0;
            } else {

                // we will default to set the character always.
                setIt = true;

                // If we are in a special paste scenario then we check for valid
                //   characters to paste.
                if (special && (!Character.isLetter(c) && !Character.isDigit(c)))
                    setIt = false;

                // we will only push a character to the screen space if we are in
                //  a field
                if (isInField(cpos) && setIt) {
                    planes.setChar(cpos, c);
                    setDirty(cpos);
                    screenFields.setCurrentFieldMDT();
                }
                //  If we placed a character then we go to the next position.
                if (setIt)
                    cpos++;
                // we will append the information to our debug buffer.
                pd.append(c);
            }
        }

        // if we have anything else not logged then log it out.
        if (pd.length() > 0)
            log.info("pasted >" + pd + "<");

        // restore out position within the FFT.
        screenFields.restoreCurrentField();
        updateDirty();

        // restore our cursor position.
        setCursor(lr + 1, lc + 1);

        setCursorActive(true);

    }

    /**
     * Copy &amp; Paste support
     *
     * @param position position of text
     * @return text from position.
     */
    @Override
    public final String copyTextField(final int position) {
        screenFields.saveCurrentField();
        isInField(position);
        final String result = screenFields.getCurrentFieldText();
        screenFields.restoreCurrentField();
        return result;
    }

    /**
     * @param formatOption formatting option to use
     * @return vector string of numberic values
     */
    @Override
    public final List<Double> sumThem(final boolean formatOption, final Rectangle2D area) {

        final StringBuilder sb = new StringBuilder();
        final Rect workR = UiUtils.toRect(area);

        log.debug("Summing");

        // obtain the decimal format for parsing
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

        final DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();

        if (formatOption) {
            dfs.setDecimalSeparator('.');
            dfs.setGroupingSeparator(',');
        } else {
            dfs.setDecimalSeparator(',');
            dfs.setGroupingSeparator('.');
        }

        df.setDecimalFormatSymbols(dfs);

        final List<Double> sumVector = new LinkedList<Double>();

        // loop through all the screen characters to send them to the clip board
        int m = workR.x;
        int i = 0;
        int t = 0;

        double sum = 0.0;

        while (workR.height-- > 0) {
            t = workR.width;
            i = workR.y;
            while (t-- > 0) {

                // only copy printable numeric characters (in this case >= ' ')
                //				char c = screen[getPos(m - 1, i - 1)].getChar();
                final char c = planes.getChar(getPos(m - 1, i - 1));
                //				if (((c >= '0' && c <= '9') || c == '.' || c == ',' || c == '-')
                //						&& !screen[getPos(m - 1, i - 1)].nonDisplay) {

                // TODO: update me here to implement the nonDisplay check as well
                if (((c >= '0' && c <= '9') || c == '.' || c == ',' || c == '-')) {
                    sb.append(c);
                }
                i++;
            }

            if (sb.length() > 0) {
                if (sb.charAt(sb.length() - 1) == '-') {
                    sb.insert(0, '-');
                    sb.deleteCharAt(sb.length() - 1);
                }
                try {
                    final Number n = df.parse(sb.toString());
                    sumVector.add(n.doubleValue());
                    sum += n.doubleValue();
                } catch (final ParseException pe) {
                    log.warn(pe.getMessage() + " at "
                            + pe.getErrorOffset());
                }
            }
            sb.setLength(0);
            m++;
        }
        log.debug("" + sum);
        return sumVector;
    }

    /**
     * This will move the screen cursor based on the mouse event.
     *
     * I do not think the checks here for the gui characters should be here but
     * will leave them here for now until we work out the interaction.  This
     * should be up to the gui frontend in my opinion.
     *
     * @param pos new cursor position.
     */
    @Override
    public boolean moveCursor(int pos) {

        if (!oia.isKeyBoardLocked()) {

            if (pos < 0 || pos >= planes.screen.length)
                return false;
            // because getRowColFromPoint returns offset of 1,1 we need to
            //    translate to offset 0,0
            //         pos -= (numCols + 1);

            final int g = planes.getWhichGUI(pos);

            // lets check for hot spots
            if (g >= BUTTON_LEFT && g <= BUTTON_LAST) {
                final StringBuffer aid = new StringBuffer();
                boolean aidFlag = true;
                switch (g) {
                    case BUTTON_RIGHT:
                    case BUTTON_MIDDLE:
                        while (planes.getWhichGUI(--pos) != BUTTON_LEFT) {
                        }
                    case BUTTON_LEFT:
                        if (planes.getChar(pos) == 'F') {
                            pos++;
                        } else
                            aidFlag = false;

                        if (planes.getChar(pos + 1) != '='
                                && planes.getChar(pos + 1) != '.'
                                && planes.getChar(pos + 1) != '/') {
                            aid.append(planes.getChar(pos));
                            aid.append(planes.getChar(pos + 1));
                        } else {
                            log.debug(" Hotspot clicked!!! we will send character "
                                    + planes.getChar(pos));
                            aid.append(planes.getChar(pos));
                        }
                        break;

                }
                if (aidFlag) {
                    switch (g) {

                        case BUTTON_LEFT_UP:
                        case BUTTON_MIDDLE_UP:
                        case BUTTON_RIGHT_UP:
                        case BUTTON_ONE_UP:
                        case BUTTON_SB_UP:
                        case BUTTON_SB_GUIDE:
                            sessionVT.sendAidKey(AID_ROLL_UP);
                            break;

                        case BUTTON_LEFT_DN:
                        case BUTTON_MIDDLE_DN:
                        case BUTTON_RIGHT_DN:
                        case BUTTON_ONE_DN:
                        case BUTTON_SB_DN:
                        case BUTTON_SB_THUMB:

                            sessionVT.sendAidKey(AID_ROLL_DOWN);
                            break;
                        case BUTTON_LEFT_EB:
                        case BUTTON_MIDDLE_EB:
                        case BUTTON_RIGHT_EB:
                            final StringBuffer eb = new StringBuffer();
                            while (planes.getWhichGUI(pos--) != BUTTON_LEFT_EB)
                                ;
                            while (planes.getWhichGUI(pos++) != BUTTON_RIGHT_EB) {
                                eb.append(planes.getChar(pos));
                            }
                            org.tn5250j.tools.system.OperatingSystem.displayURL(eb
                                    .toString());
                            // take out the log statement when we are sure it is
                            // working
                            log.info("Send to external Browser: " + eb.toString());
                            break;

                        default:
                            final int aidKey = Integer.parseInt(aid.toString());
                            if (aidKey >= 1 && aidKey <= 12)
                                sessionVT.sendAidKey(0x30 + aidKey);
                            if (aidKey >= 13 && aidKey <= 24)
                                sessionVT.sendAidKey(0xB0 + (aidKey - 12));
                    }
                } else {
                    if (screenFields.getCurrentField() != null) {
                        final int xPos = screenFields.getCurrentField().startPos();
                        for (int x = 0; x < aid.length(); x++) {
                            //                  System.out.println(sr + "," + (sc + x) + " " +
                            // aid.charAt(x));
                            planes.setChar(xPos + x, aid.charAt(x));
                        }
                        //                  System.out.println(aid);
                        screenFields.setCurrentFieldMDT();
                        sessionVT.sendAidKey(AID_ENTER);
                    }

                }
                // return back to the calling routine that the cursor was not moved
                // but something else here was done like aid keys or the such
                return false;
            }
            // this is a note to not execute this code here when we
            // implement
            //   the remain after edit function option.
            //				if (gui.rubberband.isAreaSelected()) {
            //					gui.rubberband.reset();
            //					gui.repaint();
            //				} else {
            goto_XY(pos);
            isInField(lastPos);

            // return back to the calling object that the cursor was indeed
            //  moved with in the screen object
            return true;
            //				}
        }
        return false;
    }

    @Override
    public void setVT(final tnvt v) {

        sessionVT = v;
    }

    @Override
    public void setPrehelpState(final boolean setErrorCode, final boolean lockKeyboard,
                                   final boolean unlockIfLocked) {
        if (oia.isKeyBoardLocked() && unlockIfLocked)
            oia.setKeyBoardLocked(false);
        else
            oia.setKeyBoardLocked(lockKeyboard);
        bufferedKeys = null;
        oia.setKeysBuffered(false);


    }

    /**
     * Activate the cursor on screen
     *
     * @param activate whether or not the cursor should be active.
     */
    @Override
    public void setCursorActive(final boolean activate) {
        if (cursorActive && !activate) {
            setCursorOff();
            cursorActive = activate;
        } else {
            if (!cursorActive && activate) {
                cursorActive = activate;
                setCursorOn();
            }
        }
    }

    @Override
    public void setCursorOn() {
        cursorShown = true;
        updateCursorLoc();
    }

    @Override
    public void setCursorOff() {
        cursorShown = false;
        updateCursorLoc();
    }

    /**
     *
     */
    private void updateCursorLoc() {
        if (cursorActive) {
            fireCursorChanged(3);
        }
    }

    //Added by Barry
    public String getKeys() {
        final String result = this.keybuf.toString();
        this.keybuf = new StringBuffer();
        return result;
    }

    @Override
    public synchronized void sendKeys(final KeyMnemonic keyMnemonic) {
        sendKeys(keyMnemonic.mnemonic);
    }

    /**
     * The sendKeys method sends a string of keys to the virtual screen. This
     * method acts as if keystrokes were being typed from the keyboard. The
     * keystrokes will be sent to the location given. The string being passed
     * can also contain mnemonic values such as [enter] enter key,[tab] tab key,
     * [pf1] pf1 etc...
     *
     * These will be processed as if you had pressed these keys from the
     * keyboard. All the valid special key values are contained in the MNEMONIC
     * enumeration. See also {@link KeyMnemonic}
     *
     * @param text The string of characters to be sent
     * @see #sendAid
     */
    @Override
    public synchronized void sendKeys(String text) {

        this.keybuf.append(text);

        if (isStatusErrorCode() && !resetRequired) {
            setCursorActive(false);
            simulateMnemonic(KeyMnemonic.RESET.value);
            setCursorActive(true);
        }

        if (oia.isKeyBoardLocked()) {
            if (KeyMnemonic.RESET.mnemonic.equals(text)
                    || KeyMnemonic.SYSREQ.mnemonic.equals(text)
                    || KeyMnemonic.ATTN.mnemonic.equals(text)) {
                setCursorActive(false);
                simulateMnemonic(keyMnemonicResolver.findMnemonicValue(text));
                setCursorActive(true);

            } else {
                if (isStatusErrorCode()) {
                    sessionVT.signalBell();
                    return;
                }

                oia.setKeysBuffered(true);

                if (bufferedKeys == null) {
                    bufferedKeys = text;
                    return;
                }
                bufferedKeys += text;
                return;
            }

        } else {

            if (oia.isKeysBuffered()) {
                if (bufferedKeys != null) {
                    text = bufferedKeys + text;
                }
                //            if (text.length() == 0) {
                oia.setKeysBuffered(false);
                //            }
                bufferedKeys = null;

            }
            // check to see if position is in a field and if it is then change
            //   current field to that field
            isInField(lastPos, true);
            if (text.length() == 1 && !text.equals("[") && !text.equals("]")) {
                setCursorActive(false);
                simulateKeyStroke(text.charAt(0));
                setCursorActive(true);
            } else {

                strokenizer.setKeyStrokes(text);
                String s;
                boolean done = false;

                setCursorActive(false);
                while (!done) {
                    if (strokenizer.hasMoreKeyStrokes()) {

                        // check to see if position is in a field and if it is
                        // then change
                        //   current field to that field
                        isInField(lastPos, true);
                        s = strokenizer.nextKeyStroke();
                        if (s.length() == 1) {
                            simulateKeyStroke(s.charAt(0));
                        } else {
                            simulateMnemonic(keyMnemonicResolver.findMnemonicValue(s));
                        }

                        if (oia.isKeyBoardLocked()) {

                            bufferedKeys = strokenizer
                                    .getUnprocessedKeyStroked();
                            if (bufferedKeys != null) {
                                oia.setKeysBuffered(true);

                            }
                            done = true;
                        }

                    } else {
                        done = true;
                    }
                }
                setCursorActive(true);
            }
        }
    }

    /**
     * The sendAid method sends an "aid" keystroke to the virtual screen. These
     * aid keys can be thought of as special keystrokes, like the Enter key,
     * PF1-24 keys or the Page Up key. All the valid special key values are
     * contained in the AID_ enumeration:
     *
     * @param aidKey
     *            The aid key to be sent to the host
     *
     * @see #sendKeys
     * @see TN5250jConstants#AID_CLEAR
     * @see TN5250jConstants#AID_ENTER
     * @see TN5250jConstants#AID_HELP
     * @see TN5250jConstants#AID_ROLL_UP
     * @see TN5250jConstants#AID_ROLL_DOWN
     * @see TN5250jConstants#AID_ROLL_LEFT
     * @see TN5250jConstants#AID_ROLL_RIGHT
     * @see TN5250jConstants#AID_PRINT
     * @see TN5250jConstants#AID_PF1
     * @see TN5250jConstants#AID_PF2
     * @see TN5250jConstants#AID_PF3
     * @see TN5250jConstants#AID_PF4
     * @see TN5250jConstants#AID_PF5
     * @see TN5250jConstants#AID_PF6
     * @see TN5250jConstants#AID_PF7
     * @see TN5250jConstants#AID_PF8
     * @see TN5250jConstants#AID_PF9
     * @see TN5250jConstants#AID_PF10
     * @see TN5250jConstants#AID_PF11
     * @see TN5250jConstants#AID_PF12
     * @see TN5250jConstants#AID_PF13
     * @see TN5250jConstants#AID_PF14
     * @see TN5250jConstants#AID_PF15
     * @see TN5250jConstants#AID_PF16
     * @see TN5250jConstants#AID_PF17
     * @see TN5250jConstants#AID_PF18
     * @see TN5250jConstants#AID_PF19
     * @see TN5250jConstants#AID_PF20
     * @see TN5250jConstants#AID_PF21
     * @see TN5250jConstants#AID_PF22
     * @see TN5250jConstants#AID_PF23
     * @see TN5250jConstants#AID_PF24
     */
    public void sendAid(final int aidKey) {

        sessionVT.sendAidKey(aidKey);
    }

    /**
     * Restores the error line and sets the error mode off.
     *
     */
    protected void resetError() {

        restoreErrorLine();
        setStatus(STATUS_ERROR_CODE, STATUS_VALUE_OFF, "");

    }

    protected boolean simulateMnemonic(final int mnem) {

        boolean simulated = false;

        switch (mnem) {

            case AID_CLEAR:
            case AID_ENTER:
            case AID_PF1:
            case AID_PF2:
            case AID_PF3:
            case AID_PF4:
            case AID_PF5:
            case AID_PF6:
            case AID_PF7:
            case AID_PF8:
            case AID_PF9:
            case AID_PF10:
            case AID_PF11:
            case AID_PF12:
            case AID_PF13:
            case AID_PF14:
            case AID_PF15:
            case AID_PF16:
            case AID_PF17:
            case AID_PF18:
            case AID_PF19:
            case AID_PF20:
            case AID_PF21:
            case AID_PF22:
            case AID_PF23:
            case AID_PF24:
            case AID_ROLL_DOWN:
            case AID_ROLL_UP:
            case AID_ROLL_LEFT:
            case AID_ROLL_RIGHT:

                if (!screenFields.isCanSendAid()) {
                    displayError(ERR_ENTER_NO_ALLOWED);
                } else
                    sendAid(mnem);
                simulated = true;

                break;
            case AID_HELP:
                sessionVT.sendHelpRequest();
                simulated = true;
                break;

            case AID_PRINT:
                sessionVT.hostPrint(1);
                simulated = true;
                break;

            case BACK_SPACE:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    if (screenFields.getCurrentField().startPos() == lastPos) {
                        if (backspaceError)
                            displayError(ERR_CURSOR_PROTECTED);
                        else {
                            gotoFieldPrev();
                            goto_XY(screenFields.getCurrentField().endPos());
                            updateDirty();
                        }
                    } else {
                        screenFields.getCurrentField().getKeyPos(lastPos);
                        screenFields.getCurrentField().changePos(-1);
                        resetDirty(screenFields.getCurrentField().getCurrentPos());
                        shiftLeft(screenFields.getCurrentField().getCurrentPos());
                        updateDirty();
                        screenFields.setCurrentFieldMDT();

                        simulated = true;
                    }
                } else {
                    displayError(ERR_CURSOR_PROTECTED);

                }
                break;
            case BACK_TAB:

                if (screenFields.getCurrentField() != null
                        && screenFields.isCurrentFieldHighlightedEntry()) {
                    resetDirty(screenFields.getCurrentField().startPos);
                    gotoFieldPrev();
                    updateDirty();
                } else
                    gotoFieldPrev();

                if (screenFields.isCurrentFieldContinued()) {
                    do {
                        gotoFieldPrev();
                    } while (screenFields.isCurrentFieldContinuedMiddle()
                            || screenFields.isCurrentFieldContinuedLast());
                }
                isInField(lastPos);
                simulated = true;
                break;
            case UP:
            case MARK_UP:
                process_XY(lastPos - numCols);
                simulated = true;
                break;
            case DOWN:
            case MARK_DOWN:
                process_XY(lastPos + numCols);
                simulated = true;
                break;
            case LEFT:
            case MARK_LEFT:
                process_XY(lastPos - 1);
                simulated = true;
                break;
            case RIGHT:
            case MARK_RIGHT:
                process_XY(lastPos + 1);
                simulated = true;
                break;
            case NEXTWORD:
                gotoNextWord();
                simulated = true;
                break;
            case PREVWORD:
                gotoPrevWord();
                simulated = true;
                break;
            case DELETE:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    resetDirty(lastPos);
                    screenFields.getCurrentField().getKeyPos(lastPos);
                    shiftLeft(screenFields.getCurrentFieldPos());
                    screenFields.setCurrentFieldMDT();
                    updateDirty();
                    simulated = true;
                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case TAB:

                if (screenFields.getCurrentField() != null
                        && !screenFields.isCurrentFieldContinued()) {
                    if (screenFields.isCurrentFieldHighlightedEntry()) {
                        resetDirty(screenFields.getCurrentField().startPos);
                        gotoFieldNext();
                        updateDirty();
                    } else
                        gotoFieldNext();
                } else {
                    do {
                        gotoFieldNext();
                    } while (screenFields.getCurrentField() != null
                            && (screenFields.isCurrentFieldContinuedMiddle() || screenFields
                            .isCurrentFieldContinuedLast()));
                }

                isInField(lastPos);
                simulated = true;

                break;
            case EOF:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {
                    final int where = endOfField(screenFields.getCurrentField()
                            .startPos(), true);
                    if (where > 0) {
                        setCursor((where / numCols) + 1, (where % numCols) + 1);
                    }
                    simulated = true;
                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }
                resetDirty(lastPos);

                break;
            case ERASE_EOF:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    final int where = lastPos;
                    resetDirty(lastPos);
                    if (fieldExit()) {
                        screenFields.setCurrentFieldMDT();
                        if (!screenFields.isCurrentFieldContinued()) {
                            gotoFieldNext();
                        } else {
                            do {
                                gotoFieldNext();
                                if (screenFields.isCurrentFieldContinued())
                                    fieldExit();
                            } while (screenFields.isCurrentFieldContinuedMiddle()
                                    || screenFields.isCurrentFieldContinuedLast());
                        }
                    }
                    updateDirty();
                    goto_XY(where);
                    simulated = true;

                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case ERASE_FIELD:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    final int where = lastPos;
                    lastPos = screenFields.getCurrentField().startPos();
                    resetDirty(lastPos);
                    if (fieldExit()) {
                        screenFields.setCurrentFieldMDT();
                        if (!screenFields.isCurrentFieldContinued()) {
                            gotoFieldNext();
                        } else {
                            do {
                                gotoFieldNext();
                                if (screenFields.isCurrentFieldContinued())
                                    fieldExit();
                            } while (screenFields.isCurrentFieldContinuedMiddle()
                                    || screenFields.isCurrentFieldContinuedLast());
                        }
                    }
                    updateDirty();
                    goto_XY(where);
                    simulated = true;

                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case INSERT:
                // we toggle it
                oia.setInsertMode(oia.isInsertMode() ? false : true);
                break;
            case HOME:
                // position to the home position set
                if (lastPos + numCols + 1 != homePos) {
                    goto_XY(homePos - numCols - 1);
                    // now check if we are in a field
                    isInField(lastPos);
                } else
                    gotoField(1);
                break;
            case KEYPAD_0:
                simulated = simulateKeyStroke('0');
                break;
            case KEYPAD_1:
                simulated = simulateKeyStroke('1');
                break;
            case KEYPAD_2:
                simulated = simulateKeyStroke('2');
                break;
            case KEYPAD_3:
                simulated = simulateKeyStroke('3');
                break;
            case KEYPAD_4:
                simulated = simulateKeyStroke('4');
                break;
            case KEYPAD_5:
                simulated = simulateKeyStroke('5');
                break;
            case KEYPAD_6:
                simulated = simulateKeyStroke('6');
                break;
            case KEYPAD_7:
                simulated = simulateKeyStroke('7');
                break;
            case KEYPAD_8:
                simulated = simulateKeyStroke('8');
                break;
            case KEYPAD_9:
                simulated = simulateKeyStroke('9');
                break;
            case KEYPAD_PERIOD:
                simulated = simulateKeyStroke('.');
                break;
            case KEYPAD_COMMA:
                simulated = simulateKeyStroke(',');
                break;
            case KEYPAD_MINUS:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    final int s = screenFields.getCurrentField().getFieldShift();
                    if (s == 3 || s == 5 || s == 7) {
                        planes.setChar(lastPos, '-');

                        resetDirty(lastPos);
                        advancePos();
                        if (fieldExit()) {
                            screenFields.setCurrentFieldMDT();
                            if (!screenFields.isCurrentFieldContinued()) {
                                gotoFieldNext();
                            } else {
                                do {
                                    gotoFieldNext();
                                } while (screenFields
                                        .isCurrentFieldContinuedMiddle()
                                        || screenFields
                                        .isCurrentFieldContinuedLast());
                            }
                            simulated = true;
                            updateDirty();
                            if (screenFields.isCurrentFieldAutoEnter())
                                sendAid(AID_ENTER);

                        }
                    } else {
                        displayError(ERR_FIELD_MINUS);

                    }
                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case FIELD_EXIT:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    resetDirty(lastPos);

                    final boolean autoFE = screenFields.isCurrentFieldAutoEnter();

                    if (fieldExit()) {
                        screenFields.setCurrentFieldMDT();
                        if (!screenFields.isCurrentFieldContinued() &&
                                !screenFields.isCurrentFieldAutoEnter()) {
                            gotoFieldNext();
                        } else {
                            do {
                                gotoFieldNext();
                                if (screenFields.isCurrentFieldContinued())
                                    fieldExit();
                            } while (screenFields.isCurrentFieldContinuedMiddle()
                                    || screenFields.isCurrentFieldContinuedLast());
                        }
                    }

                    updateDirty();
                    simulated = true;
                    if (autoFE)
                        sendAid(AID_ENTER);

                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case FIELD_PLUS:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    resetDirty(lastPos);

                    final boolean autoFE = screenFields.isCurrentFieldAutoEnter();
                    if (fieldExit()) {
                        screenFields.setCurrentFieldMDT();
                        if (!screenFields.isCurrentFieldContinued() &&
                                !screenFields.isCurrentFieldAutoEnter()) {
                            gotoFieldNext();
                        } else {
                            do {
                                gotoFieldNext();
                            } while (screenFields.isCurrentFieldContinuedMiddle()
                                    || screenFields.isCurrentFieldContinuedLast());
                        }
                    }
                    updateDirty();
                    simulated = true;

                    if (autoFE)
                        sendAid(AID_ENTER);

                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case FIELD_MINUS:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    final int s = screenFields.getCurrentField().getFieldShift();
                    if (s == 3 || s == 5 || s == 7) {
                        planes.setChar(lastPos, '-');

                        resetDirty(lastPos);
                        advancePos();

                        final boolean autoFE = screenFields.isCurrentFieldAutoEnter();

                        if (fieldExit()) {
                            screenFields.setCurrentFieldMDT();
                            if (!screenFields.isCurrentFieldContinued()
                                    && !screenFields.isCurrentFieldAutoEnter()) {
                                gotoFieldNext();
                            } else {
                                do {
                                    gotoFieldNext();
                                }
                                while (screenFields.isCurrentFieldContinuedMiddle()
                                        || screenFields.isCurrentFieldContinuedLast());
                            }
                        }
                        updateDirty();
                        simulated = true;
                        if (autoFE)
                            sendAid(AID_ENTER);

                    } else {
                        displayError(ERR_FIELD_MINUS);

                    }
                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case BOF:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {
                    final int where = screenFields.getCurrentField().startPos();
                    if (where > 0) {
                        goto_XY(where);
                    }
                    simulated = true;
                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }
                resetDirty(lastPos);

                break;
            case SYSREQ:
                sessionVT.systemRequest();
                simulated = true;
                break;
            case RESET:
                if (isStatusErrorCode()) {
                    resetError();
                    isInField(lastPos);
                    updateDirty();
                } else {
                    setPrehelpState(false, oia.isKeyBoardLocked(), false);
                }
                simulated = true;
                break;
            case ATTN:
                sessionVT.sendAttentionKey();
                simulated = true;
                break;
            case DUP_FIELD:
                if (screenFields.getCurrentField() != null
                        && screenFields.withinCurrentField(lastPos)
                        && !screenFields.isCurrentFieldBypassField()) {

                    if (screenFields.isCurrentFieldDupEnabled()) {
                        resetDirty(lastPos);
                        screenFields.getCurrentField().setFieldChar(lastPos,
                                (char) 0x1C);
                        screenFields.setCurrentFieldMDT();
                        gotoFieldNext();
                        updateDirty();
                        simulated = true;
                    } else {
                        displayError(ERR_DUP_KEY_NOT_ALLOWED);
                    }
                } else {
                    displayError(ERR_CURSOR_PROTECTED);
                }

                break;
            case NEW_LINE:
                if (screenFields.getSize() > 0) {
                    int startRow = getRow(lastPos) + 1;
                    final int startPos = lastPos;

                    if (startRow == getRows())
                        startRow = 0;

                    setCursor(++startRow, 1);

                    if (!isInField() && screenFields.getCurrentField() != null
                            && !screenFields.isCurrentFieldBypassField()) {
                        while (!isInField()
                                && screenFields.getCurrentField() != null
                                && !screenFields.isCurrentFieldBypassField()) {

                            // lets keep going
                            advancePos();

                            // Have we looped the screen?
                            if (lastPos == startPos) {
                                // if so then go back to starting point
                                goto_XY(startPos);
                                break;
                            }
                        }
                    }
                }
                simulated = true;
                break;
            case FAST_CURSOR_DOWN:
                int rowNow = (getCurrentRow() - 1) + 3;
                if (rowNow > getRows() - 1)
                    rowNow = rowNow - getRows();
                this.goto_XY(getPos(rowNow, getCurrentCol() - 1));
                simulated = true;
                break;
            case FAST_CURSOR_UP:
                rowNow = (getCurrentRow() - 1) - 3;
                if (rowNow < 0)
                    rowNow = (getRows()) + rowNow;
                this.goto_XY(getPos(rowNow, getCurrentCol() - 1));
                simulated = true;
                break;
            case FAST_CURSOR_LEFT:
                int colNow = (getCurrentCol() - 1) - 3;
                rowNow = getCurrentRow() - 1;
                if (colNow <= 0) {
                    colNow = getColumns() + colNow;
                    rowNow--;
                }
                if (rowNow < 0)
                    rowNow = getRows() - 1;

                process_XY(getPos(rowNow, colNow));
                simulated = true;
                break;
            case FAST_CURSOR_RIGHT:
                colNow = (getCurrentCol() - 1) + 3;
                rowNow = getCurrentRow() - 1;
                if (colNow >= getColumns()) {
                    colNow = colNow - getColumns();
                    rowNow++;
                }
                if (rowNow > getRows() - 1)
                    rowNow = getRows() - rowNow;

                process_XY(getPos(rowNow, colNow));
                simulated = true;
                break;
            default:
                log.info(" Mnemonic not supported " + mnem);
                break;

        }

        return simulated;
    }

    protected boolean simulateKeyStroke(char c) {

        if (isStatusErrorCode() && !Character.isISOControl(c) && !keyProcessed) {
            if (resetRequired) return false;
            resetError();
        }

        boolean updateField = false;
        boolean numericError = false;
        boolean updatePos = false;
        boolean autoEnter = false;

        if (!Character.isISOControl(c)) {

            if (screenFields.getCurrentField() != null
                    && screenFields.withinCurrentField(lastPos)
                    && !screenFields.isCurrentFieldBypassField()) {

                if (screenFields.isCurrentFieldFER()
                        && !screenFields.withinCurrentField(screenFields
                        .getCurrentFieldPos())
                        && lastPos == screenFields.getCurrentField().endPos()
                        && screenFields.getCurrentFieldPos() > screenFields
                        .getCurrentField().endPos()) {

                    displayError(ERR_FIELD_EXIT_INVALID);
                    feError = true;
                    return false;
                }

                switch (screenFields.getCurrentFieldShift()) {
                    case 0: // Alpha shift
                    case 2: // Numeric Shift
                    case 4: // Katakana Shift
                        updateField = true;
                        break;
                    case 1: // Alpha Only
                        if (Character.isLetter(c) || c == ',' || c == '-'
                                || c == '.' || c == ' ')
                            updateField = true;
                        break;
                    case 3: // Numeric only
                        if (Character.isDigit(c) || c == '+' || c == ','
                                || c == '-' || c == '.' || c == ' ')
                            updateField = true;
                        else
                            numericError = true;
                        break;
                    case 5: // Digits only
                        if (Character.isDigit(c))
                            updateField = true;
                        else
                            displayError(ERR_NUMERIC_09);
                        break;
                    case 7: // Signed numeric
                        if (Character.isDigit(c) || c == '+' || c == '-')
                            if (lastPos == screenFields.getCurrentField().endPos()
                                    && (c != '+' && c != '-'))
                                displayError(ERR_INVALID_SIGN);
                            else
                                updateField = true;
                        else
                            displayError(ERR_NUMERIC_09);
                        break;
                }

                if (updateField) {
                    if (screenFields.isCurrentFieldToUpper())
                        c = Character.toUpperCase(c);

                    updatePos = true;
                    resetDirty(lastPos);

                    if (oia.isInsertMode()) {
                        if (endOfField(false) != screenFields.getCurrentField()
                                .endPos())
                            shiftRight(lastPos);
                        else {

                            displayError(ERR_NO_ROOM_INSERT);
                            updatePos = false;
                        }

                    }

                    if (updatePos) {
                        screenFields.getCurrentField().getKeyPos(
                                getRow(lastPos), getCol(lastPos));
                        screenFields.getCurrentField().changePos(1);

                        planes.setChar(lastPos, c);

                        screenFields.setCurrentFieldMDT();

                        // if we have gone passed the end of the field then goto
                        // the next field
                        if (!screenFields.withinCurrentField(screenFields
                                .getCurrentFieldPos())) {
                            if (screenFields.isCurrentFieldAutoEnter()) {
                                autoEnter = true;
                            } else if (!screenFields.isCurrentFieldFER())
                                gotoFieldNext();
                            else {
                                //                        screenFields.getCurrentField().changePos(1);
                                //
                                //                        if (screenFields.
                                //                        cursorPos == endPos)
                                //                           System.out.println("end of field");
                                //
                                //                        feError != feError;
                                //                        if (feError)
                                //                           displayError(ERR_FIELD_EXIT_INVALID);
                            }

                        } else
                            setCursor(screenFields.getCurrentField()
                                    .getCursorRow() + 1, screenFields
                                    .getCurrentField().getCursorCol() + 1);

                    }

                    fireScreenChanged(1);

                    if (autoEnter)
                        sendAid(AID_ENTER);
                } else {
                    if (numericError) {
                        displayError(ERR_NUMERIC_ONLY);
                    }
                }
            } else {
                displayError(ERR_CURSOR_PROTECTED);
            }

        }
        return updatePos;
    }

    /**
     * Method: endOfField
     * <p>
     *
     * convenience method that call endOfField with lastRow lastCol and passes
     * the posSpace to that method
     *
     * @param posSpace
     *            value of type boolean - specifying to return the position of
     *            the the last space or not
     * @return a value of type int - the screen postion (row * columns) + col
     *
     */
    private int endOfField(final boolean posSpace) {
        return endOfField(lastPos, posSpace);
    }

    /**
     * Method: endOfField
     * <p>
     *
     * gets the position of the last character of the current field posSpace
     * parameter tells the routine whether to return the position of the last
     * space ( <= ' ') or the last non space posSpace == true last occurrence of
     * char <= ' ' posSpace == false last occurrence of char > ' '
     *
     * @param pos
     *            value of type int - position to start from
     * @param posSpace
     *            value of type boolean - specifying to return the position of
     *            the the last space or not
     * @return a value of type int - the screen postion (row * columns) + col
     *
     */
    private int endOfField(final int pos, final boolean posSpace) {

        int endPos = screenFields.getCurrentField().endPos();
        final int fePos = endPos;
        // get the number of characters to the right
        int count = endPos - pos;

        // first lets get the real ending point without spaces and the such
        while (planes.getChar(endPos) <= ' ' && count-- > 0) {

            endPos--;
        }

        if (endPos == fePos) {

            return endPos;

        }
        screenFields.getCurrentField().getKeyPos(endPos);
        if (posSpace) screenFields.getCurrentField().changePos(+1);
        return screenFields.getCurrentFieldPos();

    }

    private boolean fieldExit() {

        int pos = lastPos;
        boolean mdt = false;
        final int end = endOfField(false); // get the ending position of the first
        // non blank character in field

        final ScreenField sf = screenFields.getCurrentField();

        if (sf.isMandatoryEnter() && end == sf.startPos()) {
            displayError(ERR_MANDITORY_ENTER);
            return false;
        }

        // save off the current pos of the field for checking field exit required
        //   positioning.  the getKeyPos resets this information so it is useless
        //   for comparing if we are positioned passed the end of field.
        //   Maybe this should be changed to not update the current cursor position
        //   of the field.
        final int currentPos = sf.getCurrentPos();

        // get the number of characters to the right
        int count = (end - sf.startPos()) - sf.getKeyPos(pos);

        if (count == 0 && sf.isFER()) {
            if (currentPos > sf.endPos()) {
                mdt = true;
                return mdt;
            }
        }

        for (; count >= 0; count--) {
            planes.setChar(pos, initChar);
            setDirty(pos);
            pos++;
            mdt = true;
        }

        // This checks for a field minus because a field minus places
        // a negative sign and then advances a position. If it is the
        // end of the field where the minus is placed then this offset will
        //  place the count as -1.
        if (count == -1) {
            final int s = sf.getFieldShift();
            if (s == 3 || s == 5 || s == 7) {
                mdt = true;
            }
        }

        final int adj = sf.getAdjustment();

        if (adj != 0) {

            switch (adj) {

                case 5:
                    rightAdjustField('0');
                    sf.setRightAdjusted();
                    break;
                case 6:
                    rightAdjustField(' ');
                    sf.setRightAdjusted();

                    break;
                case 7:
                    sf.setManditoryEntered();
                    break;

            }
        } else {

            // we need to right adjust signed numeric fields as well.
            if (sf.isSignedNumeric()) {
                rightAdjustField(' ');
            }
        }

        return mdt;
    }

    private void rightAdjustField(final char fill) {

        final int end = endOfField(false); // get the ending position of the first
        // non blank character in field

        // get the number of characters to the right
        int count = screenFields.getCurrentField().endPos() - end;

        // subtract 1 from count for signed numeric - note for later
        if (screenFields.getCurrentField().isSignedNumeric()) {
            if (planes.getChar(end - 1) != '-')
                count--;
        }

        final int pos = screenFields.getCurrentField().startPos();

        while (count-- >= 0) {

            shiftRight(pos);
            planes.setChar(pos, fill);

            setDirty(pos);

        }

    }

    private void shiftLeft(final int sPos) {

        int endPos = 0;

        int pos = sPos;
        int pPos = sPos;

        ScreenField sf = screenFields.getCurrentField();
        int end;
        int count;
        do {
            end = endOfField(pPos, false); // get the ending position of the
            // first
            // non blank character in field

            count = (end - screenFields.getCurrentField().startPos())
                    - screenFields.getCurrentField().getKeyPos(pPos);

            // now we loop through and shift the remaining characters to the
            // left
            while (count-- > 0) {
                pos++;
                planes.setChar(pPos, planes.getChar(pos));
                setDirty(pPos);
                pPos = pos;

            }

            if (screenFields.isCurrentFieldContinued()) {
                gotoFieldNext();
                if (screenFields.getCurrentField().isContinuedFirst())
                    break;

                pos = screenFields.getCurrentField().startPos();
                planes.setChar(pPos, planes.getChar(pos));
                setDirty(pPos);

                pPos = pos;

            }
        } while (screenFields.isCurrentFieldContinued()
                && !screenFields.getCurrentField().isContinuedFirst());

        if (end >= 0 && count >= -1) {

            endPos = end;
        } else {
            endPos = sPos;

        }

        screenFields.setCurrentField(sf);
        planes.setChar(endPos, initChar);
        setDirty(endPos);
        goto_XY(screenFields.getCurrentFieldPos());
        sf = null;

    }

    private void shiftRight(final int sPos) {

        final int end = endOfField(true); // get the ending position of the first
        // non blank character in field
        int pos = end;
        int pPos = end;

        int count = end - sPos;

        // now we loop through and shift the remaining characters to the right
        while (count-- > 0) {

            pos--;
            planes.setChar(pPos, planes.getChar(pos));
            setDirty(pPos);

            pPos = pos;
        }
    }

    @Override
    public int getRow(final int pos) {

        //      if (pos == 0)
        //         return 1;

        int row = pos / numCols;

        if (row < 0) {

            row = lastPos / numCols;
        }
        if (row > (lenScreen / numCols) - 1)
            row = (lenScreen / numCols) - 1;

        return row;

    }

    @Override
    public int getCol(final int pos) {
        final int col = pos % (getColumns());
        if (col > 0) return col;
        return 0;
    }

    /**
     * This routine is 0 based offset. So to get row 20,1 then pass row 19,0
     *
     * @param row row number.
     * @param col column number.
     * @return caret position.
     */
    @Override
    public int getPos(final int row, final int col) {

        return (row * numCols) + col;
    }

    /**
     * Current position is based on offsets of 1,1 not 0,0 of the current
     * position of the screen
     *
     * @return int
     */
    @Override
    public int getCurrentPos() {

        //		return lastPos + numCols + 1;
        return lastPos + 1;

    }

    /**
     * I got this information from a tcp trace of each error. I could not find
     * any documenation for this. Maybe there is but I could not find it. If
     * anybody finds this documention could you please send me a copy. Please
     * note that I did not look that hard either.
     * <p>
     * 0000: 00 50 73 1D 89 81 00 50 DA 44 C8 45 08 00 45 00 .Ps....P.D.E..E.
     * </p>
     * <p>
     * 0010: 00 36 E9 1C 40 00 80 06 9B F9 C1 A8 33 58 C0 A8 .6..@...k....3X..
     * </p>
     * <p>
     * 0020: C0 02 06 0E 00 17 00 52 6E 88 73 40 DE CB 50 18 .......Rn.s@..P.
     * </p>
     * <p>
     * 0030: 20 12 3C 53 00 00 00 0C 12 A0 00 00 04 01 00 00 . <S............
     * </p>
     * <p>
     * 0040: 00 05 FF EF .... ----------|| The 00 XX is the code to be sent. I
     * found the following <table BORDER COLS=2 WIDTH="50%" >
     * <tr>
     * <td>ERR_CURSOR_PROTECTED</td>
     * <td>0x05</td>
     * </tr>
     * <tr>
     * <td>ERR_INVALID_SIGN</td>
     * <td>0x11</td>
     * </tr>
     * <tr>
     * <td>ERR_NO_ROOM_INSERT</td>
     * <td>0x12</td>
     * </tr>
     * <tr>
     * <td>ERR_NUMERIC_ONLY</td>
     * <td>0x09</td>
     * </tr>
     * <tr>
     * <td>ERR_NUMERIC_09</td>
     * <td>0x10</td>
     * </tr>
     * <tr>
     * <td>ERR_FIELD_MINUS</td>
     * <td>0x16</td>
     * </tr>
     * <tr>
     * <td>ERR_ENTER_NOT_ALLOWED</td>
     * <td>0x20</td>
     * </tr>
     * <tr>
     * <td>ERR_MANDITORY_ENTER</td>
     * <td>0x21</td>
     * </tr>
     * <tr>
     * <td>ERR_ENTER_NOT_ALLOWED</td>
     * <td>0x20</td>
     * </tr>
     * </table> I am tired of typing and they should be self explanitory. Finding
     * them in the first place was the pain.
     * </p>
     *
     * @param ec error code
     */
    private void displayError(final int ec) {
        saveHomePos = homePos;
        homePos = lastPos + numCols + 1;
        pendingInsert = true;
        sessionVT.sendNegResponse2(ec);

    }

    private void process_XY(int pos) {

        if (pos < 0)
            pos = lenScreen + pos;
        if (pos > lenScreen - 1)
            pos = pos - lenScreen;

        // if there was a field exit error then we need to treat the movement
        //  of the cursor in a special way that equals that of Client Access.
        //    If the cursor is moved from the field then we need to reset the
        //       position within the field so that the last character can be typed
        //       over again instead of sending the field exit error again.
        //       We also need to reset the field exit error flag.
        //
        //    How we know we have a field exit error is when the field position is
        //    set beyond the end of the field and a character is then typed we can
        //    not position that character. To reset this we need to set the next
        //    position of the field to not be beyond the end of field but to the
        //    last character.
        //
        //    Now to make it work like Client Access if the cursor is a back space
        //    then do not move the cursor but place it on the last field. All
        //    other keys will reset the field position so that entering over the
        //    last character will not cause an error but replace that character or
        //    just plain move the cursor if the key was to do that.

        final ScreenField sf = screenFields.getCurrentField();
        if (feError) {
            feError = false;
            sf.changePos(-1);
        } else {
            if (sf != null && sf.isFER()) {
                if ((sf.getCurrentPos()
                        > sf.endPos())) {
                    if (sf.withinField(pos)) {
                        sf.getKeyPos(pos);
                        return;
                    }
                    sf.getKeyPos(sf.endPos());
                }
            }

            goto_XY(pos);
        }
    }

    @Override
    public boolean isUsingGuiInterface() {

        return guiInterface;
    }

    /**
     * Convinience class to return if the cursor is in a field or not.
     *
     * @return true or false
     */

    @Override
    public boolean isInField() {

        return isInField(lastPos, true);
    }

    /**
     *
     * Convinience class to return if the position that is passed is in a field
     * or not. If it is then the chgToField parameter will change the current
     * field to this field where the position indicates
     *
     * @param pos position.
     * @param chgToField whether or not should change current position.
     * @return true or false
     */
    @Override
    public boolean isInField(final int pos, final boolean chgToField) {

        return screenFields.isInField(pos, chgToField);
    }

    /**
     *
     * Convinience class to return if the position that is passed is in a field
     * or not. If it is then the field at this position becomes the current
     * working field
     *
     * @param pos position.
     * @return true or false
     */
    public boolean isInField(final int pos) {

        return screenFields.isInField(pos, true);
    }

    /**
     * Convinience class to return if the position at row and column that is
     * passed is in a field or not. If it is then the field at this position
     * becomes the current working field.
     *
     * @param row row.
     * @param col column.
     * @return true or false
     */
    public boolean isInField(final int row, final int col) {

        return isInField(row, col, true);
    }

    /**
     *
     * Convinience class to return if the position at row and column that is
     * passed is in a field or not. If it is then the chgToField parameter will
     * change the current field to this field where the row and column
     * indicates.
     *
     * @param row row.
     * @param col column.
     * @param chgToField change current position.
     * @return true or false
     */
    public boolean isInField(final int row, final int col, final boolean chgToField) {
        return screenFields.isInField((row * numCols) + col, chgToField);
    }

    /**
     * Gets the length of the screen - number of rows times number of columns
     *
     * @return int value of screen length
     */
    @Override
    public int getScreenLength() {

        return lenScreen;
    }

    /**
     * Get the number or rows available.
     *
     * @return number of rows
     */
    @Override
    public int getRows() {

        return numRows;

    }

    /**
     * Get the number of columns available.
     *
     * @return number of columns
     */
    @Override
    public int getColumns() {

        return numCols;

    }

    /**
     * Get the current row where the cursor is
     *
     * @return the cursor current row position 1,1 based
     */
    @Override
    public int getCurrentRow() {

        return (lastPos / numCols) + 1;

    }

    /**
     * Get the current column where the cursor is
     *
     * @return the cursor current column position 1,1 based
     */
    @Override
    public int getCurrentCol() {

        return (lastPos % numCols) + 1;

    }

    /**
     * The last position of the cursor on the screen - Note - position is based
     * 0,0
     *
     * @return last position
     */
    @Override
    public int getLastPos() {

        return lastPos;

    }

    /**
     * Hotspot More... string
     *
     * @return string literal of More...
     */
    @Override
    public StringBuffer getHSMore() {
        return hsMore;
    }

    /**
     * Hotspot Bottom string
     *
     * @return string literal of Bottom
     */
    @Override
    public StringBuffer getHSBottom() {
        return hsBottom;
    }

    /**
     * Return the whole screen represented as a character array
     *
     * @return character array containing the text
     *
     * Added by Luc - LDC
     *
     * Note to KJP - Have to ask what the difference is between this method and
     * the other
     */
    @Override
    public char[] getScreenAsAllChars() {
        final char[] sac = new char[lenScreen];
        char c;

        for (int x = 0; x < lenScreen; x++) {
            c = planes.getChar(x);
            // only draw printable characters (in this case >= ' ')
            if ((c >= ' ') && (!planes.isAttributePlace(x))) {
                sac[x] = c;
                // TODO: implement the underline check here
                //				if (screen[x].underLine && c <= ' ')
                //					sac[x] = '_';
            } else
                sac[x] = ' ';
        }

        return sac;
    }

    /**
     *
     * Return the screen represented as a character array
     *
     * @return character array containing the text
     */
    @Override
    public char[] getScreenAsChars() {
        final char[] sac = new char[lenScreen];
        char c;

        for (int x = 0; x < lenScreen; x++) {
            c = planes.getChar(x);
            // only draw printable characters (in this case >= ' ')
            if ((c >= ' ') && (!planes.isAttributePlace(x))) {
                sac[x] = c;
                // TODO: implement the underline check here
                //				if (screen[x].underLine && c <= ' ')
                //					sac[x] = '_';
            } else
                sac[x] = ' ';
        }

        return sac;
    }

    public char[] getData(final int startRow, final int startCol, final int endRow, final int endCol, final int plane) {
        try {
            int from = getPos(startRow, startCol);
            int to = getPos(endRow, endCol);
            if (from > to) {

                final int f = from;
                to = f;
                from = f;
            }
            return planes.getPlaneData(from, to, plane);
        } catch (final Exception oe) {
            return null;
        }

    }

    /**
     * <p>
     *  GetScreen retrieves the various planes associated with the presentation
     *  space. The data is returned as a linear array of character values in the
     *  array provided. The array is not terminated by a null character except
     *  when data is retrieved from the text plane, in which case a single null
     *  character is appended.
     *  </p>
     *  <p>
     *  The application must supply a buffer for the returned data and the length
     *  of the buffer. Data is returned starting from the beginning of the
     *  presentation space and continuing until the buffer is full or the entire
     *  plane has been copied. For text plane data, the buffer must include one
     *  extra position for the terminating null character.
     *  <p>
     *
     * @param buffer character buffer.
     * @param bufferLength buffer length.
     * @param plane plane.
     * @return The number of characters copied to the buffer
     */
    @Override
    public synchronized int GetScreen(final char buffer[], final int bufferLength, final int plane) {
        return GetScreen(buffer, bufferLength, 0, lenScreen, plane);
    }

    /**
     * <p>
     *  GetScreen retrieves the various planes associated with the presentation
     *  space. The data is returned as a linear array of character values in the
     *  array provided. The array is not terminated by a null character except
     *  when data is retrieved from the text plane, in which case a single null
     *  character is appended.
     * </p>
     * <p>
     * The application must supply a buffer for the returned data and the length
     * of the buffer. Data is returned starting from the given position and
     * continuing until the specified number of characters have been copied, the
     * buffer is full or the entire plane has been copied. For text plane data,
     * the buffer must include one extra position for the terminating null character.
     * </p>
     *
     * @param buffer character buffer.
     * @param bufferLength buffer length.
     * @param from from position.
     * @param length data length.
     * @param plane plane.
     * @return The number of characters copied to the buffer
     */
    public synchronized int GetScreen(final char buffer[], final int bufferLength, final int from, final int length, final int plane) {
        return planes.GetScreen(buffer, bufferLength, from, length, plane);
    }

    /**
     * <p>
     *  GetScreen retrieves the various planes associated with the presentation
     *  space. The data is returned as a linear array of character values in the
     *  array provided. The array is not terminated by a null character except
     *  when data is retrieved from the text plane, in which case a single null
     *  character is appended.
     *  </p>
     *  <p>
     *  The application must supply a buffer for the returned data and the length
     *  of the buffer. Data is returned starting from the given coordinates and
     *  continuing until the specified number of characters have been copied,
     *  the buffer is full, or the entire plane has been copied. For text plane
     *  data, the buffer must include one extra position for the terminating null
     *  character.
     *  </p>
     *
     * @param buffer character buffer.
     * @param bufferLength buffer length.
     * @param row row.
     * @param col column.
     * @param length data length.
     * @param plane plane.
     * @return The number of characters copied to the buffer.
     */
    public synchronized int GetScreen(final char buffer[], final int bufferLength, final int row, final int col, final int length, final int plane) {
        // Call GetScreen function after converting row and column to
        // a position.
        return planes.GetScreen(buffer, bufferLength, row, col, length, plane);
    }

    /**
     * <p>
     *  GetScreenRect retrieves data from the various planes associated with the
     *  presentation space. The data is returned as a linear array of character
     *  values in the buffer provided.
     *  </p>
     *
     * <p>
     * The application supplies two positions that represent opposing corners of
     * a rectangle within the presentation space. The starting and ending
     * positions can have any spatial relationship to each other. The data
     * returned starts from the row containing the upper-most point to the row
     * containing the lower-most point, and from the left-most column to the
     * right-most column.
     * </p>
     * <p>
     * The specified buffer must be at least large enough to contain the number
     * of characters in the rectangle. If the buffer is too small, no data is
     * copied and zero is returned by the method. Otherwise, the method returns
     * the number of characters copied.
     * </p>
     *
     * @param buffer character buffer.
     * @param bufferLength buffer length.
     * @param startPos start position.
     * @param endPos end position.
     * @param plane plane.
     * @return The number of characters copied to the buffer
     */
    public synchronized int GetScreenRect(final char buffer[], final int bufferLength, final int startPos, final int endPos, final int plane) {
        return planes.GetScreenRect(buffer, bufferLength, startPos, endPos, plane);

    }

    /**
     * <p>
     *  GetScreenRect retrieves data from the various planes associated with the
     *  presentation space. The data is returned as a linear array of character
     *  values in the buffer provided. The buffer is not terminated by a null
     *  character.
     * </p>
     * <p>
     * The application supplies two coordinates that represent opposing corners
     * of a rectangle within the presentation space. The starting and ending
     * coordinates can have any spatial relationship to each other. The data
     * returned starts from the row containing the upper-most point to the row
     * containing the lower-most point, and from the left-most column to the
     * right-most column.
     * </p>
     * <p>
     * The specified buffer must be at least large enough to contain the number
     * of characters in the rectangle. If the buffer is too small, no data is
     * copied and zero is returned by the method. Otherwise, the method returns
     * the number of characters copied.
     * </p>
     *
     * @param buffer character buffer.
     * @param bufferLength buffer length.
     * @param startRow start row.
     * @param startCol start column.
     * @param endRow end row.
     * @param endCol end column.
     * @param plane plane.
     * @return The number characters copied to the buffer
     */
    @Override
    public synchronized int GetScreenRect(final char buffer[], final int bufferLength,
                                          final int startRow, final int startCol, final int endRow, final int endCol, final int plane) {

        return planes.GetScreenRect(buffer, bufferLength, startRow, startCol, endRow,
                endCol, plane);
    }

    public synchronized boolean[] getActiveAidKeys() {
        return sessionVT.getActiveAidKeys();
    }

    protected synchronized void setScreenData(final String text, final int location) {

        if (location < 0 || location > lenScreen) {
            return;
        }

        final int l = text.length();
        boolean updated = false;
        boolean flag = false;
        int x = 0;
        for (; x < l; x++) {
            if (isInField(location + x, true)) {
                if (!screenFields.getCurrentField().isBypassField()) {
                    if (!flag) {
                        screenFields.getCurrentField().setMDT();
                        updated = true;
                        resetDirty(location + x);
                        screenFields.setMasterMDT();
                        flag = true;
                    }

                    planes.screen[location + x] = text.charAt(x);
                    setDirty(location + x);
                }
            }

        }
        lastPos = location + x;
        if (updated) {
            fireScreenChanged(1);
        }

    }

    /**
     * This routine is based on offset 1,1 not 0,0 it will translate to offset
     * 0,0 and call the goto_XY(int pos) it is mostly used from external classes
     * that use the 1,1 offset
     *
     * @param row row.
     * @param col column.
     */
    @Override
    public void setCursor(final int row, final int col) {
        goto_XY(((row - 1) * numCols) + (col - 1));
    }

    // this routine is based on offset 0,0 not 1,1
    @Override
    public void goto_XY(final int pos) {
        lastPos = pos;
        updateCursorLoc();
    }

    /**
     * Set the current working field to the field number specified.
     *
     * @param f -
     *            numeric field number on the screen
     * @return true or false whether it was sucessful
     */
    @Override
    public boolean gotoField(int f) {

        final int sizeFields = screenFields.getSize();

        if (f > sizeFields || f <= 0)
            return false;

        screenFields.setCurrentField(screenFields.getField(f - 1));

        while (screenFields.isCurrentFieldBypassField() && f < sizeFields) {

            screenFields.setCurrentField(screenFields.getField(f++));

        }
        return gotoField(screenFields.getCurrentField());
    }

    /**
     * Convenience method to set the field object passed as the currect working
     * screen field
     *
     * @param screenField screen field.
     * @return true or false whether it was sucessful
     */
    @Override
    public boolean gotoField(final ScreenField screenField) {
        if (screenField != null) {
            goto_XY(screenField.startPos());
            return true;
        }
        return false;
    }

    /**
     * Convenience class to position the cursor to the next word on the screen
     *
     */
    private void gotoNextWord() {

        final int pos = lastPos;

        if (planes.getChar(lastPos) > ' ') {
            advancePos();
            // get the next space character
            while (planes.getChar(lastPos) > ' ' && pos != lastPos) {
                advancePos();
            }
        } else
            advancePos();

        // now that we are positioned on the next space character get the
        // next none space character
        while (planes.getChar(lastPos) <= ' ' && pos != lastPos) {
            advancePos();
        }

    }

    /**
     * Convenience class to position the cursor to the previous word on the
     * screen
     *
     */
    private void gotoPrevWord() {

        final int pos = lastPos;

        changePos(-1);

        // position previous white space character
        while (planes.getChar(lastPos) <= ' ') {
            changePos(-1);
            if (pos == lastPos)
                break;
        }

        changePos(-1);
        // get the previous space character
        while (planes.getChar(lastPos) > ' ' && pos != lastPos) {
            changePos(-1);
        }

        // and position one position more should give us the beginning of word
        advancePos();

    }

    /**
     * Convinience class to position to the next field on the screen.
     */
    private void gotoFieldNext() {

        if (screenFields.isCurrentFieldHighlightedEntry())
            unsetFieldHighlighted(screenFields.getCurrentField());

        screenFields.gotoFieldNext();

        if (screenFields.isCurrentFieldHighlightedEntry())
            setFieldHighlighted(screenFields.getCurrentField());
    }

    /**
     * Convinience class to position to the previous field on the screen.
     */
    private void gotoFieldPrev() {

        if (screenFields.isCurrentFieldHighlightedEntry())
            unsetFieldHighlighted(screenFields.getCurrentField());

        screenFields.gotoFieldPrev();

        if (screenFields.isCurrentFieldHighlightedEntry())
            setFieldHighlighted(screenFields.getCurrentField());

    }

    /**
     * Creates a window on the screen
     *
     * @param depth depth.
     * @param width width.
     * @param type type..
     * @param gui if true set planes use GUI.
     * @param colorAttr color attribute.
     * @param ul upper left.
     * @param upper upper.
     * @param ur upper rigth.
     * @param left left.
     * @param right right.
     * @param ll left left.
     * @param bottom bottom.
     * @param lr left right.
     */
    protected void createWindow(int depth, int width, final int type, final boolean gui,
                                final int colorAttr, final int ul, final int upper, final int ur, final int left, final int right,
                                final int ll, final int bottom, final int lr) {

        final int c = getCol(lastPos);
        int w = 0;
        width++;

        w = width;
        // set leading attribute byte
        //		screen[lastPos].setCharAndAttr(initChar, initAttr, true);
        planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);
        setDirty(lastPos);

        advancePos();
        // set upper left
        //		screen[lastPos].setCharAndAttr((char) ul, colorAttr, false);
        planes.setScreenCharAndAttr(lastPos, (char) ul, colorAttr, false);
        if (gui) {
            //			screen[lastPos].setUseGUI(UPPER_LEFT);
            planes.setUseGUI(lastPos, UPPER_LEFT);
        }
        setDirty(lastPos);

        advancePos();

        // draw top row

        while (w-- >= 0) {
            //			screen[lastPos].setCharAndAttr((char) upper, colorAttr, false);
            planes.setScreenCharAndAttr(lastPos, (char) upper, colorAttr, false);
            if (gui) {
                //				screen[lastPos].setUseGUI(UPPER);
                planes.setUseGUI(lastPos, UPPER);
            }
            setDirty(lastPos);
            advancePos();
        }

        // set upper right
        //		screen[lastPos].setCharAndAttr((char) ur, colorAttr, false);
        planes.setScreenCharAndAttr(lastPos, (char) ur, colorAttr, false);

        if (gui) {
            //			screen[lastPos].setUseGUI(UPPER_RIGHT);
            planes.setUseGUI(lastPos, UPPER_RIGHT);
        }
        setDirty(lastPos);
        advancePos();

        // set ending attribute byte
        planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);

        setDirty(lastPos);

        lastPos = ((getRow(lastPos) + 1) * numCols) + c;

        // now handle body of window
        while (depth-- > 0) {

            // set leading attribute byte
            planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);
            setDirty(lastPos);
            advancePos();

            // set left
            planes.setScreenCharAndAttr(lastPos, (char) left, colorAttr, false);

            if (gui) {
                planes.setUseGUI(lastPos, GUI_LEFT);
            }
            setDirty(lastPos);
            advancePos();

            w = width;
            // fill it in
            while (w-- >= 0) {
                //				screen[lastPos].setCharAndAttr(initChar, initAttr, true);
                planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);
                //				screen[lastPos].setUseGUI(NO_GUI);
                planes.setUseGUI(lastPos, NO_GUI);
                setDirty(lastPos);
                advancePos();
            }

            // set right
            //			screen[lastPos].setCharAndAttr((char) right, colorAttr, false);
            planes.setScreenCharAndAttr(lastPos, (char) right, colorAttr, false);
            if (gui) {
                //				screen[lastPos].setUseGUI(RIGHT);
                planes.setUseGUI(lastPos, GUI_RIGHT);
            }

            setDirty(lastPos);
            advancePos();

            // set ending attribute byte
            //			screen[lastPos].setCharAndAttr(initChar, initAttr, true);
            planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);
            setDirty(lastPos);

            lastPos = ((getRow(lastPos) + 1) * numCols) + c;
        }

        // set leading attribute byte
        //		screen[lastPos].setCharAndAttr(initChar, initAttr, true);
        planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);
        setDirty(lastPos);
        advancePos();

        // set lower left
        //		screen[lastPos].setCharAndAttr((char) ll, colorAttr, false);
        planes.setScreenCharAndAttr(lastPos, (char) ll, colorAttr, false);
        if (gui) {
            //			screen[lastPos].setUseGUI(LOWER_LEFT);
            planes.setUseGUI(lastPos, LOWER_LEFT);
        }
        setDirty(lastPos);
        advancePos();

        w = width;

        // draw bottom row
        while (w-- >= 0) {
            planes.setScreenCharAndAttr(lastPos, (char) bottom, colorAttr, false);
            if (gui) {
                planes.setUseGUI(lastPos, BOTTOM);
            }
            setDirty(lastPos);
            advancePos();
        }

        // set lower right
        planes.setScreenCharAndAttr(lastPos, (char) lr, colorAttr, false);
        if (gui) {
            planes.setUseGUI(lastPos, LOWER_RIGHT);
        }

        setDirty(lastPos);
        advancePos();

        // set ending attribute byte
        planes.setScreenCharAndAttr(lastPos, initChar, initAttr, true);
        setDirty(lastPos);

    }

    /**
     * Creates a scroll bar on the screen using the parameters provided.
     *  ** we only support vertical scroll bars at the time.
     *
     * @param flag -
     *            type to draw - vertical or horizontal
     * @param totalRowScrollable total number of rows scrollable.
     * @param totalColScrollable total number of columns scrollable.
     * @param sliderRowPos slider row position.
     * @param sliderColPos slider column position.
     * @param sbSize buffer size.
     */
    @Override
    public void createScrollBar(final int flag, final int totalRowScrollable,
                                   final int totalColScrollable, final int sliderRowPos, final int sliderColPos,
                                   final int sbSize) {

        //      System.out.println("Scrollbar flag: " + flag +
        //                           " scrollable Rows: " + totalRowScrollable +
        //                           " scrollable Cols: " + totalColScrollable +
        //                           " thumb Row: " + sliderRowPos +
        //                           " thumb Col: " + sliderColPos +
        //                           " size: " + sbSize +
        //                           " row: " + getRow(lastPos) +
        //                           " col: " + getCol(lastPos));

        int sp = lastPos;
        final int size = sbSize - 2;

        final int thumbPos = (int) (size * ((float) sliderColPos / (float) totalColScrollable));
        //      System.out.println(thumbPos);
        planes.setScreenCharAndAttr(sp, ' ', 32, false);
        planes.setUseGUI(sp, BUTTON_SB_UP);

        int ctr = 0;
        while (ctr < size) {
            sp += numCols;
            planes.setScreenCharAndAttr(sp, ' ', 32, false);
            if (ctr == thumbPos)
                planes.setUseGUI(sp, BUTTON_SB_THUMB);
            else
                planes.setUseGUI(sp, BUTTON_SB_GUIDE);
            ctr++;
        }
        sp += numCols;


        planes.setScreenCharAndAttr(sp, ' ', 32, false);
        planes.setUseGUI(sp, BUTTON_SB_DN);
    }

    /**
     * Write the title of the window that is on the screen
     *
     * @param pos position.
     * @param depth depth.
     * @param width width.
     * @param orientation orientation.
     * @param colorAttr color attribute.
     * @param title title.
     */
    @Override
    public void writeWindowTitle(int pos, final int depth, final int width,
                                    final byte orientation, final int colorAttr, final StringBuffer title) {

        final int len = title.length();

        // get bit 0 and 1 for interrogation
        switch (orientation & 0xc0) {
            case 0x40: // right
                pos += (4 + width - len);
                break;
            case 0x80: // left
                pos += 2;
                break;
            default: // center
                // this is to place the position to the first text position of the
                // window
                //    the position passed in is the first attribute position, the next
                //    is the border character and then there is another attribute after
                //    that.
                pos += (3 + ((width / 2) - (len / 2)));
                break;

        }

        //  if bit 2 is on then this is a footer
        if ((orientation & 0x20) == 0x20)
            pos += ((depth + 1) * numCols);

        //      System.out.println(pos + "," + width + "," + len+ "," + getRow(pos)
        //                              + "," + getCol(pos) + "," + ((orientation >> 6) & 0xf0));

        for (int x = 0; x < len; x++) {
            planes.setChar(pos, title.charAt(x));
            planes.setUseGUI(pos++, NO_GUI);

        }
    }

    /**
     * Roll the screen up or down.
     *
     * Byte 1: Bit 0 0 = Roll up 1 = Roll down Bits 1-2 Reserved Bits 3-7 Number
     * of lines that the designated area is to be rolled Byte 2: Bits 0-7 Line
     * number defining the top line of the area that will participate in the
     * roll. Byte 3: Bits 0-7 Line number defining the bottom line of the area
     * that will participate in the roll.
     *
     * @param direction roll direction.
     * @param topLine top line.
     * @param bottomLine bottom line.
     */
    @Override
    public void rollScreen(final int direction, final int topLine, final int bottomLine) {

        // get the number of lines which are the last 5 bits
        /* int lines = direction & 0x7F; */
        // get the direction of the roll which is the first bit
        //    0 - up
        //    1 - down
        final int updown = direction & 0x80;
        final int lines = direction & 0x7F;

        // calculate the reference points for the move.
        final int start = this.getPos(topLine - 1, 0);
        final int end = this.getPos(bottomLine - 1, numCols - 1);

        //      System.out.println(" starting roll");
        //      dumpScreen();
        switch (updown) {
            case 0:
                //  Now round em up and head em UP.
                for (int x = start; x < end + numCols; x++) {
                    if (x + lines * numCols >= lenScreen) {
                        //Clear at the end
                        planes.setChar(x, ' ');
                    } else {
                        planes.setChar(x, planes.getChar(x + lines * numCols));
                    }
                }
                break;
            case 1:
                //  Now round em up and head em DOWN.
                for (int x = end + numCols; x > 0; x--) {
                    if ((x - lines * numCols) < 0) {
                        //Do nothing ... tooo small!!!
                    } else {
                        planes.setChar(x - lines * numCols, planes.getChar(x));
                        //and clear
                        planes.setChar(x, ' ');
                    }
                }
                break;
            default:
                log.warn(" Invalid roll parameter - please report this");
        }
        //      System.out.println(" end roll");
        //      dumpScreen();

    }

    public void dumpScreen() {

        final StringBuffer sb = new StringBuffer();
        final char[] s = getScreenAsChars();
        final int c = getColumns();
        final int l = getRows() * c;
        int col = 0;
        for (int x = 0; x < l; x++, col++) {
            sb.append(s[x]);
            if (col == c) {
                sb.append('\n');
                col = 0;
            }
        }
        log.info(sb.toString());

    }

    /**
     * Add a field to the field format table.
     *
     * @param attr - Field attribute
     * @param len - length of field
     * @param ffw1 - Field format word 1
     * @param ffw2 - Field format word 2
     * @param fcw1 - Field control word 1
     * @param fcw2 - Field control word 2
     */
    @Override
    public void addField(final int attr, final int len, final int ffw1, final int ffw2, final int fcw1, final int fcw2) {

        lastAttr = attr;
        planes.setScreenCharAndAttr(lastPos, initChar, lastAttr, true);
        setDirty(lastPos);
        advancePos();

        ScreenField sf = null;

        // from 14.6.12 for Start of Field Order 5940 function manual
        //  examine the format table for an entry that begins at the current
        //  starting address plus 1.
        if (screenFields.existsAtPos(lastPos)) {
            screenFields.setCurrentFieldFFWs(ffw1, ffw2);
        } else {
            sf = screenFields.setField(attr, getRow(lastPos), getCol(lastPos),
                    len, ffw1, ffw2, fcw1, fcw2);
            lastPos = sf.startPos();
            int x = len;

            boolean gui = guiInterface;
            if (sf.isBypassField())
                gui = false;

            while (x-- > 0) {

                if (planes.getChar(lastPos) == 0)
                    planes.setScreenCharAndAttr(lastPos, ' ', lastAttr, false);
                else
                    planes.setScreenAttr(lastPos, lastAttr);

                if (gui) {
                    planes.setUseGUI(lastPos, FIELD_MIDDLE);
                }

                // now we set the field plane attributes
                planes.setScreenFieldAttr(lastPos, ffw1);

                advancePos();

            }

            if (gui)
                if (len > 1) {
                    planes.setUseGUI(sf.startPos(), FIELD_LEFT);

                    if (lastPos > 0)
                        planes.setUseGUI(lastPos - 1, FIELD_RIGHT);
                    else
                        planes.setUseGUI(lastPos, FIELD_RIGHT);

                } else {
                    planes.setUseGUI(lastPos - 1, FIELD_ONE);
                }

            //         screen[lastPos].setCharAndAttr(initChar,initAttr,true);
            setEndingAttr(initAttr);

            lastPos = sf.startPos();
        }
    }

    /**
     * Return the fields that are contained in the Field Format Table
     *
     * @return ScreenFields object
     */
    private ScreenFields getScreenFields() {
        return screenFields;
    }

    /**
     * Redraw the fields on the screen. Used for gui enhancement to redraw the
     * fields when toggling
     *
     */
    @Override
    public void drawFields() {

        ScreenField sf;

        final int sizeFields = screenFields.getSize();
        for (int x = 0; x < sizeFields; x++) {

            sf = screenFields.getField(x);

            if (!sf.isBypassField()) {
                int pos = sf.startPos();

                int l = sf.length;

                boolean f = true;

                if (l >= lenScreen)
                    l = lenScreen - 1;

                if (l > 1) {
                    while (l-- > 0) {

                        if (guiInterface && f) {
                            planes.setUseGUI(pos, FIELD_LEFT);
                            f = false;
                        } else {

                            planes.setUseGUI(pos, FIELD_MIDDLE);

                        }

                        if (guiInterface && l == 0) {
                            planes.setUseGUI(pos, FIELD_RIGHT);
                        }

                        setDirty(pos++);
                    }
                } else {
                    planes.setUseGUI(pos, FIELD_ONE);
                }
            }
        }

        //updateDirty();
    }

    /**
     * Draws the field on the screen. Used to redraw or change the attributes of
     * the field.
     *
     * @param screenField Field to be redrawn
     */
    @Override
    public void drawField(final ScreenField screenField) {
        int pos = screenField.startPos();
        int x = screenField.length;
        while (x-- > 0) {
            setDirty(pos++);
        }
        updateDirty();
    }

    /**
     * Set the field to be displayed as highlighted.
     *
     * @param sf -
     *            Field to be highlighted
     */
    private void setFieldHighlighted(final ScreenField sf) {

        int pos = sf.startPos();

        int x = sf.length;
        final int na = sf.getHighlightedAttr();

        while (x-- > 0) {
            planes.setScreenAttr(pos, na);
            setDirty(pos++);
        }
        fireScreenChanged(1);

    }

    /**
     * Draw the field as un higlighted. This is used to reset the field
     * presentation on the screen after the field is exited.
     *
     * @param sf -
     *            Field to be unhighlighted
     */
    private void unsetFieldHighlighted(final ScreenField sf) {

        int pos = sf.startPos();

        int x = sf.length;
        final int na = sf.getAttr();

        while (x-- > 0) {
            planes.setScreenAttr(pos, na);
            setDirty(pos++);
        }
        fireScreenChanged(1);

    }

    @Override
    public boolean checkHotSpots() {

        return planes.checkHotSpots();
    }

    @Override
    public void setChar(final char cByte) {
        if (lastPos > 0) {
            lastAttr = planes.getCharAttr(lastPos - 1);
        }
        
        if (cByte > 0 && cByte < ' ') {
            planes.setScreenCharAndAttr(lastPos, (char) 0, 33, false);
        } else {
            planes.setScreenCharAndAttr(lastPos, cByte, lastAttr, false);
            if (guiInterface && !isInField(lastPos, false)) {
                planes.setUseGUI(lastPos, NO_GUI);
            }
        }

        setDirty(lastPos);
        advancePos();
    }

    protected void setEndingAttr(final int cByte) {
        final int attr = lastAttr;
        setAttr(cByte);
        lastAttr = attr;
    }

    @Override
    public void setAttr(final int cByte) {
        lastAttr = cByte;

        //      int sattr = screen[lastPos].getCharAttr();
        //         System.out.println("changing from " + sattr + " to attr " + lastAttr
        // +
        //                     " at " + (this.getRow(lastPos) + 1) + "," + (this.getCol(lastPos) +
        // 1));
        planes.setScreenCharAndAttr(lastPos, initChar, lastAttr, true);
        setDirty(lastPos);

        advancePos();
        final int pos = lastPos;
        while (planes.getCharAttr(lastPos) != lastAttr
                && !planes.isAttributePlace(lastPos)) {

            planes.setScreenAttr(lastPos, lastAttr);
            if (guiInterface && !isInField(lastPos, false)) {
                final int g = planes.getWhichGUI(lastPos);
                if (g >= FIELD_LEFT && g <= FIELD_ONE)
                    planes.setUseGUI(lastPos, NO_GUI);
            }
            setDirty(lastPos);
            advancePos();
        }

        // sanity check for right now
        //      if (times > 200)
        //         System.out.println(" setAttr = " + times + " start = " + (sr + 1) +
        // "," + (sc + 1));

        lastPos = pos;
    }

    @Override
    public void setScreenCharAndAttr(final char right, final int colorAttr, final boolean isAttr) {

        planes.setScreenCharAndAttr(lastPos, right, colorAttr, isAttr);
        setDirty(lastPos);
        advancePos();

    }

    @Override
    public void setScreenCharAndAttr(final char right, final int colorAttr,
                                        final int whichGui, final boolean isAttr) {

        planes.setScreenCharAndAttr(lastPos, right, colorAttr, isAttr);
        planes.setUseGUI(lastPos, whichGui);

        setDirty(lastPos);
        advancePos();

    }

    /**
     * Draw or redraw the dirty parts of the screen and display them.
     *
     * Rectangle dirty holds the dirty area of the screen to be updated.
     *
     * If you want to change the screen in anyway you need to set the screen
     * attributes before calling this routine.
     */
    @Override
    public void updateDirty() {

        fireScreenChanged(1);

    }

    @Override
    public void setDirty(final int pos) {

        final int minr = Math.min(getRow(pos), getRow(dirtyScreen.x));
        final int minc = Math.min(getCol(pos), getCol(dirtyScreen.x));

        final int maxr = Math.max(getRow(pos), getRow(dirtyScreen.y));
        final int maxc = Math.max(getCol(pos), getCol(dirtyScreen.y));

        final int x1 = getPos(minr, minc);
        final int x2 = getPos(maxr, maxc);

        dirtyScreen.setBounds(x1, x2, 0, 0);

    }

    private void resetDirty(final int pos) {

        dirtyScreen.setBounds(pos, pos, 0, 0);

    }

    /**
     * Change the screen position by one column
     */
    @Override
    public void advancePos() {
        changePos(1);
    }

    /**
     * Change position of the screen by the increment of parameter passed.
     *
     * If the position change is under the minimum of the first screen position
     * then the position is moved to the last row and column of the screen.
     *
     * If the position change is over the last row and column of the screen then
     * cursor is moved to first position of the screen.
     *
     * @param num num characters to add to last position.
     */
    @Override
    public void changePos(final int num) {

        lastPos += num;
        if (lastPos < 0)
            lastPos = lenScreen + lastPos;
        if (lastPos > lenScreen - 1)
            lastPos = lastPos - lenScreen;
    }

    @Override
    public void goHome() {

        //  now we try to move to first input field according to
        //  14.6 WRITE TO DISPLAY Command
        //    ? If the WTD command is valid, after the command is processed,
        //          the cursor moves to one of three locations:
        //    - The location set by an insert cursor order (unless control
        //          character byte 1, bit 1 is equal to B'1'.)
        //    - The start of the first non-bypass input field defined in the
        //          format table
        //    - A default starting address of row 1 column 1.

        if (pendingInsert && homePos > 0) {
            setCursor(getRow(homePos), getCol(homePos));
            isInField(); // we now check if we are in a field
        } else {
            if (!gotoField(1)) {
                homePos = getPos(1, 1);
                setCursor(1, 1);
                isInField(0, 0); // we now check if we are in a field
            } else {
                homePos = getPos(getCurrentRow(), getCurrentCol());
            }
        }
    }

    @Override
    public void setPendingInsert(final boolean flag, final int icX, final int icY) {
        pendingInsert = flag;
        if (pendingInsert) {
            homePos = getPos(icX, icY);
        }

        if (!isStatusErrorCode()) {
            setCursor(icX, icY);
        }
    }

    @Override
    public void setPendingInsert(final boolean flag) {
        if (homePos != -1)
            pendingInsert = flag;
    }

    /**
     * Set the error line number to that of number passed.
     *
     * @param line error line number.
     */
    @Override
    public void setErrorLine(final int line) {

        planes.setErrorLine(line);
    }

    /**
     * Returns the current error line number
     *
     * @return current error line number
     */
    @Override
    public int getErrorLine() {
        return planes.getErrorLine();
    }

    /**
     * Saves off the current error line characters to be used later.
     *
     */
    @Override
    public void saveErrorLine() {
        planes.saveErrorLine();
    }

    /**
     * Restores the error line characters from the save buffer.
     *
     * @see #saveErrorLine()
     */
    @Override
    public void restoreErrorLine() {

        if (planes.isErrorLineSaved()) {
            planes.restoreErrorLine();
            fireScreenChanged(1, planes.getErrorLine() - 1, 0, planes.getErrorLine() - 1, numCols - 1);
        }
    }

    @Override
    public void setStatus(final byte attr, final byte value, final String s) {

        // set the status area
        switch (attr) {

            case STATUS_SYSTEM:
                if (value == STATUS_VALUE_ON) {
                    oia.setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT, ScreenOIA.OIA_LEVEL_INPUT_INHIBITED, s);
                } else {
                    oia.setInputInhibited(ScreenOIA.INPUTINHIBITED_NOTINHIBITED, ScreenOIA.OIA_LEVEL_NOT_INHIBITED, s);
                }
                break;

            case STATUS_ERROR_CODE:
                if (value == STATUS_VALUE_ON) {
                    setPrehelpState(true, true, false);
                    oia.setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
                            ScreenOIA.OIA_LEVEL_INPUT_ERROR, s);

                    sessionVT.signalBell();
                } else {
                    oia.setInputInhibited(ScreenOIA.INPUTINHIBITED_NOTINHIBITED,
                            ScreenOIA.OIA_LEVEL_NOT_INHIBITED);
                    setPrehelpState(false, true, true);
                    homePos = saveHomePos;
                    saveHomePos = 0;
                    pendingInsert = false;
                }
                break;

        }
    }

    @Override
    public boolean isStatusErrorCode() {

        return oia.getLevel() == ScreenOIA.OIA_LEVEL_INPUT_ERROR;

    }

    /**
     * This routine clears the screen, resets row and column to 0, resets the
     * last attribute to 32, clears the fields, turns insert mode off,
     * clears/initializes the screen character array.
     */
    @Override
    public void clearAll() {

        lastAttr = 32;
        lastPos = 0;

        clearTable();
        clearScreen();
        planes.setScreenAttr(0, initAttr);
        oia.setInsertMode(false);
    }

    /**
     * Clear the fields table
     */
    @Override
    public void clearTable() {

        oia.setKeyBoardLocked(true);
        screenFields.clearFFT();
        planes.initalizeFieldPlanes();
        pendingInsert = false;
        homePos = -1;
    }

    /**
     * Clear the gui constructs
     *
     */
    @Override
    public void clearGuiStuff() {

        for (int x = 0; x < lenScreen; x++) {
            planes.setUseGUI(x, NO_GUI);
        }
        dirtyScreen.setBounds(0, lenScreen - 1, 0, 0);
    }

    /**
     * Clear the screen by setting the initial character and initial attribute
     * to all the positions on the screen
     */
    @Override
    public void clearScreen() {

        planes.initalizePlanes();

        dirtyScreen.setBounds(0, lenScreen - 1, 0, 0);

        oia.clearScreen();

    }

    @Override
    public void restoreScreen() {

        lastAttr = 32;
        dirtyScreen.setBounds(0, lenScreen - 1, 0, 0);
        updateDirty();
    }

    /**
     * Notify all registered listeners of the onScreenChanged event.
     *
     */
    private void fireScreenChanged(final int which, final int startRow, final int startCol,
                                   final int endRow, final int endCol) {
        if (screenListeners != null) {
            // Patch below contributed by Mitch Blevins
            //int size = listeners.size();
            final Vector<ScreenListener> lc = new Vector<ScreenListener>(screenListeners);
            final int size = lc.size();
            for (int i = 0; i < size; i++) {
                //ScreenListener target =
                //      (ScreenListener)listeners.elementAt(i);
                final ScreenListener target = lc.elementAt(i);
                target.onScreenChanged(1, startRow, startCol, endRow, endCol);
            }
        }
        dirtyScreen.setBounds(lenScreen, 0, 0, 0);
    }

    /**
     * Notify all registered listeners of the onScreenChanged event.
     *
     */
    private synchronized void fireScreenChanged(final int update) {
        if (dirtyScreen.x > dirtyScreen.y) {
            //         log.info(" x < y " + dirtyScreen);
            return;
        }

        fireScreenChanged(update, getRow(dirtyScreen.x), getCol(dirtyScreen.x),
                getRow(dirtyScreen.y), getCol(dirtyScreen.y));

    }

    /**
     * Notify all registered listeners of the onScreenChanged event.
     *
     */
    private synchronized void fireCursorChanged(final int update) {
        final int startRow = getRow(lastPos);
        final int startCol = getCol(lastPos);

        if (screenListeners != null) {
            final List<ScreenListener> lc = new LinkedList<ScreenListener>(screenListeners);
            for (final ScreenListener target : lc) {
                target.onScreenChanged(update, startRow, startCol, startRow, startCol);
            }
        }
    }

    /**
     * Notify all registered listeners of the onScreenSizeChanged event.
     *
     */
    private void fireScreenSizeChanged() {
        if (screenListeners != null) {
            final List<ScreenListener> lc = new LinkedList<ScreenListener>(screenListeners);
            for (final ScreenListener target : lc) {
                target.onScreenSizeChanged(numRows, numCols);
            }
        }
    }

    /**
     * Add a ScreenListener to the listener list.
     *
     * @param listener  The ScreenListener to be added
     */
    @Override
    public void addScreenListener(final ScreenListener listener) {

        if (screenListeners == null) {
            screenListeners = new java.util.Vector<ScreenListener>(3);
        }
        screenListeners.addElement(listener);

    }

    /**
     * Remove a ScreenListener from the listener list.
     *
     * @param listener  The ScreenListener to be removed
     */
    @Override
    public void removeScreenListener(final ScreenListener listener) {

        if (screenListeners == null) {
            return;
        }
        screenListeners.removeElement(listener);
    }

    /**
     * Utility method to share the repaint behaviour between setBounds() and
     * updateScreen.
     */
    @Override
    public void repaintScreen() {

        setCursorOff();
        dirtyScreen.setBounds(0, lenScreen - 1, 0, 0);
        updateDirty();
        // restore statuses that were on the screen before resize
        if (oia.getLevel() == ScreenOIA.OIA_LEVEL_INPUT_ERROR) {
            oia.setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
                    ScreenOIA.OIA_LEVEL_INPUT_ERROR);
        }

        if (oia.getLevel() == ScreenOIA.OIA_LEVEL_INPUT_INHIBITED) {
            oia.setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
                    ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);
        }

        if (oia.isMessageWait())
            oia.setMessageLightOn();
        setCursorOn();
    }

    // ADDED BY BARRY - changed by Kenneth to use the character plane
    //  This should be replaced with the getPlane methods when they are implemented
    @Override
    public char[] getCharacters() {
        return planes.screen;
    }
    @Override
    public boolean isPlanesAttributePlace(final int pos) {
        return planes.isAttributePlace(pos);
    }

    @Override
    public void addOIAListener(final ScreenOIAListener listener) {
        getOIA().addOIAListener(listener);
    }

    @Override
    public int getHomePos() {
        return homePos;
    }

    @Override
    public ScreenField getCurrentScreenField() {
        return getScreenFields().getCurrentField();
    }

    @Override
    public char getPlanesChar(final int pos) {
        return getPlanes().getChar(pos);
    }

    @Override
    public int getPlanesCharAttr(final int pos) {
        return getPlanes().getCharAttr(pos);
    }

    @Override
    public ScreenField getScreenField(final int index) {
        return getScreenFields().getField(index);
    }

    @Override
    public int getScreenFieldsSize() {
        return getScreenFields().getSize();
    }

    @Override
    public boolean isOiaInsertMode() {
        return getOIA().isInsertMode();
    }

    @Override
    public boolean isOiaKeyBoardLocked() {
        return getOIA().isKeyBoardLocked();
    }

    @Override
    public boolean isOiaMessageWait() {
        return getOIA().isMessageWait();
    }

    @Override
    public boolean isPlanesUseGui(final int pos) {
        return getPlanes().isUseGui(pos);
    }

    @Override
    public void readFormatTable(final Buffer baosp, final int readType, final ICodePage codePage) {
        getScreenFields().readFormatTable(baosp, readType, codePage);
    }

    @Override
    public void removeOIAListener(final ScreenOIAListener l) {
        getOIA().removeOIAListener(l);
    }

    @Override
    public void setOiaInputInhibited(final int inhibit, final int whatCode) {
        getOIA().setInputInhibited(inhibit, whatCode);
    }

    @Override
    public void setOiaInputInhibited(final int inhibit, final int whatCode, final String message) {
        getOIA().setInputInhibited(inhibit, whatCode, message);
    }

    @Override
    public void setOiaKeyBoardLocked(final boolean b) {
        getOIA().setKeyBoardLocked(b);
    }

    @Override
    public void setOiaMessageLightOff() {
        getOIA().setMessageLightOff();
    }

    @Override
    public void setOiaMessageLightOn() {
        getOIA().setMessageLightOn();
    }

    @Override
    public void setOiaScriptActive(final boolean running) {
        getOIA().setScriptActive(running);
    }

    @Override
    public void setPlanesChar(final int pos, final char c) {
        getPlanes().setChar(pos, c);
    }

    @Override
    public void setPlanesScreenCharAndAttr(final int pos, final char c, final int attr, final boolean isAttr) {
        getPlanes().setScreenCharAndAttr(pos, c, attr, isAttr);
    }

    @Override
    public void setPlanesScreenFieldAttr(final int pos, final int attr) {
        getPlanes().setScreenFieldAttr(pos, attr);
    }

    @Override
    public ScreenField setScreenFieldsField(final int attr, final int row, final int col, final int len, final int ffw1,
            final int ffw2, final int fcw1, final int fcw2) {
        return getScreenFields().setField(attr, row, col, len, ffw1, ffw2, fcw1, fcw2);
    }

    @Override
    public void setScreenFieldsFieldChar(final char c) {
        getScreenFields().getCurrentField().setFieldChar(c);
    }

    @Override
    public void setScreenFieldsMasterMDT() {
        getScreenFields().setMasterMDT();
    }

    @Override
    public void setScreenFieldsSelectionFieldInfo(final int type, final int index, final int position) {
        getScreenFields().getCurrentField().setSelectionFieldInfo(type, index, position);
    }
}
