/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;
import vavi.util.event.GenericSupport;


/**
 * Model.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 040827 nsano initial version <br>
 */
class Model {

    /** view listener TODO remove, use proxy? */
    private GenericSupport gs = new GenericSupport();

    /** */
    public void addViewListener(GenericListener listener) {
        gs.addGenericListener(listener);
    }

    /** */
    public void viewUpdated(Object source, String name, Object ... arg) {
        gs.fireEventHappened(new GenericEvent(source, name, arg));
    }

    //----

    // BASIC TYPES
    enum DisplayMode {
        NONE_MODE,
        OUTLINE_MODE,
        EXPANDED_MODE
    }

    /** Current display mode */
    DisplayMode displayMode = DisplayMode.OUTLINE_MODE;

    /** The list of files to be compared. */
    List<Pair> pairs = new ArrayList<>();

    /** The currently selected pair. */
    Pair current;

    /** */
    private List<File> leftFiles  = new ArrayList<>();

    /** */
    private List<File> rightFiles = new ArrayList<>();

    /** dirs (true) or files (false) */
    boolean multiMode;

    /** */
    enum ShowExpandMode {
        left,
        right,
        both
    }

    /** expand */
    ShowExpandMode showExpandMode = ShowExpandMode.both;

    /** */
    enum ShowNumMode {
        left,
        right,
        none
    }

    /** expand */
    ShowNumMode showNumMode = ShowNumMode.left;

    /** mark */
    boolean hideMarked;

    /** option */
    boolean ignoreBlanks;
    /** option */
    boolean showDifferent;
    /** option */
    boolean showIdentical;
    /** option */
    boolean showLeft;
    /** option */
    boolean showRight;

    /**
     * Gets a canonical path of the first element of files.
     */
    private static String getPath(List<File> files) {
        return files.get(0).getPath();
    }

    /** */
    public String getRightFilePath() {
        return getPath(rightFiles);
    }

    /** */
    public String getLeftFilePath() {
        return getPath(leftFiles);
    }

    /**
     * @return files
     */
    public List<File> getLeftFiles() {
        return leftFiles;
    }

    /**
     * @return files
     */
    public List<File> getRightFiles() {
        return rightFiles;
    }

    /**
     * dirs or files
     */
    public boolean isMultiMode() {
        return multiMode;
    }

