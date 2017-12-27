/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import vavi.apps.jwindiff.Model.ShowExpandMode;
import vavi.apps.jwindiff.Model.ShowNumMode;
import vavi.apps.jwindiff.Pair.Type;


/**
 * SimpleListCellRenderer.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 040612 vavi refactoring <br>
 */
class SimpleListCellRenderer extends DefaultListCellRenderer {

    /** */
    private static final ResourceBundle rb = ResourceBundle.getBundle("JWinDiffResources", Locale.getDefault());

    /** */
    private Model model;

    /** */
    SimpleListCellRenderer(Model model) {
        this.model = model;
    }

    /** */
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(list,
                                                         value,
                                                         index,
                                                         isSelected,
                                                         cellHasFocus);
        if (value instanceof Pair) {
            setPair((Pair) value, index, (int) list.getSize().getWidth(), list.getFontMetrics(list.getFont()));
        } else if (value instanceof Line) {
            setLine((Line) value, index);
        }

        return c;
    }

    /** */
    private void setPair(Pair pair, int index, int width, FontMetrics fontMetrics) {

        if (pair.getDiff().isDifferent()) {
            setForeground(Color.red);
        }
        if (pair.getMarked()) {
            setBackground(Color.yellow);
        }
        setOpaque(true);

        String s = new String();
        if (!model.getShowNumMode().equals(ShowNumMode.none)) {
            s += toInt5(index + 1);
        }
        if (model.isMultiMode()) {
            s += toStringN(pair.getCommonName(), width / 2, fontMetrics);
        } else {
            s += toStringN(pair.getLeft() + " : " + pair.getRight(), width / 2, fontMetrics);
        }

        s += getDescription(pair);

        setText(s);
    }

    /**
     * Returns a string describing the difference between two files.
     * Assumption: at least of the two files is indeed a REGULARFILE
     */
    private String getDescription(Pair pair) {
        if (pair.left == null) {
            return MessageFormat.format(rb.getString("pair.description.only"), pair.rightFilePath);
        } else if (pair.right == null) {
            return MessageFormat.format(rb.getString("pair.description.only"), pair.leftFilePath);
        } else if (pair.diff == Type.IDENTICAL) {
            return rb.getString("pair.description.identical");
        } else if (pair.diff == Type.DIFFERENT_BLANKS) {
            return rb.getString("pair.description.differentBlanks");
        } else if (pair.diff == Type.DIFFERENT || pair.diff == Type.DIFFERENT_NOTSURE) {
            if (pair.left.lastModified() < pair.right.lastModified()) {
                return MessageFormat.format(rb.getString("pair.description.different"), pair.rightFilePath);
            } else if (pair.left.lastModified() > pair.right.lastModified()) {
                return MessageFormat.format(rb.getString("pair.description.different"), pair.leftFilePath);
            } else {
                return rb.getString("pair.description.differentSametime");
            }
        } else {
            return rb.getString("pair.description.error");
        }
    }

    /**
     * @param index for debug.
     */
    private void setLine(Line line, int index) {
        if (line.getFlag() == Line.Type.DELETED) {
            setBackground(Color.red);
        } else if (line.getFlag() == Line.Type.INSERTED) {
            setBackground(Color.yellow);
        }
        setOpaque(true);

        String s = new String();
//      String s = new String(toInt5(index) + " "); // TODO debug
        if (!model.getShowNumMode().equals(ShowNumMode.none) &&
            !(model.getShowExpandMode().equals(ShowExpandMode.left) && line.getFlag() == Line.Type.INSERTED) &&
            !(model.getShowExpandMode().equals(ShowExpandMode.right) && line.getFlag() == Line.Type.DELETED)) {
            s += toInt5(line.getLineNumber() + 1) + " ";
        }

        // TODO, TAB
        switch (model.getShowExpandMode()) {
        case left:
            if (line.getFlag() == Line.Type.PLAIN)
            s += "  " + "   " + replaceTabToSpace(line.getLine());
            else if (line.getFlag() == Line.Type.DELETED)
            s += "<!" + "   " + replaceTabToSpace(line.getLine());
            break;
        case right:
            if (line.getFlag() == Line.Type.PLAIN)
            s += "  " + "   " + replaceTabToSpace(line.getLine());
            else if (line.getFlag() == Line.Type.INSERTED)
            s += "!>" + "   " + replaceTabToSpace(line.getLine());
            break;
        case both:
            s += (line.getFlag() == Line.Type.DELETED  ? "<!" :
                  line.getFlag() == Line.Type.INSERTED ? "!>" : "  ") +
            "   " +
            replaceTabToSpace(line.getLine());
            break;
        }
        setText(s);
    }

    /** TODO */
    private final String toInt5(int i) {
        String s = "    " + String.valueOf(i);
        return s.substring(s.length() - 5);
    }

    /** TODO */
    private final String toStringN(String v, int w, FontMetrics fontMetrics) {
        int width = fontMetrics.stringWidth(v);
        int n = 1;
        String s;
        if (width > w) {
            do {
                s = v.substring(0, v.length() - n);
                n++;
                width = fontMetrics.stringWidth(s);
            } while (width > w);
        } else {
            do {
                s = v + spaceN(n);
                n++;
                width = fontMetrics.stringWidth(s);
            } while (width < w);
        }

        return s;
    }

    /** TODO */
    private final String spaceN(int n) {
//Debug.println(n);
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /** TODO 漢字等の文字幅に対応していない */
    private final String replaceTabToSpace(String s) {
        int p = 0;
        while (true) {
            p = s.indexOf('\t', p);
            if (p == -1) {
                break;
            }
            String tab = spaceN(8 - p % 8);
            String sb = s.substring(0, p);
            String sa = s.substring(p + 1);
            s = sb + tab + sa;
        }

        return s;
    }
}

/* */

