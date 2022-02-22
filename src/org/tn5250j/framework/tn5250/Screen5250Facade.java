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

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.keyboard.KeyMnemonic;

import javafx.geometry.Rectangle2D;

public interface Screen5250Facade {
    final static byte STATUS_SYSTEM = 1;
    final static byte STATUS_ERROR_CODE = 2;
    final static byte STATUS_VALUE_ON = 1;
    final static byte STATUS_VALUE_OFF = 2;

    final static int initAttr = 32;
    final static char initChar = 0;

    /**
     * Get the current row where the cursor is
     *
     * @return the cursor current row position 1,1 based
     */
    int getCurrentRow();

    /**
     * Get the current column where the cursor is
     *
     * @return the cursor current column position 1,1 based
     */
    int getCurrentCol();

    /**
     * Get the number of columns available.
     *
     * @return number of columns
     */
    int getColumns();

    /**
     * The last position of the cursor on the screen - Note - position is based
     * 0,0
     *
     * @return last position
     */
    int getLastPos();

    /**
     * This routine is 0 based offset. So to get row 20,1 then pass row 19,0
     *
     * @param row row
     * @param col column
     * @return position
     */
    int getPos(int row, int col);

    void setCursorOff();

    /**
     * Change the screen position by one column
     */
    void advancePos();

    void setCursorOn();

    /**
     * Convenience method to set the field object passed as the currect working
     * screen field
     *
     * @param screenField
     * @return true or false whether it was sucessful
     */
    boolean gotoField(final ScreenField screenField);

    /**
     * Set the current working field to the field number specified.
     *
     * @param f -
     *            numeric field number on the screen
     * @return true or false whether it was sucessful
     */
    boolean gotoField(int f);

    /**
     * Change position of the screen by the increment of parameter passed.
     *
     * If the position change is under the minimum of the first screen position
     * then the position is moved to the last row and column of the screen.
     *
     * If the position change is over the last row and column of the screen then
     * cursor is moved to first position of the screen.
     *
     * @param i
     */
    void changePos(int i);

    int getRow(int pos);

    int getCol(int pos);

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
    void sendKeys(String text);

    void sendKeys(final KeyMnemonic keyMnemonic);

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
    char[] getScreenAsAllChars();

    /**
     * Activate the cursor on screen
     *
     * @param activate
     */
    void setCursorActive(final boolean activate);

    boolean isPlanesAttributePlace(int cursorPos);

    int getPlanesCharAttr(int cursorPos);

    char getPlanesChar(int cursorPos);

    void setPlanesChar(int cursorPos, char c);

    void setScreenFieldsMasterMDT();

    /**
     * Get the number or rows available.
     *
     * @return number of rows
     */
    int getRows();

    void setRowsCols(final int rows, final int cols);
    /**
     * This routine clears the screen, resets row and column to 0, resets the
     * last attribute to 32, clears the fields, turns insert mode off,
     * clears/initializes the screen character array.
     */
    void clearAll();

    // this routine is based on offset 0,0 not 1,1
    void goto_XY(final int pos);

    boolean isPlanesUseGui(int y);

    void setPlanesScreenCharAndAttr(int y, char planesChar, int b, boolean c);

    ScreenField setScreenFieldsField(int attr, int row, int col, int fLen, int ffw1, int ffw2, int fcw1, int fcw2);

    void setPlanesScreenFieldAttr(int i, int ffw1);

    boolean isUsingGuiInterface();

    /**
     * Redraw the fields on the screen. Used for gui enhancement to redraw the
     * fields when toggling
     *
     */
    void drawFields();

    void restoreScreen();

    void setPendingInsert(final boolean flag, final int icX, final int icY);

    /**
     * Convinience class to return if the cursor is in a field or not.
     *
     * @return true or false
     */
    boolean isInField();

    /**
     * Clear the fields table
     */
    void clearTable();

    void goHome();

