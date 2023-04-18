/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
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

import static org.tn5250j.TN5250jConstants.CMD_READ_INPUT_FIELDS;
import static org.tn5250j.TN5250jConstants.CMD_READ_MDT_FIELDS;
import static org.tn5250j.TN5250jConstants.CMD_READ_MDT_IMMEDIATE_ALT;

import org.tn5250j.encoding.ICodePage;

public class ScreenFields {

    private ScreenField[] screenFields;
    private ScreenField currentField;
    private ScreenField saveCurrent;
    private int sizeFields;
    private boolean cpfExists;
    private int nextField;
    private int fieldIds;
    private Screen5250Facade screen;
    private boolean masterMDT;
    protected boolean currentModified;

    public ScreenFields(final Screen5250Facade s) {

        screen = s;
        screenFields = new ScreenField[256];
    }

    protected void clearFFT() {

        sizeFields = nextField = fieldIds = 0;
        cpfExists = false;   // clear the cursor progression fields flag
        currentField = null;
        masterMDT = false;

    }

    protected boolean existsAtPos(final int lastPos) {

        ScreenField sf = null;

        // from 14.6.12 for Start of Field Order 5940 function manual
        //  examine the format table for an entry that begins at the current
        //  starting address plus 1.
        for (int x = 0; x < sizeFields; x++) {
            sf = screenFields[x];

            if (lastPos == sf.startPos()) {
                currentField = sf;
                currentModified = false;
                return true;
            }

        }

        return false;
    }

    public boolean isMasterMDT() {
        return masterMDT;
    }

    protected void setMasterMDT() {
        masterMDT = true;
    }

    public boolean isCurrentField() {
        return currentField == null;
    }

    public boolean isCurrentFieldFER() {
        return currentField.isFER();
    }

    public boolean isCurrentFieldDupEnabled() {
        return currentField.isDupEnabled();
    }

    public boolean isCurrentFieldToUpper() {
        return currentField.isToUpper();
    }

    public boolean isCurrentFieldBypassField() {
        return currentField.isBypassField();
    }

    public boolean isCurrentFieldHighlightedEntry() {
        if (currentField != null)
            return currentField.isHiglightedEntry();
        else
            return false;
    }

    public boolean isCurrentFieldAutoEnter() {
        return currentField.isAutoEnter();
    }

    public boolean withinCurrentField(final int pos) {
        return currentField.withinField(pos);
    }

    public boolean isCurrentFieldContinued() {
        return currentField.isContinued();
    }

    public boolean isCurrentFieldContinuedFirst() {
        return currentField.isContinuedFirst();
    }

    public boolean isCurrentFieldContinuedMiddle() {
        return currentField.isContinuedMiddle();
    }

    public boolean isCurrentFieldContinuedLast() {
        return currentField.isContinuedLast();
    }

    public boolean isCurrentFieldModified() {
        return currentModified;
    }

    /**
     * This routine is used to check if we can send the Aid key to the host
     *
     * Taken from Section  16.2.1.2 Enter/Rec Adv Key
     *
     * In the normal unlocked state, when the workstation operator presses the
     * Enter/Rec Adv key:
     *
     * 1. The 5494 checks for the completion of mandatory-fill, self-check, and
     *    right-adjust fields when in an active field. (An active field is one in
     *    which the workstation operator has begun entering data.) If the
     *    requirements of the field have not been satisfied, an error occurs.
     *
     * @return true if can send aid.
     *
     */
    public boolean isCanSendAid() {

        // We also have to check if we are still in the field.
        if (currentField != null &&
                (currentField.getAdjustment() > 0 || currentField.isSignedNumeric())
                && currentModified && isInField()
                && !currentField.isCanSend())

            return false;
        else
            return true;

    }

    protected void saveCurrentField() {
        saveCurrent = currentField;
    }

    protected void restoreCurrentField() {
        currentField = saveCurrent;
    }

    protected void setCurrentField(final ScreenField sf) {
        currentField = sf;
    }

    protected void setCurrentFieldMDT() {
        currentField.setMDT();
        currentModified = true;
        masterMDT = true;
    }