    /**
     * dirs or files
     */
    public void setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
    }

    /**
     * expand
     */
    public ShowExpandMode getShowExpandMode() {
        return showExpandMode;
    }

    /**
     * expand
     */
    public void setShowExpandMode(ShowExpandMode showExpandMode) {
        this.showExpandMode = showExpandMode;
    }

    /**
     * expand
     */
    public ShowNumMode getShowNumMode() {
        return showNumMode;
    }

    /**
     * expand
     */
    public void setShowNumMode(ShowNumMode showNumMode) {
        this.showNumMode = showNumMode;
    }

    /**
     * mark
     */
    public boolean isHideMarked() {
        return hideMarked;
    }

    /**
     * mark
     */
    public void setHideMarked(boolean hideMarked) {
        this.hideMarked = hideMarked;
    }

    /**
     * option
     */
    public boolean isIgnoreBlanks() {
        return ignoreBlanks;
    }

    /**
     * option
     */
    public void setIgnoreBlanks(boolean ignoreBlanks) {
        this.ignoreBlanks = ignoreBlanks;
    }

    /**
     * option
     */
    public boolean isShowDifferent() {
        return showDifferent;
    }

    /**
     * option
     */
    public void setShowDifferent(boolean showDifferent) {
new Exception("*** DUMMY ***").printStackTrace();
        this.showDifferent = showDifferent;
    }

    /**
     * option
     */
    public boolean isShowIdentical() {
        return showIdentical;
    }

    /**
     * option
     */
    public void setShowIdentical(boolean showIdentical) {
        this.showIdentical = showIdentical;
    }

    /**
     * option
     */
    public boolean isShowLeft() {
        return showLeft;
    }

    /**
     * option
     */
    public void setShowLeft(boolean showLeft) {
        this.showLeft = showLeft;
    }

    /**
     * option
     */
    public boolean isShowRight() {
        return showRight;
    }

    /**
     * option
     */
    public void setShowRight(boolean showRight) {
        this.showRight = showRight;
    }

    //----

    /**
     * Toggle the marked state of all files.
     */
    void toggleAllMarks() {
        for (Pair pair : pairs) {
            pair.setMarked(!pair.getMarked());
        }
    }

    /**
     * Perform the processing. Called at start of day and when the targets
     * change.
     */
    void updateTargets() {
        if (leftFiles.size() == 0 || rightFiles.size() == 0) {
            displayMode = DisplayMode.NONE_MODE;
            return;
        }

        // Figure out which mode we should start in

        File left = leftFiles.get(0);
        File right = rightFiles.get(0);

        multiMode = right.isDirectory() || left.isDirectory();

        displayMode = multiMode ? DisplayMode.OUTLINE_MODE : DisplayMode.EXPANDED_MODE;

        // Get rid of any pre-existing list of files.

// Debug.println(isMultiMode);
        if (multiMode) {
            makePairs(left, right);
            setMarked();
        } else {
            current = new Pair(getLeftFilePath(), left, getRightFilePath(), right);
            pairs.clear();
            pairs.add(current);
        }
    }

    /** */
    void rescan(Pair[] pairs) {
        for (Pair pair : pairs) {
            pair.rescan();
Debug.println(pair.getCommonName());
        }
    }

    /**
     * Build up a comprehensive list of files.
     *
     * @param ldir the directory to be added to the file list.
     * @param rdir same as the left.
     */
    void makePairs(File ldir, File rdir) {

        List<File> lfiles = new ArrayList<>();
        List<File> rfiles = new ArrayList<>();
        fillFileList(ldir, lfiles);
        fillFileList(rdir, rfiles);

        makePairs(lfiles, rfiles);
    }

    /**
     * Build up a comprehensive list of files.
     *
     * @param lfiles the file list.
     * @param rfiles same as the left.
     */
    void makePairs(List<File> lfiles, List<File> rfiles) {
        pairs.clear();

        for (File lfile : lfiles) {
            File rfile = findIn(rfiles, lfile);
            if (rfile != null) {
                pairs.add(new Pair(getLeftFilePath(), lfile, getRightFilePath(), rfile));
                rfiles.remove(rfile);
            } else {
                pairs.add(new Pair(getLeftFilePath(), lfile, getRightFilePath(), null));
            }
        }

        for (File rfile : rfiles) {
            pairs.add(new Pair(getLeftFilePath(), null, getRightFilePath(), rfile));
        }

        pairs.sort((o1, o2) -> {
            String first = o1.getCommonName();
            String second = o2.getCommonName();
            return first.compareToIgnoreCase(second);
        });
    }

    /**
     * Fills an array of File for the contents of a given directory.
     *
     * @param directory path to the required directory
     */
    private void fillFileList(File directory, List<File> entries) {

        File[] files = directory.listFiles();

        for (File file : files) {

            if (file.isDirectory()) {
                fillFileList(file, entries);
            } else {
                entries.add(file);
            }
        }
    }

    /**
     * Look for the given name in the name field of a given array of Files.
     *
     * @return null if not found.
     */
    private File findIn(List<File> rfiles, File lfile) {

        try {
            String lt = getLeftFilePath();
            String lf = lfile.getPath();
            if (lf.startsWith(lt)) {
                lf = lf.substring(lt.length() + 1);
// Debug.println("lf: " + lf);
            }
            String rt = getRightFilePath();
            for (File rfile : rfiles) {
                String rf = rfile.getPath();
                if (rf.startsWith(rt)) {
                    rf = rf.substring(rt.length() + 1);
                }
// Debug.println("rf: " + rf);
                if (lf.equals(rf)) {
                    return rfile;
                }
            }
        } catch (NullPointerException e) {
            Debug.printStackTrace(e);
        }

        return null;
    }

    /**
     * Toggle the marked state of selected file.
     */
    void toggleSelectedMark(Pair[] selected) {
        if (displayMode == Model.DisplayMode.OUTLINE_MODE) {
            for (Pair pair : selected) {
                pair.setMarked(!pair.getMarked());
            }
        }
    }

    /**
     * Mark all files which match a given regex.
     */
    void markRegex(String regex) {
        try {
            Pattern p = Pattern.compile(regex);
            for (Pair pair : pairs) {
                Matcher m = p.matcher(pair.getCommonName());
                if (m.find()) {
                    pair.setMarked(true);
                }
            }
        } catch (PatternSyntaxException e) {
            Debug.println(e);
        }
    }

    /**
     * mark by regex markPatterns
     */
    void setMarked() {
        for (String pattern : markPatterns) {
            markRegex(pattern);
        }
    }

    // serializer ----

    /** */
    private static final ResourceBundle rb = ResourceBundle.getBundle("JWinDiffResources", Locale.getDefault());

    /** TODO use prefs */
    void serializePairs(OutputStream os) throws IOException {

        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));

        int count = 0;

        String s = MessageFormat.format(rb.getString("filelist.header"), getLeftFilePath(), getRightFilePath());
        w.write(s);
        w.newLine();

        if (showIdentical) {
            w.write(rb.getString("filelist.message.0"));
            w.newLine();

            for (Pair pair : pairs) {
                if (pair.getMarked() && hideMarked) {
                    continue;
                }
                if (pair.getDiff() == Pair.Type.IDENTICAL) {
                    w.write(getDescriptionForListing(pair));
                    w.newLine();
                    count++;
                }
            }
        }
        if (showDifferent) {
            w.write(rb.getString("filelist.message.1"));
            w.newLine();

            for (Pair pair : pairs) {
                if (pair.getMarked() && hideMarked) {
                    continue;
                }
                if (pair.getDiff().isDifferent()) {
                    w.write(getDescriptionForListing(pair));
                    w.newLine();
                    count++;
                }
            }

        }
        if (showLeft) {
            w.write(rb.getString("filelist.message.2"));
            w.newLine();

            for (Pair pair : pairs) {
                if (pair.getMarked() && hideMarked) {
                    continue;
                }
                if (pair.getRight() == null || !pair.getRight().exists()) {

                    w.write(getDescriptionForListing(pair));
                    w.newLine();
                    count++;
                }
            }
        }
        if (showRight) {
            w.write(rb.getString("filelist.message.3"));
            w.newLine();

            for (Pair pair : pairs) {
                if (pair.getMarked() && hideMarked) {
                    continue;
                }
                if (pair.getLeft() == null || !pair.getLeft().exists()) {

                    w.write(getDescriptionForListing(pair));
                    w.newLine();
                    count++;
                }
            }
        }

        s = MessageFormat.format(rb.getString("filelist.footer"), count, (count != 1) ? "s" : "");
        w.write(s);
        w.newLine();
    }

    /** */
    private String getDescriptionForListing(Pair pair) {
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return pair.getCommonName() + "," + sdf.format(new Date(pair.left.lastModified())) + "," + pair.left.length() + "," + sdf.format(new Date(pair.right.lastModified())) + "," + pair.right.length();
    }

    /** TODO use prefs */
    void serialize(OutputStream os) throws IOException {

        Properties props = new Properties();

        props.setProperty(PROP_EDITOR, editor != null ? editor : "");
        props.setProperty(PROP_EXPAND, showExpandMode.name());
        props.setProperty(PROP_NUMBERS, showNumMode.name());
        props.setProperty(PROP_IGNOREBLANKS, String.valueOf(ignoreBlanks));
        props.setProperty(PROP_SHOWIDENTICAL, String.valueOf(showIdentical));
        props.setProperty(PROP_SHOWLEFTONLY, String.valueOf(showLeft));
        props.setProperty(PROP_SHOWRIGHTONLY, String.valueOf(showRight));
        props.setProperty(PROP_SHOWDIFFERENT, String.valueOf(showDifferent));
        props.setProperty(PROP_HIDEMARKED, String.valueOf(hideMarked));

        // history

        int i = 1;
        for (String pattern : markPatterns) {
            String name = "history.pattern." + i++;
            props.setProperty(name, pattern);
        }

        i = 1;
        for (String pattern : dir1Patterns) {
            String name = "dir1.pattern." + i++;
            props.setProperty(name, pattern);
        }

        i = 1;
        for (String pattern : dir2Patterns) {
            String name = "dir2.pattern." + i++;
            props.setProperty(name, pattern);
        }


        props.store(os, "JWinDiff");
    }

    // deserializer ----

    /** The command string to invoke editor */
    String editor = System.getProperty("EDITOR");

    /** The configuration properties */
    Set<String> markPatterns = new HashSet<>();

    /** The configuration properties */
    Set<String> dir1Patterns = new HashSet<>();

    /** The configuration properties */
    Set<String> dir2Patterns = new HashSet<>();

    private static final String PROP_EDITOR = "editor";
    private static final String PROP_EXPAND = "expand";
    private static final String PROP_NUMBERS = "numbers";
    private static final String PROP_IGNOREBLANKS = "ignoreblanks";
    private static final String PROP_SHOWIDENTICAL = "showidentical";
    private static final String PROP_SHOWLEFTONLY = "showleftonly";
    private static final String PROP_SHOWRIGHTONLY = "showrightonly";
    private static final String PROP_SHOWDIFFERENT = "showdifferent";
    private static final String PROP_HIDEMARKED = "hidemarked";

    /** */
    void deserialize(InputStream is) throws IOException {

        Properties props = new Properties();

        props.load(is);

        // spin through the file setting configuration options

        String value = props.getProperty(PROP_EDITOR);
        if (value != null) {
            editor = value;
        }

        value = props.getProperty(PROP_EXPAND, "both");
        showExpandMode = ShowExpandMode.valueOf(value);

        value = props.getProperty(PROP_NUMBERS, "none");
        showNumMode = ShowNumMode.valueOf(value);

        value = props.getProperty(PROP_IGNOREBLANKS, "false");
        ignoreBlanks = Boolean.parseBoolean(value);
        value = props.getProperty(PROP_SHOWIDENTICAL, "true");
        showIdentical = Boolean.parseBoolean(value);
        value = props.getProperty(PROP_SHOWLEFTONLY, "false");
        showLeft = Boolean.parseBoolean(value);
        value = props.getProperty(PROP_SHOWRIGHTONLY, "false");
        showRight = Boolean.parseBoolean(value);
        value = props.getProperty(PROP_SHOWDIFFERENT, "true");
        showDifferent = Boolean.parseBoolean(value);
        value = props.getProperty(PROP_HIDEMARKED, "true");
        hideMarked = Boolean.parseBoolean(value);

        // history

        int i = 1;
        while (i < 100) {
            String name = "history.pattern." + i++;
            value = props.getProperty(name);
            if (value != null && !"".equals(value)) {
                markPatterns.add(value);
            } else {
                break;
            }
        }

        i = 1;
        while (i < 100) {
            String name = "dir1.pattern." + i++;
            value = props.getProperty(name);
            if (value != null && !"".equals(value)) {
                dir1Patterns.add(value);
            } else {
                break;
            }
        }

        i = 1;
        while (i < 100) {
            String name = "dir2.pattern." + i++;
            value = props.getProperty(name);
            if (value != null && !"".equals(value)) {
                dir2Patterns.add(value);
            } else {
                break;
            }
        }
    }
}

/* */