    /**
     * Roll the screen up or down.
     *
     * Byte 1: Bit 0 0 = Roll up 1 = Roll down Bits 1-2 Reserved Bits 3-7 Number
     * of lines that the designated area is to be rolled Byte 2: Bits 0-7 Line
     * number defining the top line of the area that will participate in the
     * roll. Byte 3: Bits 0-7 Line number defining the bottom line of the area
     * that will participate in the roll.
     *
     * @param direction
     * @param topLine
     * @param bottomLine
     */
    void rollScreen(final int direction, final int topLine, final int bottomLine);

    void setPrehelpState(final boolean setErrorCode, final boolean lockKeyboard,
            final boolean unlockIfLocked);

    void setOiaInputInhibited(int inputinhibitedNotinhibited, int oiaLevelInputInhibited);

    void setOiaInputInhibited(int inputinhibitedSystemWait, int oiaLevelInputInhibited, String string);

    void setOiaKeyBoardLocked(boolean b);

    void readFormatTable(ByteArrayOutputStream baosp, int readType, ICodePage codePage);

    boolean isStatusErrorCode();

    /**
     * Restores the error line characters from the save buffer.
     *
     * @see #saveErrorLine()
     */
    void restoreErrorLine();

    void setStatus(final byte attr, final byte value, final String s);

    /**
     * Draw or redraw the dirty parts of the screen and display them.
     *
     * Rectangle dirty holds the dirty area of the screen to be updated.
     *
     * If you want to change the screen in anyway you need to set the screen
     * attributes before calling this routine.
     */
    void updateDirty();

    void setOiaMessageLightOn();

    void setOiaMessageLightOff();

    /**
     * Is the keyboard locked or not
     *
     * @return locked or not
     */
    boolean isOiaKeyBoardLocked();

    /**
     * Current position is based on offsets of 1,1 not 0,0 of the current
     * position of the screen
     *
     * @return int
     */
    int getCurrentPos();

    int getHomePos();

    int getScreenFieldsSize();

    ScreenField getScreenField(int x);

    /**
     * Draws the field on the screen. Used to redraw or change the attributes of
     * the field.
     *
     * @param screenField Field to be redrawn
     */
    void drawField(final ScreenField screenField);

    void setPendingInsert(boolean flag);

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
    void addField(final int attr, final int len, final int ffw1, final int ffw2, final int fcw1, final int fcw2);

    void setAttr(final int cByte);

    void setChar(final int cByte);

    /**
     * This routine is based on offset 1,1 not 0,0 it will translate to offset
     * 0,0 and call the goto_XY(int pos) it is mostly used from external classes
     * that use the 1,1 offset
     *
     * @param row
     * @param col
     */
    void setCursor(final int row, final int col);

    /**
     * Set the error line number to that of number passed.
     *
     * @param line
     */
    void setErrorLine(int line);

    /**
     * Clear the screen by setting the initial character and initial attribute
     * to all the positions on the screen
     */
    void clearScreen();

    /**
     * Returns the current error line number
     *
     * @return current error line number
     */
    int getErrorLine();

    /**
     * Saves off the current error line characters to be used later.
     *
     */
    void saveErrorLine();

    /**
     * Gets the length of the screen - number of rows times number of columns
     *
     * @return int value of screen length
     */
    int getScreenLength();

    /**
     * Hotspot More... string
     *
     * @return string literal of More...
     */
    StringBuffer getHSMore();

    /**
     * Hotspot Bottom string
     *
     * @return string literal of Bottom
     */
    StringBuffer getHSBottom();

    /**
    *
    * Convinience class to return if the position that is passed is in a field
    * or not. If it is then the chgToField parameter will change the current
    * field to this field where the position indicates
    *
    * @param pos
    * @param chgToField
    * @return true or false
    */
    boolean isInField(int x, boolean b);

    void setDirty(int pos);

    void setScreenCharAndAttr(final char right, final int colorAttr, final boolean isAttr);