    protected void setCurrentFieldFFWs(final int ffw1, final int ffw2) {

        masterMDT = currentField.setFFWs(ffw1, ffw2);

    }


    protected ScreenField setField(final int attr, final int row, final int col, final int len, final int ffw1,
                                   final int ffw2, final int fcw1, final int fcw2) {

        ScreenField sf = null;
        screenFields[nextField] = new ScreenField(screen);
        screenFields[nextField].setField(attr, row, col, len, ffw1, ffw2, fcw1, fcw2);
        sf = screenFields[nextField++];

        sizeFields++;


        // set the field id if it is not a bypass field
        // this is used for cursor progression
        //  changed this because of problems not allocating field id's for
        //  all fields.  kjp 2002/10/21
//      if (!sf.isBypassField())
        sf.setFieldId(++fieldIds);

        // check if the cursor progression field flag should be set.
//      if ((fcw1 & 0x88) == 0x88)
        if (fcw1 == 0x88)
            cpfExists = true;

        if (currentField != null) {
            currentField.next = sf;
            sf.prev = currentField;
        }

        currentField = sf;

        // check if the Modified Data Tag was set while creating the field
        if (!masterMDT)
            masterMDT = currentField.mdt;

        currentModified = false;

        return currentField;

    }

    public ScreenField getField(final int index) {

        return screenFields[index];
    }

    public ScreenField getCurrentField() {
        return currentField;
    }

    public int getCurrentFieldPos() {
        return currentField.getCurrentPos();
    }

    protected int getCurrentFieldShift() {
        return currentField.getFieldShift();
    }

    public String getCurrentFieldText() {

        return currentField.getText();
    }

    public int getCurrentFieldHighlightedAttr() {
        return currentField.getHighlightedAttr();
    }

    public int getSize() {

        return sizeFields;
    }

    public int getFieldCount() {

        return sizeFields;
    }

    protected boolean isInField(final int pos) {
        return isInField(pos, true);
    }

    protected boolean isInField() {
        return isInField(screen.getLastPos(), true);
    }

    protected boolean isInField(final int pos, final boolean chgToField) {

        ScreenField sf;

        for (int x = 0; x < sizeFields; x++) {
            sf = screenFields[x];

            if (sf.withinField(pos)) {

                if (chgToField) {
                    if (!currentField.equals(sf))
                        currentModified = false;
                    currentField = sf;
                }
                return true;
            }
        }
        return false;

    }

    /**
     * Searches the collection for the target string and returns the iOhioField
     * object containing that string.  The string must be totally contained
     * within the field to be considered a match.
     * @param startPos The row and column where to start the search. The position
     *                 is inclusive (for example, row 1, col 1 means that
     *                 position 1,1 will be used as the starting location and
     *                 1,1 will be included in the search).
     * @param startPos start position.
     *
     * @return If found, an iOhioField object containing the target string. If
     *         not found, returns a null.
     */
    public ScreenField findByString(final int startPos) {

        // first lets check if the string exists in the screen space
//      iOhioPosition pos = screen.findString(targetString, startPos, length,
//                                             dir, ignoreCase);

        // if it does exist then lets search the fields by the position that
        //  was found and return the results of that search.
//      if (pos != null) {
        return findByPosition(startPos);
//      }

        //return null;

    }

    /**
     * Searches the collection for the target position and returns the ScreenField
     * object containing that position.
     *
     * @param targetPosition The target row and column expressed as a linear
     *          position within the presentation space.
     *
     * @return If found, a ScreenField object containing the target position.
     *         If not found, returns a null.
     */
    public ScreenField findByPosition(final int targetPosition) {

        ScreenField sf = null;

        for (int x = 0; x < sizeFields; x++) {

            sf = screenFields[x];

            if (sf.withinField(targetPosition)) {
                return sf;
            }

        }

        return null;
    }

    /**
     * Searches the collection for the target position and returns the ScreenField
     * object containing that position.
     *
     * @param row The beginning row to start search with in the presentation space.
     * @param col The beginning column to start search with in the presentation space.
     *
     * @return If found, a ScreenField object containing the target position.
     *         If not found, returns a null.
     */
    public ScreenField findByPosition(final int row, final int col) {

        return findByPosition(screen.getPos(row, col));
    }

