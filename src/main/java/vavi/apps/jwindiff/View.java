/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.UIManager;

import vavi.apps.jwindiff.Controller.Order;
import vavi.apps.jwindiff.Model.DisplayMode;
import vavi.apps.jwindiff.Model.ShowExpandMode;
import vavi.apps.jwindiff.Model.ShowNumMode;
import vavi.swing.JFileChooserField;
import vavi.swing.JFileChooserHistoryComboBox;
import vavi.swing.JHistoryComboBox;
import vavi.swing.mvc.SwingComponent;
import vavi.swing.mvc.XView;
import vavi.swing.mvc.XViewAction;
import vavi.util.Debug;


/**
 * View.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 060504 nsano initial version <br>
 */
@XView
class View {

    /** */
    private static final ResourceBundle rb = ResourceBundle.getBundle("JWinDiffResources", Locale.getDefault());

    static {
        UIManager.getDefaults().put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.getDefaults().put("TextField.border", BorderFactory.createEmptyBorder());
    }

    /** */
    View() {
        createFrame();

        createTargetsDialog();
        createSaveListDialog();
        createEditorDialog();
        createPatternDialog();
    }

    // main view ----

    /** */
    @SwingComponent(listSelectionListener = "lsl_valueChanged",
                    mouseClicked = "lml_mouseClicked",
                    mousePressed = "lml_mousePressed",
                    mouseDragged = "lml_mouseDragged",
                    mouseReleased = "lml_mouseReleased")
    JList<Object> mainView;

    //----

    /** */
    @XViewAction
    void updateMain(List<Line> listModel) {
        DefaultListModel<Object> viewListModel = new DefaultListModel<>();
        for (Line line : listModel) {
            viewListModel.addElement(line);
        }
        mainView.setModel(viewListModel);
    }

    /** TODO */
    @XViewAction
    void redisplayOutlineBefore(String left, String right) {
        changeMode.setText(rb.getString("button.changeMode.text.expand"));
        viewOutline.setSelected(true);
        viewExpand.setSelected(false);

        names.setText("");
        paths.setText(left + " : " + right);
    }

    /**
     * TODO
     * set the diff display widget into outline mode
     */
    @XViewAction
    void redisplayOutlineAfter(List<Pair> pairs, Pair current) {
        DefaultListModel<Object> listModel = new DefaultListModel<>();
        for (Pair pair : pairs) {
            listModel.addElement(pair);
        }

        mainView.setModel(listModel);
        mainView.setSelectedValue(current, true);

        pictView.setVisible(false);
        pictView.setEnabled(false);
    }

    /** TODO */
    @XViewAction
    void redisplayExpandedBefore() {
        changeMode.setText(rb.getString("button.changeMode.text.outline"));
        viewOutline.setSelected(false);
        viewExpand.setSelected(true);

        pictView.setEnabled(true);
        pictView.setVisible(true);
    }

    /** */
    @XViewAction
    void setNames(String name) {
        names.setText(name + " ");
    }

    /** */
    @XViewAction
    void setPaths(String path) {
        paths.setText(" " + path);
    }

    /** */
    @XViewAction
    void displayException(Exception e) {
        DefaultListModel<Object> model = new DefaultListModel<>();
        model.addElement(new Line(0, e.toString(), Line.Type.PLAIN));
        mainView.setModel(model);
    }

    /** */
    @XViewAction
    void displaySingleFile(String[] lines) {
        DefaultListModel<Object> model = new DefaultListModel<>();
        for (int i = 0; i < lines.length; i++) {
            model.addElement(new Line(i, lines[i], Line.Type.PLAIN));
        }
        mainView.setModel(model);
    }

    /** */
    @XViewAction
    void updateGraphics() {
        pictView.repaint();
    }

    /** */
    @XViewAction
    void toExpand() {
        viewExpand.setSelected(true);
    }

    /** */
    @XViewAction
    void toOutline() {
        viewOutline.setSelected(true);
    }

    /** */
    @XViewAction
    void toSelection() {
        mainView.ensureIndexIsVisible(0);
    }

    /** */
    @XViewAction
    void startSelection(Point point) {
        first = mainView.locationToIndex(point);
// Debug.println(first);
    }

    /** */
    @XViewAction
    void continueSelection(Point point) {
        last = mainView.locationToIndex(point);
        mainView.setSelectionInterval(first, last);
// Debug.println(first + ", " + last);
    }

    /** */
    @XViewAction
    void endSelection(Point point) {
        last = mainView.locationToIndex(point);
        if (first != last) {
            mainView.setSelectionInterval(first, last);
// Debug.println(first + ", " + last);
        }
    }

    /** */
    @XViewAction
    void setTitle(String title) {
        top.setTitle(title);
    }

    /** */
    void selectionChanged(String name) { // TODO unused???
        if (!mainView.isSelectionEmpty()) {
Debug.println(mainView.getSelectedIndex());
            names.setText(name);
//      } else {
//Debug.println("A: " + min + ", " + max);
//          mainView.setValueIsAdjusting(false);
        }
    }

    /** */
    @XViewAction
    void moveCursor(int x, int y) {
        int height = pictView.getSize().height;
        int size = mainView.getModel().getSize();
//      int first = mainView.getFirstVisibleIndex();
//      int last = mainView.getLastVisibleIndex();
//      int h = Math.abs((last - first) / 2);
//      int index = y * size / height - h;
        int index = y * size / height;
        mainView.ensureIndexIsVisible(index);
        pictView.repaint();
    }