    void setScreenCharAndAttr(final char right, final int colorAttr,
            final int whichGui, final boolean isAttr);

    void writeWindowTitle(int pos, final int depth, final int width,
            final byte orientation, final int monoAttr, final int colorAttr, final StringBuffer title);

    /**
     * Creates a scroll bar on the screen using the parameters provided.
     *  ** we only support vertical scroll bars at the time.
     *
     * @param flag -
     *            type to draw - vertical or horizontal
     * @param totalRowScrollable
     * @param totalColScrollable
     * @param sliderRowPos
     * @param sliderColPos
     * @param sbSize
     */
    void createScrollBar(final int flag, final int totalRowScrollable,
            final int totalColScrollable, final int sliderRowPos, final int sliderColPos,
            final int sbSize);

    void setScreenFieldsFieldChar(char c);

    void setScreenFieldsSelectionFieldInfo(int i, int fld, int chcPos);

    /**
     * Clear the gui constructs
     *
     */
    void clearGuiStuff();

    ScreenField getCurrentScreenField();

    void toggleGUIInterface();

    /**
     * Copy & Paste support
     *
     * @param content
     * @param special
     */
    void pasteText(final String content, final boolean special);

    char[] getCharacters();

    /**
     *
     * Return the screen represented as a character array
     *
     * @return character array containing the text
     */
    char[] getScreenAsChars();

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
     * @param buffer
     * @param bufferLength
     * @param plane
     * @return The number of characters copied to the buffer
     */

    int GetScreen(final char buffer[], final int bufferLength, final int plane);

    boolean checkHotSpots();

    void setVT(final tnvt v);

    /**
     * This will move the screen cursor based on the mouse event.
     *
     * I do not think the checks here for the gui characters should be here but
     * will leave them here for now until we work out the interaction.  This
     * should be up to the gui frontend in my opinion.
     *
     * @param pos
     */
    boolean moveCursor(int pos);

    boolean isOiaMessageWait();

    /**
     * Remove a iOhioSessionListener from the listener list.
     *
     * @param listener  The iOhioSessionListener to be removed
     */
    void removeOIAListener(ScreenOIAListener guiGraBuf);

    /**
     * Remove a ScreenListener from the listener list.
     *
     * @param listener  The ScreenListener to be removed
     */
    void removeScreenListener(ScreenListener guiGraBuf);

    /**
     * Add a ScreenOIAListener to the listener list.
     *
     * @param listener  The ScreenOIAListener to be added
     */
    void addOIAListener(ScreenOIAListener guiGraBuf);

    /**
     * Add a ScreenListener to the listener list.
     *
     * @param listener  The ScreenListener to be added
     */
    void addScreenListener(ScreenListener guiGraBuf);

    void setOiaScriptActive(boolean b);
    /**
     * Utility method to share the repaint behaviour between setBounds() and
     * updateScreen.
     */
    void repaintScreen();

    /**
     * Copy & Paste support
     *
     * @param position
     * @return
     */
    String copyTextField(final int position);

    /**
     * Copy & Paste support
     *
     * @see {@link #pasteText(String, boolean)}
     * @see {@link #copyTextField(int)}
     */
    String copyText(final Rect workR);

    /**
     * @param formatOption formatting option to use
     * @return vector string of numberic values
     */
    List<Double> sumThem(final boolean formatOption, final Rectangle2D area);

    void setUseGUIInterface(final boolean gui);

    void setResetRequired(final boolean reset);

    void setBackspaceError(final boolean onError);

    boolean isOiaInsertMode();

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
     * @param buffer
     * @param bufferLength
     * @param startRow
     * @param startCol
     * @param endRow
     * @param endCol
     * @param plane
     * @return The number characters copied to the buffer
     */
    int GetScreenRect(final char buffer[], final int bufferLength,
            final int startRow, final int startCol, final int endRow, final int endCol, final int plane);
}
