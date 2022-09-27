/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;

import vavi.apps.jwindiff.Model.DisplayMode;
import vavi.apps.jwindiff.Model.ShowExpandMode;
import vavi.apps.jwindiff.Model.ShowNumMode;
import vavi.swing.mvc.SwingControllerAction;
import vavi.swing.mvc.XController;
import vavi.util.Debug;
import vavi.util.diff.DiffUtil;


/**
 * Controller.
 * アプリケーションそのもの。View を除いたもの。
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 060726 nsano initial version <br>
 */
@XController
class Controller {

    /** */
    private static final ResourceBundle rb = ResourceBundle.getBundle("JWinDiffResources", Locale.getDefault());

    enum Order {
        Ascent,
        Descent
    }

    /** モデル */
    Model model;

    /** */
    Controller(Model model) {
        this.model = model;
    }

    //----

    /**
     * Rescans the currently selected file.
     * TODO this method seems to be in model but param is view-model
     * @param pairs comes from view-model (selected)
     * @controller
     */
    void rescan(Pair[] pairs) {
        model.viewUpdated(this, "setTitle", rb.getString("frame.title.scanning"));
        if (model.displayMode != Model.DisplayMode.OUTLINE_MODE) {
            model.current.rescan();
        } else {
            model.rescan(pairs);
        }
        finishWork();
        update();
    }

    /**
     * Display outline.
     * @controller
     */
    @SwingControllerAction
    void updateOutline() {

// Debug.println(displayMode);
        if (model.displayMode != Model.DisplayMode.OUTLINE_MODE) {
            return;
        }

        model.viewUpdated(this, "redisplayOutlineBefore", model.getLeftFilePath(), model.getRightFilePath());

        List<Pair> list = new ArrayList<>();

        // set the current visibility of each pair
        for (int i = 0; i < model.pairs.size(); i++) {
            Pair pair = model.pairs.get(i);
            if (pair.getDiff() == Pair.Type.NOTYETDIFFED) {
                pair.quickDiff();
            }
            if (pair.isVisible(model.isShowIdentical(), model.isShowLeft(), model.isShowRight(), model.isShowDifferent(), model.isHideMarked())) {
// pair.debug();
                list.add(pair);
            }
        }

        model.viewUpdated(this, "redisplayOutlineAfter", list, model.current);
    }

    /**
     * Display expanded.
     * @controller
     */
    void updateExpanded() {

        if (model.displayMode != Model.DisplayMode.EXPANDED_MODE) {
            return;
        }

        model.viewUpdated(this, "redisplayExpandedBefore");

        try {
            if (model.current.getRight() == null) {
Debug.println("here1");
                model.viewUpdated(this, "setNames", model.current.getCommonName());
                displaySingleFile(model.current.getLeft());
                model.viewUpdated(this, "setNames", model.current.getCommonName());
            } else if (model.current.getLeft() == null) {
Debug.println("here2");
                model.viewUpdated(this, "setNames", model.current.getCommonName());
                displaySingleFile(model.current.getRight());
                model.viewUpdated(this, "setNames", model.current.getCommonName());
            } else if ((model.current.getDiff() == Pair.Type.NOTYETDIFFED) || (model.current.getDiff() == Pair.Type.DIFFERENT_NOTSURE)) {
Debug.println("here3");
                // diff both (regular) files,
                // and show the newly obtained results
                if (model.isMultiMode()) {
                    model.viewUpdated(this, "setNames", model.current.getCommonName());
                    model.viewUpdated(this, "setPaths", model.getLeftFilePath() + " : " + model.getRightFilePath());
                } else {
                    model.viewUpdated(this, "setNames", model.current.getLeft().getName() + " : " + model.current.getRight().getName());
                    model.viewUpdated(this, "setPaths", model.getLeftFilePath() + " : " + model.getRightFilePath());
                }
                model.current.slowDiff(model.isIgnoreBlanks());
Debug.print(model.current);
                Printer printer = new Printer(model);
                printer.print(model.current.script);
                model.viewUpdated(this, "updateMain", printer.getResult());
            } else if (model.current.getDiff() == Pair.Type.IDENTICAL) {
Debug.println("here4");
                displaySingleFile(model.current.getLeft());
                model.viewUpdated(this, "setNames", model.current.getCommonName());
            } else {
Debug.println("here5");
                // redisplay known diff results.
                Printer printer = new Printer(model);
                printer.print(model.current.script);
                model.viewUpdated(this, "updateMain", printer.getResult());
            }
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
            model.viewUpdated(this, "displayException", e);
        }

        model.viewUpdated(this, "updateGraphics");
    }