    public ScreenField[] getFields() {

        final ScreenField[] fields = new ScreenField[sizeFields];
        for (int x = 0; x < sizeFields; x++) {

            fields[x] = screenFields[x];
        }

        return fields;
    }

    public ScreenField getFirstInputField() {

        if (sizeFields <= 0)
            return null;

        int f = 0;
        ScreenField sf = screenFields[f];

        while (sf.isBypassField() && f++ < sizeFields) {
            sf = screenFields[f];
        }

        if (sf.isBypassField())
            return null;
        else
            return sf;

    }

    public void gotoFieldNext() {

        // sanity check - we where getting null pointers after a restore of screen
        //   and cursor was not positioned on a field when returned
        //   *** Note *** to myself
        //   maybe this is fixed I will have to check this some time
        int lastPos = screen.getLastPos();

        if (currentField == null && (sizeFields != 0) && !isInField(lastPos, true)) {
            final int pos = lastPos;
            screen.setCursorOff();
            screen.advancePos();
            lastPos = screen.getLastPos();
            while (!isInField() && pos != lastPos) {
                screen.advancePos();
            }
            screen.setCursorOn();
        }

        // if we are still null do nothing
        if (currentField == null)
            return;

        ScreenField sf = currentField;

        if (!sf.withinField(lastPos)) {
            screen.setCursorOff();

            if (sizeFields > 0) {

                // lets get the current position so we can test if we have looped
                //    the screen and not found a valid field.
                final int pos = lastPos;
                final int savPos = lastPos;
                boolean done = false;
                do {
                    screen.advancePos();
                    lastPos = screen.getLastPos();
                    if (isInField(lastPos)
                            || pos == lastPos) {
                        if (!currentField.isBypassField()) {
                            screen.gotoField(currentField);
                            done = true;
                        }
                    }
                } while (!done && lastPos != savPos);
            }
            currentModified = false;
            screen.setCursorOn();

        } else {
            if (!cpfExists) {
                do {

                    sf = sf.next;
                }
                while (sf != null && sf.isBypassField());

            } else {
                int f = 0;
                final int cp = sf.getCursorProgression();

                if (cp == 0) {
                    do {

                        sf = sf.next;
                    }
                    while (sf != null && sf.isBypassField());

                } else {
                    ScreenField sf1 = null;
                    boolean found = false;
                    while (!found && f < sizeFields) {

                        sf1 = screenFields[f++];
                        if (sf1.getFieldId() == cp)
                            found = true;
                    }
                    if (found)
                        sf = sf1;
                    else {
                        do {
                            sf = sf.next;
                        }
                        while (sf != null && sf.isBypassField());

                    }
                    sf1 = null;
                }
            }
            if (sf == null)
                screen.gotoField(1);
            else {
                currentField = sf;
                screen.gotoField(currentField);
            }

            currentModified = false;

        }
    }

    public void gotoFieldPrev() {

        ScreenField sf = currentField;
        int lastPos = screen.getLastPos();

        if (!sf.withinField(lastPos)) {
            screen.setCursorOff();

            if (sizeFields > 0) {
                // lets get the current position so we can test if we have looped
                //    the screen and not found a valid field.
                final int pos = lastPos;
                final int savPos = lastPos;
                boolean done = false;

                do {
                    screen.changePos(-1);
                    lastPos = screen.getLastPos();

                    if (isInField(lastPos)
                            || (pos == lastPos)) {

                        if (!currentField.isBypassField()) {
                            screen.gotoField(currentField);
                            done = true;
                        }
                    }
                } while (!done && lastPos != savPos);
            }
            screen.setCursorOn();

        } else {

            if (sf.startPos() == lastPos) {
                if (!cpfExists) {

                    do {
                        sf = sf.prev;
                    }
                    while (sf != null && sf.isBypassField());
                } else {

                    int f = 0;
                    final int cp = sf.getFieldId();
                    ScreenField sf1 = null;
                    boolean found = false;
                    while (!found && f < sizeFields) {

                        sf1 = screenFields[f++];
                        if (sf1.getCursorProgression() == cp)
                            found = true;
                    }
                    if (found)
                        sf = sf1;
                    else {
                        do {
                            sf = sf.prev;
                        }
                        while (sf != null && sf.isBypassField());
                    }
                    sf1 = null;
                }
            }

            if (sf == null) {
                int size = sizeFields;
                sf = screenFields[size - 1];

                while (sf.isBypassField() && size-- > 0) {
                    sf = screenFields[size];

                }
            }
            currentField = sf;
            currentModified = false;
            screen.gotoField(currentField);
        }
    }

