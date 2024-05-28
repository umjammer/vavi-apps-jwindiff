/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import vavi.util.Debug;
import vavi.util.diff.Diff;
import vavi.util.diff.DiffUtil;


/**
 * Pair.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 040612 vavi refactoring <br>
 */
class Pair {

    /** TODO init */
    Path rightDir;

    /** TODO init */
    Path leftDir;

    /**
     *
     */
    enum Type {
        /**  0 ..... */
        NOTYETDIFFED(false),
        /**  1 ....+ */
        DIFFERENT_NOTSURE(true),
        /**  3 ...++ */
        DIFFERENT_BLANKS(true),
        /**  5 ..+.+ */
        DIFFERENT(true),
        /**  8 .+... */
        IDENTICAL(false),
        /** 16 +.... */
        INCOMPARABLE(false);
        /* */
        final boolean different;
        /** */
        Type(boolean different) {
            this.different = different;
        }
        /** */
        boolean isDifferent() {
            return different;
        }
    }

    /** */
    Path left;

    /** */
    public void setLeft(Path left) {
        this.left = left;
    }

    /** */
    public Path getLeft() {
        return left;
    }

    /** */
    Path right;

    /** */
    public void setRight(Path right) {
        this.right = right;
    }

    /** */
    public Path getRight() {
        return right;
    }

    /** saved diff results for this pair of files */
    Diff.Change script;

    /** this file has been marked */
    boolean marked;

    /** */
    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    /** */
    public boolean getMarked() {
        return marked;
    }

    /** results of diffing the two files */
    Type diff = Type.NOTYETDIFFED;

    /** */
    public void setDiff(Type diff) {
        this.diff = diff;
    }

    /** */
    public Type getDiff() {
        return diff;
    }

    /** */
    public Pair(Path lbase, Path left, Path rbase, Path right) {
        this.leftDir = lbase;
        this.left  = left;
        this.rightDir = rbase;
        this.right = right;
    }

    /**
     * Rescans the currently selected file.
     */
    public void rescan() {
        rescanInternal();
        quickDiff();
    }

    /** */
    private void rescanInternal() {
        if (left == null) {
            Path file = leftDir.resolve(right.getFileName());
            if (Files.exists(file)) {
                left = file;
Debug.println("new left: " + left);
            }
        } else if (right == null) {
            Path file = rightDir.resolve(left.getFileName());
            if (Files.exists(file)) {
                right = file;
Debug.println("new right: " + right);
            }
        } else {
            if (!Files.exists(left)) {
                left = null;
Debug.println("disappear left");
            } if (!Files.exists(right)) {
                right = null;
Debug.println("disappear right");
            }
        }
    }

    /** */
    public String getCommonName() {
        try {
            if (right == null) {
                if (left.startsWith(leftDir)) {
                    return "./" + left.getFileName();
                } else {
                    return left.toString();
                }
            } else /* if (left == null) */ {
                if (right.startsWith(rightDir)) {
                    return "./" + right.getFileName();
                } else {
                    return right.toString();
                }
            }
        } catch (NullPointerException e) {
Debug.printStackTrace(e);
            return null;
        }
    }

    /**
     * Compare two files with specified names
     * This is self-contained; it opens the files and closes them.
     * Value is 0 if files are the same, 1 if different,
     * 2 if there is a problem opening them.
     */
    private void compare(boolean ignoreWhiteSpace) throws IOException {

        String[] a;
        String[] b;

        if (ignoreWhiteSpace) {
            a = DiffUtil.readLinesIgnoreWhiteSpace(left.toFile());
            b = DiffUtil.readLinesIgnoreWhiteSpace(right.toFile());
        } else {
            a = DiffUtil.readLines(left.toFile());
            b = DiffUtil.readLines(right.toFile());
        }

        Diff d = new Diff(a, b);
        script = d.getChange(false);
    }

    /**
     *
     */
    public void quickDiff() {

        if (left == null || right == null) {
            return;
        }

        // to begin with, allow blank-only diffs to show up
        // regardless of the current mode (for speed)
        try {
            if (!isBinaryFile(left)) {
                compare(false);
                diff = script == null ? Type.IDENTICAL : Type.DIFFERENT_NOTSURE;
            } else {
                diff = compareBinary() ?  Type.IDENTICAL : Type.DIFFERENT_NOTSURE;
            }
        } catch (IOException e) {
Debug.printStackTrace(e);
            diff = Type.INCOMPARABLE;
        }
    }

    private static final Map<Path, Boolean> isBinCache = new HashMap<>();

    /** */
    static boolean isBinaryFile(Path file) throws IOException {
        Boolean result = isBinCache.get(file);
        if (result == null) {
            result = false;
            int bufferSize = 1024; // Read 1 KB of the file
            byte[] buffer = new byte[bufferSize];
            try (InputStream fis = Files.newInputStream(file)) {
                int bytesRead = fis.read(buffer, 0, bufferSize);
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    if (b < 0x09 || (b > 0x0D && b < 0x20) || b == 0x7F) {
                        result = true;
                        break;
                    }
                }
            }
            isBinCache.put(file, result);
        }
        return result;
    }

    /** TODO md5? */
    boolean compareBinary() throws IOException {
        return Files.size(left) == Files.size(right);
    }

    /** */
    public void slowDiff(boolean isIgnoreBlanks) {
        try {
            compare(isIgnoreBlanks);
            if (script == null) {
                quickDiff();
                if (diff == Type.IDENTICAL) {
                    diff = Type.IDENTICAL;
                } else {
                    diff = Type.DIFFERENT_BLANKS;
                }
            } else {
                diff = Type.DIFFERENT;
            }
        } catch (IOException e) {
            Debug.println(Level.SEVERE, e);
            diff = Type.INCOMPARABLE;
        }
    }

    /**
     * Determines whether the given pair should currently be visible in outline
     * mode.
     */
    public boolean isVisible(boolean identical, boolean showLeftOnly, boolean showRightOnly, boolean showDifferent, boolean hideMarked) {
        if (diff.equals(Type.IDENTICAL) && !identical) {
            return false;
        }
        if (right == null && !showLeftOnly) {
            return false;
        }
        if (left == null && !showRightOnly) {
            return false;
        }
        if (diff.isDifferent() && !showDifferent) {
            return false;
        }
        if (marked && hideMarked) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "left: " + left +
                ", lbase: " + leftDir +
                ", right: " + right +
                ", rbase: " + rightDir +
                ", diff: " + diff +
                ", marked: " + marked +
                ", script: " + script +
                ", commond: " + getCommonName();
    }
}