    /**
     * Display as selected mode.
     * @controller
     */
    void update() {
// Debug.println(displayMode);
        if (model.displayMode == Model.DisplayMode.OUTLINE_MODE) {
            updateOutline();
        } else {
            updateExpanded();
        }
    }

    /**
     * form
     */
    void displaySingleFile(File file) throws IOException {
// Debug.println("here");
        String[] lines = DiffUtil.readLines(file);
        model.viewUpdated(this, "displaySingleFile", (Object[]) lines);
    }

    /**
     * form
     */
    @SwingControllerAction
    void toExpand() {
        if (model.displayMode == Model.DisplayMode.EXPANDED_MODE) {
            model.viewUpdated(this, "toExpand");
            return;
        }
        if (model.current == null) {
            model.viewUpdated(this, "toOutline");
            return;
        }
        if (!model.current.isVisible(model.isShowIdentical(), model.isShowLeft(), model.isShowRight(), model.isShowDifferent(), model.isHideMarked())) {
Debug.println("here");
Debug.print(model.current);
            model.viewUpdated(this, "toOutline");
            return;
        }
        model.displayMode = Model.DisplayMode.EXPANDED_MODE;
        updateExpanded();
        model.viewUpdated(this, "toSelection");
    }

    /**
     * @controller
     */
    @SwingControllerAction
    void toOutline() {
        if (model.displayMode == Model.DisplayMode.OUTLINE_MODE) {
            model.viewUpdated(this, "toOutline");
            return;
        }
        model.displayMode = Model.DisplayMode.OUTLINE_MODE;
        updateOutline();
    }

    /** The configuration file TODO model??? */
    private static File configFile = new File(System.getProperty("user.home") + File.separator + ".jwindiff");

