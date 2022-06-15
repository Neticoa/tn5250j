package org.tn5250j.gui;
/*
=====================================================================

  ColumnComparator.java

  Created by Claude Duguay
  Copyright (c) 2002
   This was taken from a Java Pro magazine article
   http://www.fawcette.com/javapro/codepage.asp?loccode=jp0208

   I have NOT asked for permission to use this.

=====================================================================
*/

import java.util.Comparator;
import java.util.Vector;

public class ColumnComparator implements Comparator<Object> {
    protected int index;
    protected boolean ascending;

    public ColumnComparator(final int index, final boolean ascending) {
        this.index = index;
        this.ascending = ascending;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(final Object one, final Object two) {
        if (one instanceof Vector && two instanceof Vector) {
            final Vector<?> vOne = (Vector<?>) one;
            final Vector<?> vTwo = (Vector<?>) two;
            final Object oOne = vOne.elementAt(index);
            final Object oTwo = vTwo.elementAt(index);
            if (oOne instanceof Comparable && oTwo instanceof Comparable) {
                final Comparable<Object> cOne = (Comparable<Object>) oOne;
                final Comparable<Object> cTwo = (Comparable<Object>) oTwo;
                if (ascending) {
                    return cOne.compareTo(cTwo);
                } else {
                    return cTwo.compareTo(cOne);
                }
            }
        }

        return 1;
    }
}