    //------------------------------------------------------------------------

    /**
     * Variables for the various dialogs
     */
    private JDialog targetsDialog;
    /** */
    JFileChooserField leftTargetChooser;
    /** */
    JFileChooserField rightTargetChooser;
    /** */
    @SwingComponent(action = "okTargetsDialogAction")
    JButton okTargetsDialogButton;
    /** */
    @SwingComponent(view = "pageCompareTargetsDialog_close")
    JButton cancelTargetsDialogButton;

    /**
     * Code to create each of the dialogs
     */
    private void createTargetsDialog() {

        int w = top.getSize().width;
        // int h = top.getSize().height;

        targetsDialog = new JDialog(top, true);
        targetsDialog.setTitle(rb.getString("dialog.targets.title"));

        JPanel base = new JPanel(new GridLayout(4, 1));
        targetsDialog.getContentPane().add(base);

        JPanel p = new JPanel(new GridLayout(2, 1));
        JLabel l = new JLabel(rb.getString("dialog.targets.label.0.text"));
        l.setPreferredSize(new Dimension(w * 6 / 10, 0));
        p.add(l);

        JTextField t = new JTextField(System.getProperty("user.dir"));
        t.setBackground(UIManager.getColor("Panel.background"));
        t.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 4));
        p.add(t);

        base.add(p);

        p = new JPanel(new GridLayout(2, 1));

        l = new JLabel(rb.getString("dialog.targets.label.1.text"));
        p.add(l);

        leftTargetChooser = new JFileChooserHistoryComboBox();
        p.add(leftTargetChooser);

        base.add(p);

        p = new JPanel(new GridLayout(2, 1));

        l = new JLabel(rb.getString("dialog.targets.label.2.text"));
        p.add(l);

        rightTargetChooser = new JFileChooserHistoryComboBox();
        p.add(rightTargetChooser);

        base.add(p);

        p = new JPanel();

        okTargetsDialogButton = new JButton();
        okTargetsDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        targetsDialog.getRootPane().setDefaultButton(okTargetsDialogButton);
        p.add(okTargetsDialogButton);

        cancelTargetsDialogButton = new JButton();
        cancelTargetsDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelTargetsDialogButton);

        base.add(p);

        targetsDialog.pack();
    }

    /** */
    @XViewAction
    void initCompareTargetsDialog(File left, File right) {
        leftTargetChooser.setCurrentDirectory(left);
        leftTargetChooser.setSelectedFile(left);
        rightTargetChooser.setCurrentDirectory(right);
        rightTargetChooser.setSelectedFile(right);
    }

    /** */
    @XViewAction
    void pageCompareTargetsDialog() {
        targetsDialog.setLocationRelativeTo(top);
        targetsDialog.setVisible(true);
    }

    /** */
    @XViewAction
    void pageCompareTargetsDialog_close() {
        targetsDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    private JDialog saveListDialog;

    /** */
    JFileChooserField listFileChooser;

    /** */
    JCheckBox hasIdentical;

    /** */
    JCheckBox hasDifferent;

    /** */
    JCheckBox hasLeft;

    /** */
    JCheckBox hasRight;

    /** */
    JCheckBox hasNotMarked;

    /** */
    @SwingComponent(action = "okSaveListDialogAction")
    JButton okSaveListDialogButton;

    /** */
    @SwingComponent(view = "pageSaveListDialog_close")
    JButton cancelSaveListDialogButton;

    /** */
    private void createSaveListDialog() {

        int w = top.getSize().width;
        // int h = top.getSize().height;

        saveListDialog = new JDialog(top, true);
        saveListDialog.setTitle(rb.getString("dialog.seveList.title"));

        JPanel base = new JPanel(new BorderLayout());
        saveListDialog.getContentPane().add(base);

        JPanel p = new JPanel(new GridLayout(2, 1));
        JLabel l = new JLabel(rb.getString("dialog.seveList.label.0.text"));
        l.setPreferredSize(new Dimension(w * 6 / 10, 0));
        p.add(l);

        listFileChooser = new JFileChooserHistoryComboBox();
        listFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        p.add(listFileChooser);

        base.add(BorderLayout.NORTH, p);

        p = new JPanel(new GridLayout(6, 1));
        l = new JLabel(rb.getString("dialog.seveList.label.1.text"));
        p.add(l);

        hasIdentical = new JCheckBox();
        hasIdentical.setText(rb.getString("dialog.seveList.checkBox.0.text"));
        p.add(hasIdentical);
        hasDifferent = new JCheckBox(rb.getString("dialog.seveList.checkBox.1.text"));
        p.add(hasDifferent);
        hasLeft = new JCheckBox(rb.getString("dialog.seveList.checkBox.2.text"));
        p.add(hasLeft);
        hasRight = new JCheckBox(rb.getString("dialog.seveList.checkBox.3.text"));
        p.add(hasRight);
        hasNotMarked = new JCheckBox(rb.getString("dialog.seveList.checkBox.4.text"));
        p.add(hasNotMarked);

        base.add(BorderLayout.CENTER, p);

        p = new JPanel();
        okSaveListDialogButton = new JButton();
        okSaveListDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        saveListDialog.getRootPane().setDefaultButton(okSaveListDialogButton);
        p.add(okSaveListDialogButton);

        cancelSaveListDialogButton = new JButton();
        cancelSaveListDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelSaveListDialogButton);

        base.add(BorderLayout.SOUTH, p);

        saveListDialog.pack();
    }

    /**
     * initialize what to include in the list according to current prefs
     */
    @XViewAction
    void pageSaveListDialog() {
        // TODO model から取るべきか？
        hasIdentical.setSelected(showIdentical.isSelected());
        hasDifferent.setSelected(showDifferent.isSelected());
        hasLeft.setSelected(showLeft.isSelected());
        hasRight.setSelected(showRight.isSelected());
        hasNotMarked.setSelected(hideMarked.isSelected());

        saveListDialog.setLocationRelativeTo(top);
        saveListDialog.setVisible(true);
    }

    /** */
    @XViewAction
    void pageSaveListDialog_close() {
        saveListDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    private JDialog editorDialog;

    /** The editor path name */
    JFileChooserField editorChooser;

    /** */
    @SwingComponent(action = "okEditorDialogAction")
    JButton okEditorDialogButton;

    /** */
    @SwingComponent(view = "pageEditorDialog_close")
    JButton cancelEditorDialogButton;

    /** */
    private void createEditorDialog() {

        int w = top.getSize().width;
//      int h = top.getSize().height;

        editorDialog = new JDialog(top, true);
        editorDialog.setTitle(rb.getString("dialog.editor.title"));

        JPanel base = new JPanel(new GridLayout(2, 1));
        editorDialog.getContentPane().add(base);

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(w * 6 / 10, 0));
        editorChooser = new JFileChooserHistoryComboBox();
        editorChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        p.add(BorderLayout.SOUTH, editorChooser);
        base.add(p);

        p = new JPanel();

        okEditorDialogButton = new JButton();
        okEditorDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        editorDialog.getRootPane().setDefaultButton(okEditorDialogButton);
        p.add(okEditorDialogButton);

        cancelEditorDialogButton = new JButton();
        cancelEditorDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelEditorDialogButton);
        base.add(p);

        editorDialog.pack();
    }

    /** */
    @XViewAction
    void initEditorDialog(File file) {
        editorChooser.setCurrentDirectory(file);
        editorChooser.setSelectedFile(file);
    }

    /** */
    @XViewAction
    void pageEditorChooser() {
        editorChooser.requestFocus();
        editorDialog.setLocationRelativeTo(top);
        editorDialog.setVisible(true);
    }

    @XViewAction
    void pageEditorDialog_close() {
        editorDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    private JDialog patternDialog;

    /** The text for matching */
    JHistoryComboBox patternField;

    /** */
    @SwingComponent(action = "okPatternDialogAction")
    JButton okPatternDialogButton;

    /** */
    @SwingComponent(view = "pagePatternDialog_close")
    JButton cancelPatternDialogButton;

    /** */
    private void createPatternDialog() {

        int w = top.getSize().width;
        // int h = top.getSize().height;

        patternDialog = new JDialog(top, true);
        patternDialog.setTitle(rb.getString("dialog.pattern.title"));

        JPanel base = new JPanel(new GridLayout(2, 1));
        patternDialog.getContentPane().add(base);

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(w * 4 / 10, 0));
        patternField = new JHistoryComboBox();
        p.add(BorderLayout.SOUTH, patternField);
        base.add(p);

        p = new JPanel();

        okPatternDialogButton = new JButton();
        okPatternDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        patternDialog.getRootPane().setDefaultButton(okPatternDialogButton);
        p.add(okPatternDialogButton);

        cancelPatternDialogButton = new JButton();
        cancelPatternDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelPatternDialogButton);
        base.add(p);

        patternDialog.pack();
    }

    /** */
    @XViewAction
    void pagePatternDialog() {
        patternField.requestFocus();
        patternDialog.setLocationRelativeTo(top);
        patternDialog.setVisible(true);
    }

    /** */
    @XViewAction
    void pagePatternDialog_close() {
        patternDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    public static final Color green = new Color(0xD6, 0xFF, 0xEA);
    public static final Color red = new Color(0xFF, 0xE5, 0xDD);
    public static final Color darkGreen = new Color(0x00, 0x57, 0x33);
    public static final Color darkRed = new Color(0xC3, 0x00, 0x00);
    public static final Color darkGray = new Color(0xCA, 0xCA, 0xCA);
    public static final Color blue = new Color(0x00, 0x2A, 0xDA);

    /** */
    int first;

    /** */
    int last;

    /** */
    private class PicturePanel extends JPanel {
        public void paint(Graphics g) {
            super.paint(g);
            drawBar(g);
            drawCursor(g);
        }

        private static final int BR = 42;

        private static final int BW = 8;

        /** */
        private void drawBar(Graphics g) {
            Line.Type flag = null;
            int i = 0;
            int first = -1;
// Debug.println("----");
            while (i < mainView.getModel().getSize()) {
                // TODO かっこ悪い
                if (!(mainView.getModel().getElementAt(0) instanceof Line)) {
                    return;
                }

                Line line = (Line) mainView.getModel().getElementAt(i);
                if (line.getFlag() != flag) {
                    if (first != -1) {
                        drawBarImpl(g, first, i, flag);
                    }
                    flag = line.getFlag();
                    first = i;
                }
                i++;
            }
            drawBarImpl(g, first, i, flag);
        }

        private void drawBarImpl(Graphics g, int first, int last, Line.Type flag) {
            int height = getSize().height;
            int size = mainView.getModel().getSize();

            int y = Math.round((float) first / size * height);
            int h = Math.round((float) (last - first) / size * height);
// Debug.println(first + ", " + last + ", " + flag);
            if (flag == Line.Type.PLAIN) {
                g.setColor(Color.black);
                g.drawRect(BR, y, BW, h);
            } else if (flag == Line.Type.DELETED) {
                g.setColor(red);
                g.fillRect(BR, y, BW, h);
            } else if (flag == Line.Type.INSERTED) {
                g.setColor(green);
                g.fillRect(BR, y, BW, h);
            }
        }

        private static final int CX = 60;

        private static final int CW = 8;

        /** */
        private void drawCursor(Graphics g) {
            int height = getSize().height;
            int size = mainView.getModel().getSize();
            int first = mainView.getFirstVisibleIndex();
            int last = mainView.getLastVisibleIndex();

            int y = Math.round((float) first / size * height);
            int h = Math.round((float) (last - first + 1) / size * height);

            g.setColor(darkGray);
            g.fillRect(CX, y, CW, h);
        }

        public Dimension getPreferredSize() {
            int height = getSize().height;
            return new Dimension(80, height);
        }
    }

    // -------------------------------------------------------------------------

    /** */
    @SwingComponent(action = "toExpand")
    JCheckBoxMenuItem viewExpand;
    /** */
    @SwingComponent(action = "toOutline")
    JCheckBoxMenuItem viewOutline;
    /** In expanded mode, which files to show -- mutually exclusive */
    @SwingComponent(action = "setShowExpandModeAction")
    JCheckBoxMenuItem showLeftOnly;
    /** In expanded mode, which files to show -- mutually exclusive */
    @SwingComponent(action = "setShowExpandModeAction")
    JCheckBoxMenuItem showRightOnly;
    /** In expanded mode, which files to show -- mutually exclusive */
    @SwingComponent(action = "setShowExpandModeAction")
    JCheckBoxMenuItem showBoth;
    /** In expanded mode, which file numbering to show -- mutually exclusive */
    @SwingComponent(action = "setShowNumModeAction")
    JCheckBoxMenuItem showLeftNums;
    /** In expanded mode, which file numbering to show -- mutually exclusive */
    @SwingComponent(action = "setShowNumModeAction")
    JCheckBoxMenuItem showRightNums;
    /** In expanded mode, which file numbering to show -- mutually exclusive */
    @SwingComponent(action = "setShowNumModeAction")
    JCheckBoxMenuItem hideNums;
    /** */
    @SwingComponent(action = "updateOutline")
    JCheckBoxMenuItem ignoreBlanks;
    /** in outline mode, which files to list */
    @SwingComponent(action = "showIdenticalAction")
    JCheckBoxMenuItem showIdentical;
    /** */
    @SwingComponent(action = "showDifferentAction")
    JCheckBoxMenuItem showDifferent;
    /** */
    @SwingComponent(action = "showLeftAction")
    JCheckBoxMenuItem showLeft;
    /** */
    @SwingComponent(action = "showRightAction")
    JCheckBoxMenuItem showRight;
    /** */
    @SwingComponent(action = "hideMarkedAction")
    JCheckBoxMenuItem hideMarked;
    /** The popup menu */
    private JPopupMenu popupOutline;
    /** */
    private JPopupMenu popupExpanded;
    /** Widgets */
    @SwingComponent(windowClosing = "windowClosing")
    JFrame top;
    /** */
    @SwingComponent(sub = "verticalScrollBar", adjustmentListener = "pal_adjustmentValueChanged")
    JScrollPane sp;
    /** */
    @SwingComponent(mousePressed = "pml_mousePressed")
    JPanel pictView;
    /** */
    private JLabel names;
    /** */
    private JLabel paths;
    /** */
    @SwingComponent(action = "changeMode")
    JButton changeMode;
    /** */
    @SwingComponent(action = "pageCompareTargetsDialog")
    JMenuItem compareTargetsMenuItem;
    /** */
    @SwingComponent(action = "abortAction")
    JMenuItem abortMenuItem;
    /** */
    @SwingComponent(action = "pageSaveFile")
    JMenuItem saveFilelistMenuItem;
    /** */
    @SwingComponent(action = "pageMain_close")
    JMenuItem exitMenuItem;
    /** */
    @SwingComponent(action = "editLeft")
    JMenuItem editLeftMenuItem;
    /** */
    @SwingComponent(action = "editRight")
    JMenuItem editRightMenuItem;
    /** */
    @SwingComponent(action = "pageEditorChooser")
    JMenuItem setEditorMenuItem;
    /** */
    @SwingComponent(action = "prevAction")
    JMenuItem prevMenuItem;
    /** */
    @SwingComponent(action = "nextAction")
    JMenuItem nextMenuItem;
    /** */
    @SwingComponent(action = "rescanAction")
    JMenuItem rescanMenuItem;
    /** */
    @SwingComponent(action = "markFileAction")
    JMenuItem markFileMenuItem;
    /** */
    @SwingComponent(view = "pagePatternDialog")
    JMenuItem markPatternMenuItem;
    /** */
    @SwingComponent(action = "toggleAllMarks")
    JMenuItem toggleMarkedMenuItem;
    /** */
    JButton modeButton;
    /** */
    @SwingComponent(action = "toExpand")
    JMenuItem expandMenuItem;
    /** */
    @SwingComponent(action = "toOutline")
    JMenuItem outlineMenuItem;

    /** 使い回しはできない */
    @SwingComponent(action = "prevAction")
    JMenuItem prevMenuItem2;
    /** */
    @SwingComponent(action = "nextAction")
    JMenuItem nextMenuItem2;
    /** */
    @SwingComponent(action = "rescanAction")
    JMenuItem rescanMenuItem2;
    /** */
    @SwingComponent(action = "editLeft")
    JMenuItem editLeftMenuItem2;
    /** */
    @SwingComponent(action = "editRight")
    JMenuItem editRightMenuItem2;

    /** */
    @SwingComponent(action = "prevAction")
    JMenuItem prevMenuItem3;
    /** */
    @SwingComponent(action = "nextAction")
    JMenuItem nextMenuItem3;
    /** */
    @SwingComponent(action = "rescanAction")
    JMenuItem rescanMenuItem3;
    /** */
    @SwingComponent(action = "editLeft")
    JMenuItem editLeftMenuItem3;
    /** */
    @SwingComponent(action = "editRight")
    JMenuItem editRightMenuItem3;

    /** */
    @SwingComponent(action = "copyFilesAction")
    JMenuItem copyFilesMenuItem;

    /**
     * Build the widget structure of the main window
     */
    private void createFrame() {

        // Set up the actions table for the whole application
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        int width = d.width / 2;
        int height = d.height * 3 / 4;

        top = new JFrame();
        top.setIconImage(t.getImage(this.getClass().getResource(rb.getString("frame.jWinDiff.iconImage"))));
        top.setTitle(rb.getString("frame.title.scanning"));
        top.setSize(width, height);
        top.setLocation((d.width - width) / 2, (d.height - height) / 2);

        JPanel base = new JPanel();
        base.setLayout(new BorderLayout());
        top.getContentPane().add(base);

        pictView = new PicturePanel();
        pictView.setOpaque(true);
        pictView.setBackground(Color.white);
        base.add(pictView, BorderLayout.WEST);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(true);
//      panel.setBackground(Color.cyan);
        base.add(panel, BorderLayout.NORTH);

        int fontSize = Integer.valueOf(rb.getString("panel.jWinDiff.font.size"));
        mainView = new JList<>();
        mainView.setFont(new Font(rb.getString("panel.jWinDiff.font.name"), Font.PLAIN, fontSize));
Debug.println(mainView.getFont());

        sp = new JScrollPane();
        sp.setViewportView(mainView);
        base.add(sp);

        // Now create each of the menus

        JMenuBar menuBar = new JMenuBar();

        // file
        JMenu menu = new JMenu();
        menu.setText(rb.getString("menu.file.text"));
        menu.setMnemonic(KeyEvent.VK_F);

        compareTargetsMenuItem = new JMenuItem();
        compareTargetsMenuItem.setText(rb.getString("menuItem.compareTargets.text"));
        compareTargetsMenuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(compareTargetsMenuItem);

        menu.addSeparator();

        abortMenuItem = new JMenuItem();
        abortMenuItem.setText(rb.getString("menuItem.abort.text"));
        abortMenuItem.setMnemonic(KeyEvent.VK_A);
        abortMenuItem.setEnabled(false);
        menu.add(abortMenuItem);

        menu.addSeparator();

        saveFilelistMenuItem = new JMenuItem();
        saveFilelistMenuItem.setText(rb.getString("menuItem.saveFileList.text"));
        saveFilelistMenuItem.setMnemonic(KeyEvent.VK_S);
        menu.add(saveFilelistMenuItem);

        copyFilesMenuItem = new JMenuItem();
        copyFilesMenuItem.setText(rb.getString("menuItem.copyFiles.text"));
        copyFilesMenuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(copyFilesMenuItem);

        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.print.text"));
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        exitMenuItem = new JMenuItem();
        exitMenuItem.setText(rb.getString("menuItem.exit.text"));
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        menu.add(exitMenuItem);

        menuBar.add(menu);

        // edit
        menu = new JMenu();
        menu.setText(rb.getString("menu.edit.text"));
        menu.setMnemonic(KeyEvent.VK_E);

        editLeftMenuItem = new JMenuItem();
        editLeftMenuItem.setText(rb.getString("menuItem.editLeftFile.text"));
        editLeftMenuItem.setMnemonic(KeyEvent.VK_L);
        menu.add(editLeftMenuItem);

        editRightMenuItem = new JMenuItem();
        editRightMenuItem.setText(rb.getString("menuItem.editRightFile.text"));
        editRightMenuItem.setMnemonic(KeyEvent.VK_R);
        menu.add(editRightMenuItem);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.editCompositeFile.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        setEditorMenuItem = new JMenuItem();
        setEditorMenuItem.setText(rb.getString("menuItem.setEditor.text"));
        setEditorMenuItem.setMnemonic(KeyEvent.VK_E);
        menu.add(setEditorMenuItem);

        menuBar.add(menu);

        // view
        ButtonGroup bg = new ButtonGroup();

        menu = new JMenu(rb.getString("menu.view.text"));
        menu.setMnemonic(KeyEvent.VK_V);

        viewOutline = new JCheckBoxMenuItem();
        viewOutline.setText(rb.getString("menuItem.outline.text"));
        viewOutline.setMnemonic(KeyEvent.VK_O);
        menu.add(viewOutline);
        bg.add(viewOutline);

        viewExpand = new JCheckBoxMenuItem();
        viewExpand.setText(rb.getString("menuItem.expand.text"));
        viewExpand.setMnemonic(KeyEvent.VK_E);
        menu.add(viewExpand);
        bg.add(viewExpand);

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem();
        menuItem.setText(rb.getString("menuItem.picture.text"));
//      menuItem.setMnemonic(KeyEvent.VK_P); // M+p
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        prevMenuItem = new JMenuItem();
        prevMenuItem.setText(rb.getString("menuItem.previousChange.text"));
        prevMenuItem.setMnemonic(KeyEvent.VK_P); // F7
        menu.add(prevMenuItem);

        nextMenuItem = new JMenuItem();
        nextMenuItem.setText(rb.getString("menuItem.nextChange.text"));
        nextMenuItem.setMnemonic(KeyEvent.VK_N); // F8
        menu.add(nextMenuItem);

        menu.addSeparator();

        rescanMenuItem = new JMenuItem();
        rescanMenuItem.setText(rb.getString("menuItem.rescanSelectedFile.text"));
        rescanMenuItem.setMnemonic(KeyEvent.VK_R);
        menu.add(rescanMenuItem);

        menuBar.add(menu);

        // expand
        menu = new JMenu(rb.getString("menu.expand.text"));
        menu.setMnemonic(KeyEvent.VK_X);

        bg = new ButtonGroup();

        showLeftOnly = new JCheckBoxMenuItem();
        showLeftOnly.setText(rb.getString("menuItem.leftFileOnly.text"));
        showLeftOnly.setMnemonic(KeyEvent.VK_F); // M+l
        showLeftOnly.setActionCommand(ShowExpandMode.left.name());
        menu.add(showLeftOnly);
        bg.add(showLeftOnly);

        showRightOnly = new JCheckBoxMenuItem();
        showRightOnly.setText(rb.getString("menuItem.rightFileOnly.text"));
        showRightOnly.setMnemonic(KeyEvent.VK_H); // M-r
        showRightOnly.setActionCommand(ShowExpandMode.right.name());
        menu.add(showRightOnly);
        bg.add(showRightOnly);

        showBoth = new JCheckBoxMenuItem();
        showBoth.setText(rb.getString("menuItem.bothFiles.text"));
        showBoth.setMnemonic(KeyEvent.VK_O); // M-b
        showBoth.setActionCommand(ShowExpandMode.both.name());
        menu.add(showBoth);
        bg.add(showBoth);

        menu.addSeparator();

        bg = new ButtonGroup();

        showLeftNums = new JCheckBoxMenuItem();
        showLeftNums.setText(rb.getString("menuItem.leftLineNumbers.text"));
        showLeftNums.setMnemonic(KeyEvent.VK_L);
        showLeftNums.setActionCommand(ShowNumMode.left.name());
        menu.add(showLeftNums);
        bg.add(showLeftNums);

        showRightNums = new JCheckBoxMenuItem();
        showRightNums.setText(rb.getString("menuItem.rightLineNumbers.text"));
        showRightNums.setMnemonic(KeyEvent.VK_R);
        showRightNums.setActionCommand(ShowNumMode.right.name());
        menu.add(showRightNums);
        bg.add(showRightNums);

        hideNums = new JCheckBoxMenuItem();
        hideNums.setText(rb.getString("menuItem.noLineNumbers.text"));
        hideNums.setMnemonic(KeyEvent.VK_N);
        hideNums.setActionCommand(ShowNumMode.none.name());
        menu.add(hideNums);
        bg.add(hideNums);

        menuBar.add(menu);

        // opts
        menu = new JMenu(rb.getString("menu.opts.text"));
        menu.setMnemonic(KeyEvent.VK_O);

        ignoreBlanks = new JCheckBoxMenuItem();
        ignoreBlanks.setText(rb.getString("menuItem.ignoreBlanks.text"));
        ignoreBlanks.setMnemonic(KeyEvent.VK_B);
        menu.add(ignoreBlanks);

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem(rb.getString("menuItem.monoColours.text"));
        menuItem.setMnemonic(KeyEvent.VK_M);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        showIdentical = new JCheckBoxMenuItem();
        showIdentical.setText(rb.getString("menuItem.showIdenticalFiles.text"));
        showIdentical.setMnemonic(KeyEvent.VK_I);
        menu.add(showIdentical);

        showLeft = new JCheckBoxMenuItem();
        showLeft.setText(rb.getString("menuItem.showLeftOnlyFiles.text"));
        showLeft.setMnemonic(KeyEvent.VK_L);
        menu.add(showLeft);

        showRight = new JCheckBoxMenuItem();
        showRight.setText(rb.getString("menuItem.showRightOnlyFiles.text"));
        showRight.setMnemonic(KeyEvent.VK_R);
        menu.add(showRight);

        showDifferent = new JCheckBoxMenuItem();
        showDifferent.setText(rb.getString("menuItem.showDifferentFiles.text"));
        showDifferent.setMnemonic(KeyEvent.VK_D);
        menu.add(showDifferent);

        menuBar.add(menu);

        // mark
        menu = new JMenu();
        menu.setText(rb.getString("menu.mark.text"));
        menu.setMnemonic(KeyEvent.VK_K);

        markFileMenuItem = new JMenuItem();
        markFileMenuItem.setText(rb.getString("menuItem.markFile.text"));
        markFileMenuItem.setMnemonic(KeyEvent.VK_M); // M+m
        menu.add(markFileMenuItem);

        markPatternMenuItem = new JMenuItem();
        markPatternMenuItem.setText(rb.getString("menuItem.markPattern.text"));
        markPatternMenuItem.setMnemonic(KeyEvent.VK_P);
        menu.add(markPatternMenuItem);

        hideMarked = new JCheckBoxMenuItem();
        hideMarked.setText(rb.getString("menuItem.hideMarkedFiles.text"));
        hideMarked.setMnemonic(KeyEvent.VK_H);
        menu.add(hideMarked);

        toggleMarkedMenuItem = new JMenuItem();
        toggleMarkedMenuItem.setText(rb.getString("menuItem.toggleMarkedState.text"));
        toggleMarkedMenuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(toggleMarkedMenuItem);

        menuBar.add(menu);

        // help
        menu = new JMenu();
        menu.setText(rb.getString("menu.help.text"));
        menu.setMnemonic(KeyEvent.VK_H);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.contents.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.about.text"));
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menuBar.add(menu);

        top.setJMenuBar(menuBar);

        // popup outline menu
        popupOutline = new JPopupMenu();

        markFileMenuItem.setMnemonic(KeyEvent.VK_M);
        popupOutline.add(markFileMenuItem);

        popupOutline.addSeparator();

        nextMenuItem2 = new JMenuItem();
        nextMenuItem2.setText(rb.getString("menuItem.nextChange.text"));
        nextMenuItem2.setMnemonic(KeyEvent.VK_N); // F8
        popupOutline.add(nextMenuItem2);

        prevMenuItem2 = new JMenuItem();
        prevMenuItem2.setText(rb.getString("menuItem.previousChange.text"));
        prevMenuItem2.setMnemonic(KeyEvent.VK_P); // F7
        popupOutline.add(prevMenuItem2);

        expandMenuItem = new JMenuItem();
        expandMenuItem.setText(rb.getString("button.changeMode.text.expand"));
        expandMenuItem.setMnemonic(KeyEvent.VK_E);
        popupOutline.add(expandMenuItem);

        rescanMenuItem2 = new JMenuItem();
        rescanMenuItem2.setText(rb.getString("menuItem.rescanSelectedFile.text"));
        rescanMenuItem2.setMnemonic(KeyEvent.VK_R);
        popupOutline.add(rescanMenuItem2);

        popupOutline.addSeparator();

        editLeftMenuItem2 = new JMenuItem();
        editLeftMenuItem2.setText(rb.getString("menuItem.editLeftFile.text"));
        editLeftMenuItem2.setMnemonic(KeyEvent.VK_L);
        popupOutline.add(editLeftMenuItem2);

        editRightMenuItem2 = new JMenuItem();
        editRightMenuItem2.setText(rb.getString("menuItem.editRightFile.text"));
        editRightMenuItem2.setMnemonic(KeyEvent.VK_R);
        popupOutline.add(editRightMenuItem2);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.editCompositeFile.text"));
        menuItem.setEnabled(false);
        popupOutline.add(menuItem);

        popupOutline.add(copyFilesMenuItem);

        // popup expanded menu
        popupExpanded = new JPopupMenu();

        menuItem = popupExpanded.add(rb.getString("menuItem.showMoveDest.text"));
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setEnabled(false);

        popupExpanded.addSeparator();

        nextMenuItem3 = new JMenuItem();
        nextMenuItem3.setText(rb.getString("menuItem.nextChange.text"));
        nextMenuItem3.setMnemonic(KeyEvent.VK_N); // F8
        popupExpanded.add(nextMenuItem3);

        prevMenuItem3 = new JMenuItem();
        prevMenuItem3.setText(rb.getString("menuItem.previousChange.text"));
        prevMenuItem3.setMnemonic(KeyEvent.VK_P); // F7
        popupExpanded.add(prevMenuItem3);

        outlineMenuItem = new JMenuItem();
        outlineMenuItem.setText(rb.getString("button.changeMode.text.outline"));
        outlineMenuItem.setMnemonic(KeyEvent.VK_O);
        popupExpanded.add(outlineMenuItem);

        rescanMenuItem3 = new JMenuItem();
        rescanMenuItem3.setText(rb.getString("menuItem.rescanSelectedFile.text"));
        rescanMenuItem3.setMnemonic(KeyEvent.VK_R);
        popupExpanded.add(rescanMenuItem3);

        popupExpanded.addSeparator();

        editLeftMenuItem3 = new JMenuItem();
        editLeftMenuItem3.setText(rb.getString("menuItem.editLeftFile.text"));
        editLeftMenuItem3.setMnemonic(KeyEvent.VK_L);
        popupExpanded.add(editLeftMenuItem3);

        editRightMenuItem3 = new JMenuItem();
        editRightMenuItem3.setText(rb.getString("menuItem.editRightFile.text"));
        editRightMenuItem3.setMnemonic(KeyEvent.VK_R);
        popupExpanded.add(editRightMenuItem3);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.editCompositeFile.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        popupExpanded.add(menuItem);

        // labels
        names = new JLabel();
        names.setForeground(blue);
        panel.add(BorderLayout.WEST, names);

        paths = new JLabel();
        panel.add(BorderLayout.CENTER, paths);

        // chageMode button
        changeMode = new JButton();
        changeMode.setMnemonic(KeyEvent.VK_D);
        panel.add(BorderLayout.EAST, changeMode);
    }

    /** */
    @XViewAction
    void initMain(Model model) {
        // 1.
        mainView.setCellRenderer(new SimpleListCellRenderer(model));

        // 2.
        viewOutline.setSelected(model.displayMode == DisplayMode.OUTLINE_MODE);
        viewExpand.setSelected(model.displayMode == DisplayMode.EXPANDED_MODE);
        changeMode.setText(model.displayMode == DisplayMode.OUTLINE_MODE ? rb.getString("button.changeMode.text.outline") : rb.getString("button.changeMode.text.expand"));

        // 3.1. DI model -> view
        switch (model.getShowExpandMode()) {
        case left:
            showLeftOnly.setSelected(true);
            break;
        case right:
            showRightOnly.setSelected(true);
            break;
        case both:
            showBoth.setSelected(true);
            break;
        }

        switch (model.getShowNumMode()) {
        case left:
            showLeftNums.setSelected(true);
            break;
        case right:
            showRightNums.setSelected(true);
            break;
        case none:
            hideNums.setSelected(true);
            break;
        }

//Debug.println("isIgnoreBlanks: " + model.isIgnoreBlanks());
//Debug.println("isShowIdentical: " + model.isShowIdentical());
//Debug.println("isShowLeft: " + model.isShowLeft());
//Debug.println("isShowRight: " + model.isShowRight());
//Debug.println("isShowDifferent: " + model.isShowDifferent());
//Debug.println("isHideMarked: " + model.isHideMarked());
        ignoreBlanks.setSelected(model.isIgnoreBlanks());
        showIdentical.setSelected(model.isShowIdentical());
        showLeft.setSelected(model.isShowLeft());
        showRight.setSelected(model.isShowRight());
        showDifferent.setSelected(model.isShowDifferent());
        hideMarked.setSelected(model.isHideMarked());

        // 3.2. history
        for (String pattern : model.markPatterns) {
            patternField.addItem(pattern);
        }

        for (String pattern : model.dir1Patterns) {
            patternField.addItem(pattern);
        }

        for (String pattern : model.dir2Patterns) {
            patternField.addItem(pattern);
        }
    }

    /** */
    @XViewAction
    void pageMain() {
        top.setVisible(true);
    }

    /** */
    @XViewAction
    void pageMain_close() {
        top.dispose();
    }

    // ----

    /** */
    @XViewAction
    void pagePopupOutline(int x, int y) {
        popupOutline.show(mainView, x, y);
    }

    /** */
    @XViewAction
    void pagePopupExpanded(int x, int y) {
        popupExpanded.show(mainView, x, y);
    }

    // -------------------------------------------------------------------------

    /**
     * Find next or prev diffs.
     * TODO use model instead of model in view
     */
    @XViewAction
    void findOutline(Order param) {
        if (param == Order.Ascent) {
            int index;
            if (mainView.isSelectionEmpty()) {
                index = 0;
            } else {
                index = mainView.getSelectedIndex();
            }
            ListModel<Object> model = mainView.getModel();
            while (index++ < model.getSize() - 1) {
                Pair.Type diff = ((Pair) model.getElementAt(index)).getDiff();
                if (diff.isDifferent()) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
            }
        } else {
            ListModel<Object> model = mainView.getModel();
            int index;
            if (mainView.isSelectionEmpty()) {
                index = model.getSize();
            } else {
                index = mainView.getSelectedIndex();
            }
            while (index-- > 0) {
                Pair.Type diff = ((Pair) model.getElementAt(index)).getDiff();
                if (diff.isDifferent()) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
            }
        }
        mainView.repaint(); // TODO for popup menu garbage
    }

    /**
     * TODO use model instead of model in view
     */
    @XViewAction
    void findExpand(Order param) {
        if (param == Order.Ascent) {
            int index;
            if (mainView.isSelectionEmpty()) {
                index = 0;
            } else {
                index = mainView.getSelectedIndex();
            }
            ListModel<Object> model = mainView.getModel();
            while (index++ < model.getSize() - 1) {
                if (((Line) model.getElementAt(index)).getFlag() == Line.Type.PLAIN) {
                    break;
                }
            }
            while (index < model.getSize()) {
                if (((Line) model.getElementAt(index)).getFlag() != Line.Type.PLAIN) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
                index++;
            }
        } else {
            ListModel<Object> model = mainView.getModel();
            int index;
            if (mainView.isSelectionEmpty()) {
                index = model.getSize();
            } else {
                index = mainView.getSelectedIndex();
            }
            while (index-- > 0) {
                if (((Line) model.getElementAt(index)).getFlag() == Line.Type.PLAIN) {
                    break;
                }
            }
            while (index >= 0) {
                if (((Line) model.getElementAt(index)).getFlag() != Line.Type.PLAIN) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
                index--;
            }
        }
        mainView.repaint(); // TODO for popup menu garbage
    }
}

/* */