    protected void readFormatTable(final Buffer baosp, final int readType, final ICodePage codePage) {

        ScreenField sf;
        boolean isSigned = false;
        char c;

        if (masterMDT) {

            final StringBuffer sb = new StringBuffer();
            for (int x = 0; x < sizeFields; x++) {
                isSigned = false;

                sf = screenFields[x];

                if (sf.mdt || (readType == CMD_READ_INPUT_FIELDS)) {

                    sb.setLength(0);
                    sb.append(sf.getText());


                    if (readType == CMD_READ_MDT_FIELDS ||
                            readType == CMD_READ_MDT_IMMEDIATE_ALT) {
                        int len = sb.length() - 1;

                        // we strip out all '\u0020' and less
                        while (len >= 0 &&
//                     (sb.charAt(len) <= ' ' || sb.charAt(len) >= '\uff20' )) {
                                (sb.charAt(len) < ' ' || sb.charAt(len) >= '\uff20')) {

                            // if we have the dup character and dup is enabled then we
                            //    stop here
                            if (sb.charAt(len) == 0x1C && sf.isDupEnabled())
                                break;

                            sb.deleteCharAt(len--);
                        }

                    }

//               System.out.println("field " + sf.toString());
//               System.out.println(">" + sb.toString() + "<");
//               System.out.println(" field is all nulls");
                    if (sf.isSignedNumeric() && sb.length() > 0 && sb.charAt(sb.length() - 1) == '-') {
                        isSigned = true;
                        sb.setLength(sb.length() - 1);
                    }

                    final int len3 = sb.length();

                    if (len3 > 0 || (readType == CMD_READ_MDT_FIELDS ||
                            readType == CMD_READ_MDT_IMMEDIATE_ALT)) {

                        if ((readType == CMD_READ_MDT_FIELDS ||
                                readType == CMD_READ_MDT_IMMEDIATE_ALT)) {

                            baosp.write(17);   // start of field data
                            if (sf.isSelectionField()) {
                                baosp.write(screen.getRow(sf.selectionPos) + 1);
                                baosp.write(screen.getCol(sf.selectionPos) + 1);
                            } else {
                                baosp.write(sf.startRow() + 1);
                                baosp.write(sf.startCol() + 1);
                            }

                        }
//                  int len = sb.length();
                        if (sf.isSelectionField()) {
                            baosp.write(0);
                            baosp.write(sf.selectionIndex + 0x1F);

                        } else {
                        	
                        	baosp.write(codePage.string2bytes(sb.toString()));
                        	/*
                            for (int k = 0; k < len3; k++) {
                                c = sb.charAt(k);
                                // here we have to check for special instances of the
                                //    characters in the string field.  Attribute bytes
                                //    are encoded with an offset of \uff00
                                //    This is a hack !!!!!!!!!!!
                                //    See ScreenField object for a description
                                if (c < ' ' || c >= '\uff20') {

                                    // if it is an offset attribute byte we just pass
                                    //    it straight on to the output stream
                                    if (c >= '\uff20' && c <= '\uff3f') {
                                        baosp.write(c - '\uff00');
                                    } else
                                        // check for dup character
                                        if (c == 0x1C)
                                            baosp.write(c);
                                        else
                                            baosp.write(codePage.char2bytes(' '));
                                } else {
                                    if (isSigned && k == len3 - 1) {
                                        baosp.write(0xd0 | (0x0f & c));
                                    } else
                                        baosp.write(codePage.char2bytes(c));

                                }
                            }*/
                        }
                    }
                }
            }
        }
    }

}