    /** Loads properties */
    private void loadOptions() {

        // DI properties -> model
        try {
            model.deserialize(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
System.err.println(e);
            saveOptions();
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
        }
    }

    /**
     * Writes out all of the configuration options.
     */
    private void saveOptions() {
        try {
            model.serialize(Files.newOutputStream(configFile.toPath()));
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
        }
    }

    /**
     * @controller unused
     */
    void clearPatternHistory() {
        model.markPatterns.clear();
    }

    /**
     * @controller
     */
    void serializePairs(File file) {
        try {
            OutputStream os = Files.newOutputStream(file.toPath());
            model.serializePairs(os);
            os.close();
        } catch (IOException e) {
Debug.printStackTrace(e);
        }
    }

    /**
     * @controller
     */
    void updateTargets(File left, File right) {
        model.viewUpdated(this, "setTitle", rb.getString("frame.title.scanning"));

        model.getLeftFiles().clear();
        model.getRightFiles().clear();

        try {
            model.getLeftFiles().add(left.getCanonicalFile());
            model.getRightFiles().add(right.getCanonicalFile());
        } catch (IOException e) {
Debug.println(e);
        }

        model.updateTargets();

        finishWork();
    }

    /**
     * @controller
     */
    void setEditor(String editor) {
        model.editor = editor;
    }

    /**
     * expand
     * @controller
     */
    void setShowExpandMode(String name) {
        model.setShowExpandMode(ShowExpandMode.valueOf(name));
        updateExpanded();
    }

    /**
     * expand
     * @controller
     */
    void setShowNumMode(String name) {
        model.setShowNumMode(ShowNumMode.valueOf(name));
        updateExpanded();
    }

    /**
     * option
     * @controller
     */
    void setIgnoreBlanks(boolean ignoreBlanks) {
        model.ignoreBlanks = ignoreBlanks;
    }

    /**
     * option
     * @controller
     */
    void setShowIdentical(boolean showIdentical) {
        model.setShowIdentical(showIdentical);
        updateOutline();
    }

    /**
     * option
     * @controller
     */
    void setShowDifferent(boolean showDifferent) {
        model.setShowDifferent(showDifferent);
        updateOutline();
    }

    /** option */
    void setShowLeft(boolean showLeft) {
        model.setShowLeft(showLeft);
        updateOutline();
    }

    /** option */
    void setShowRight(boolean showRight) {
        model.setShowRight(showRight);
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void setHideMarked(boolean hideMarked) {
        model.setHideMarked(hideMarked);
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    @SwingControllerAction
    void toggleAllMarks() {
        model.toggleAllMarks();
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void toggleSelectedMark(Pair[] selected) {
        model.toggleSelectedMark(selected);
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void markRegex(String regex) {
        if (!regex.isEmpty()) { // TODO is this logic in model?
            model.markRegex(regex);
            model.markPatterns.add(regex);
            updateOutline();
        }
    }

    //----

    /** form */
    void setCurrent(Pair pair) {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            model.current = pair;
        }
    }

    /** form */
    void initMain(String[] args) throws IOException {
        // Get the targets from the command line arguments now that Xt has
        // removed its options
        if (args.length == 1) {
            throw new IllegalArgumentException(rb.getString("message.usage"));
        }

top:    for (int i = 0; i < args.length; i++) {
Debug.println(i + ": arg: " + args[i]);
            if ("-1".equals(args[i])) {
                i++;
                while (i < args.length) {
                    File file = new File(args[i]);
                    if (file.exists()) {
                        file = new File(file.getCanonicalPath());
                        model.getLeftFiles().add(file);
Debug.println(i + ": add left: " + file);
                    } else {
Debug.println(i + ": not exists: " + file);
                        i--;
                        continue top;
                    }
                    i++;
                }
            } else if ("-2".equals(args[i])) {
                i++;
                while (i < args.length) {
                    File file = new File(args[i]);
                    if (file.exists()) {
                        file = new File(file.getCanonicalPath());
                        model.getRightFiles().add(file);
Debug.println(i + ": add right: " + file);
                    } else {
Debug.println(i + ": not exists: " + file);
                        i--;
                        continue top;
                    }
                    i++;
                }
            } else {
                File file = new File(args[i]);
Debug.println("file: " + file);
                if (file.exists()) {
                    if (i == 0) {
                        file = new File(file.getCanonicalPath());
                        model.getLeftFiles().add(file);
Debug.println(i + ": add left: " + file);
                    } else if (i == 1) {
                        if (model.getLeftFiles().size() > 0) {
                            File left = model.getLeftFiles().get(0);
                            if (left.isDirectory() && file.isDirectory()) {
                                file = new File(file.getCanonicalPath());
                                model.getRightFiles().add(file);
Debug.println(i + ": add right as directory: " + file);
                            } else if (!left.isDirectory() && !file.isDirectory()) {
                                file = new File(file.getCanonicalPath());
                                model.getRightFiles().add(file);
Debug.println(i + ": add right as file: " + file);
                            } else {
                                String s = MessageFormat.format(rb.getString("message.error"), left, file);
                                throw new IllegalArgumentException(s);
                            }
                        } else {
                            throw new IllegalArgumentException("no left files");
                        }
                    } else {
Debug.println(i + ":???: " + file);
                    }
                } else {
Debug.println(i + ":ignore: " + file);
                }
            }
        }

        if (args.length != 0 && (model.getLeftFiles().size() == 0 || model.getRightFiles().size() == 0)) {
            throw new IllegalArgumentException(rb.getString("message.usage"));
        }
    }

    /** form */
    void pageMain() {
        loadOptions();
        model.viewUpdated(this, "initMain", model);

        model.viewUpdated(this, "pageMain");

        model.viewUpdated(this, "setTitle", rb.getString("frame.title.scanning"));
        model.updateTargets();
        finishWork();

        // redisplayOutline();
        update();
    }

    /** form */
    @SwingControllerAction
    void pageMain_close() {
        model.viewUpdated(this, "pageMain_close");
        pageMain_close2();
    }

    /** form */
    void pageMain_close2() {
        saveOptions();
        System.exit(0);
    }

    /** form */
    void pagePopup(int x, int y) {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            model.viewUpdated(this, "pagePopupOutline", x, y);
        } else {
            model.viewUpdated(this, "pagePopupExpanded", x, y);
        }
    }

    /** form */
    @SwingControllerAction
    void pageSaveFile() {
        model.viewUpdated(this, "pageSaveFile");
    }

    /** form */
    @SwingControllerAction
    void pageEditorChooser() {
        if (model.editor != null && model.editor.length() != 0) {
            File file = new File(model.editor);
            model.viewUpdated(this, "initEditorDialog", file);
        }
        model.viewUpdated(this, "pageEditorChooser");
    }

    /** form */
    @SwingControllerAction
    void pageCompareTargetsDialog() {
        if (model.displayMode == DisplayMode.NONE_MODE) {
            if (model.getLeftFiles().size() > 0 && model.getRightFiles().size() > 0) {
                File left = model.getLeftFiles().get(0);
                File right = model.getRightFiles().get(0);
                model.viewUpdated(this, "initCompareTargetsDialog", left, right);
            }
        }
        model.viewUpdated(this, "pageCompareTargetsDialog");
    }

    /** form */
    @SwingControllerAction
    void editLeft() {
        if (model.current != null) {
            editFile(model.current.getLeft());
        }
    }

    /** form */
    @SwingControllerAction
    void editRight() {
        if (model.current != null) {
            editFile(model.current.getRight());
        }
    }

    /**
     * Invokes a editor.
     */
    private void editFile(File file) {
        try {
            if (file != null && file.exists() && model.editor != null) {
                Runtime.getRuntime().exec(model.editor + " \"" + file.getAbsolutePath() + "\"");
            }
        } catch (Exception e) {
Debug.printStackTrace(e);
        }
    }

    /**
     * TODO this method seems to be in model but param is view-model
     * @param pairs comes from model-view
     */
    void copyFiles(Pair[] pairs) {
        for (Pair pair : pairs) {
Debug.print(pair);
            if (pair.getLeft() == null || !pair.getLeft().exists()) {
                try {
                    Path file = Paths.get(pair.leftFilePath, pair.getCommonName());
                    Path dir = file.getParent();
                    if (!Files.exists(dir)) {
                        Files.createDirectories(dir);
                    }
                    Files.copy(Paths.get(pair.getRight().toURI()), file);
                    pair.setLeft(file.toFile());
                    pair.rescan();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (pair.getRight() == null || !pair.getRight().exists()) {
                try {
                    Path file = Paths.get(pair.rightFilePath, pair.getCommonName());
                    Path dir = file.getParent();
                    if (!Files.exists(dir)) {
                        Files.createDirectories(dir);
                    }
                    Files.copy(Paths.get(pair.getLeft().toURI()), file);
                    pair.setRight(file.toFile());
                    pair.rescan();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
Debug.println("both files do not exist: " + pair.getCommonName());
            }
        }
        updateOutline();
    }

    /** */
    void findDiff(Order order) {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            model.viewUpdated(this, "findOutline", order);
        } else {
            model.viewUpdated(this, "findExpand", order);
        }
    }

    /**
     * abort (file)
     */
    @SwingControllerAction
    void abortAction() {
        // TODO can't yet interrupt
    }

    /**
     * copyFiles
     */
    @SwingControllerAction(view = "mainView.selectedValuesList")
    void copyFilesAction(List<Object> selection) {
        if (selection.size() == 0 || !(selection.get(0) instanceof Pair)) {
            return;
        }
        Pair[] pairs = selection.toArray(new Pair[0]);
        copyFiles(pairs);
    }

    /**
     * prev
     */
    @SwingControllerAction
    void prevAction() {
        findDiff(Order.Descent);
    }

    /**
     * next
     */
    @SwingControllerAction
    void nextAction() {
        findDiff(Order.Ascent);
    }

    /**
     * rescan
     */
    @SwingControllerAction(view = "mainView.selectedValuesList")
    void rescanAction(List<Object> selection) {
        if (selection.size() == 0 || !(selection.get(0) instanceof Pair)) {
            return;
        }
        Pair[] pairs = selection.toArray(new Pair[0]);
        rescan(pairs);
    }

    /**
     * show mode (expand)
     */
    @SwingControllerAction
    void setShowExpandModeAction(ActionEvent ev) {
        setShowExpandMode(ev.getActionCommand());
    }

    /**
     * num mode (expand)
     */
    @SwingControllerAction
    void setShowNumModeAction(ActionEvent ev) {
        setShowNumMode(ev.getActionCommand());
    }

    /**
     * showIdentical (options)
     */
    @SwingControllerAction(view = "showIdentical.selected")
    void showIdenticalAction(boolean showIdentical) {
        setShowIdentical(showIdentical);
    }

    /**
     * showLeft (options)
     */
    @SwingControllerAction(view = "showLeft.selected")
    void showLeftAction(boolean showLeft) {
        setShowLeft(showLeft);
    }

    /**
     * showRight (options)
     */
    @SwingControllerAction(view = "showRight.selected")
    void showRightAction(boolean showRight) {
        setShowRight(showRight);
    }

    /**
     * showDifferent (options)
     */
    @SwingControllerAction(view = "showDifferent.selected")
    void showDifferentAction(boolean showDifferent) {
        setShowDifferent(showDifferent);
    }

    /**
     * markFile (mark)
     */
    @SwingControllerAction(view = "mainView.selectedValuesList")
    void markFileAction(List<Object> selection) {
        if (selection.size() == 0 || !(selection.get(0) instanceof Pair)) {
            return;
        }
        Pair[] pairs = selection.toArray(new Pair[0]);
        toggleSelectedMark(pairs);
    }

    /**
     * hideMarked (mark)
     */
    @SwingControllerAction(view = "hideMarked.selected")
    void hideMarkedAction(boolean hideMarked) {
        setHideMarked(hideMarked);
    }

    //----

    /**
     * editorDialog: ok
     */
    @SwingControllerAction(view = "editorChooser.selectedFile")
    void okEditorDialogAction(File editor) {
        if (editor.exists()) {
            setEditor(editor.toString());
        }
        model.viewUpdated(this, "pageEditorDialog_close");
    }

    /**
     * patternDialog: ok
     */
    @SwingControllerAction(view = "patternField.selectedItem")
    void okPatternDialogAction(String regex) {
        markRegex(regex);
        model.viewUpdated(this, "pagePatternDialog_close");
    }

    /**
     * targetsDialog: ok
     */
    @SwingControllerAction(view = { "leftTargetChooser.selectedFile", "rightTargetChooser.selectedFile" })
    void okTargetsDialogAction(File left, File right) {
        if (left != null && left.exists() && right != null && right.exists()) {
            updateTargets(left, right);
            update();
        }
        model.viewUpdated(this, "pageCompareTargetsDialog_close");
    }

    /**
     * saveListDialog: ok
     */
    @SwingControllerAction(view = {
        "listFileChooser.selectedFile",
        "hasIdentical.selected",
        "hasDifferent.selected",
        "hasLeft.selected",
        "hasRight.selected",
        "hasNotMarked.selected",
    })
    void okSaveListDialogAction(
        File file, boolean hasIdentical, boolean hasDifferent, boolean hasLeft, boolean hasRight, boolean hasNotMarked) {

        setShowIdentical(hasIdentical);
        setShowDifferent(hasDifferent);
        setShowLeft(hasLeft);
        setShowRight(hasRight);
        setHideMarked(hasNotMarked);

        serializePairs(file);
        model.viewUpdated(this, "pageSaveListDialog_close");
    }

    /** form */
    @SwingControllerAction
    void changeMode() {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            toExpand();
        } else {
            toOutline();
        }
    }

    /** form */
    void moveCursor(int x, int y) {
        if (x > 50 && x < 80) {
            model.viewUpdated(this, "moveCursor", x, y);
        }
    }

    /** form */
    void updateGraphics() {
        if (model.displayMode == DisplayMode.EXPANDED_MODE) {
            model.viewUpdated(this, "updateGraphics");
        }
    }

    /** form sub */
    private void finishWork() {
        if (model.isMultiMode()) {
            String title = MessageFormat.format(rb.getString("frame.title.compare"), model.getLeftFilePath(), model.getRightFilePath());
            model.viewUpdated(this, "setTitle", title);
        } else {
            model.viewUpdated(this, "setTitle", rb.getString("frame.title.default"));
        }
    }

    /** */
    @SwingControllerAction
    void lsl_valueChanged(ListSelectionEvent ev) {
        if (!ev.getValueIsAdjusting()) {
            if (!(((JList<?>) ev.getSource()).getSelectedValue() instanceof Pair)) {
                return;
            }
            Pair current = (Pair) ((JList<?>) ev.getSource()).getSelectedValue();

            setCurrent(current);
        }
    }

    /** */
    @SwingControllerAction
    void lml_mouseClicked(MouseEvent ev) {
        if (ev.getClickCount() == 2) {
            toExpand();
        } else if (SwingUtilities.isRightMouseButton(ev)) {
            int x = ev.getX();
            int y = ev.getY();

            // TODO menu control, selection control
            pagePopup(x, y);
        }
    }

    /** */
    @SwingControllerAction
    void lml_mousePressed(MouseEvent ev) {
        if (SwingUtilities.isLeftMouseButton(ev)) {
            model.viewUpdated(this, "startSelection", ev.getPoint());
        }
    }

    /** */
    @SwingControllerAction
    void lml_mouseDragged(MouseEvent ev) {
        if (SwingUtilities.isLeftMouseButton(ev)) {
            model.viewUpdated(this, "continueSelection", ev.getPoint());
        }
    }

    /** */
    @SwingControllerAction
    void lml_mouseReleased(MouseEvent ev) {
        if (SwingUtilities.isLeftMouseButton(ev)) {
            model.viewUpdated(this, "endSelection", ev.getPoint());
        }
    }

    /** */
    @SwingControllerAction
    void pml_mousePressed(MouseEvent ev) {
        int x = ev.getX();
        int y = ev.getY();

        moveCursor(x, y);
    }

    /** */
    @SwingControllerAction
    void pal_adjustmentValueChanged(AdjustmentEvent ev) {
        updateGraphics();
    }

    /** */
    @SwingControllerAction
    void windowClosing(WindowEvent ev) {
        pageMain_close2();
    }
}

/* */
