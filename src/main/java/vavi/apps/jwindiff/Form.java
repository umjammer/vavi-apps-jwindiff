/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.List;

import vavi.apps.jwindiff.Controller.Order;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;
import vavi.util.event.GenericSupport;


/**
 * Form. (one of model???)
 * モデルに影響しないデータ。画面遷移等
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060726 nsano initial version <br>
 */
class Form {

    /** view listener */
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

    /** page
     * @see View#pageMain_close() */
    void pageMain_close() {
        viewUpdated(this, "pageMain_close");
    }

    /** page
     * @see View#pageSaveListDialog() */
    void pageSaveFile() {
        viewUpdated(this, "pageSaveListDialog");
    }

    /** page
     * @see View#pagePopupOutline(int, int) */
    void pagePopupOutline(int x, int y) {
        viewUpdated(this, "pagePopupOutline", x, y);
    }

    /** page
     * @see View#pagePopupExpanded(int, int) */
    void pagePopupExpanded(int x, int y) {
        viewUpdated(this, "pagePopupExpanded", x, y);
    }

    /** @see View#initCompareTargetsDialog(File, File) */
    void initCompareTargetsDialog(File left, File right) {
        viewUpdated(this, "initCompareTargetsDialog", left, right);
    }

    /** page
     * @see View#pageCompareTargetsDialog() */
    void pageCompareTargetsDialog() {
        viewUpdated(this, "pageCompareTargetsDialog");
    }

    /** @see View#initEditorDialog(File) */
    void initEditorDialog(File file) {
        viewUpdated(this, "initEditorDialog", file);
    }

    /** page
     * @see View#pageEditorChooser() */
    void pageEditorChooser() {
        viewUpdated(this, "pageEditorChooser");
    }

    /** page
     * @see View#pageEditorDialog_close() */
    void pageEditorDialog_close() {
        viewUpdated(this, "pageEditorDialog_close");
    }

    /** page
     * @see View#pagePatternDialog_close() */
    void pagePatternDialog_close() {
        viewUpdated(this, "pagePatternDialog_close");
    }

    /** page
     * @see View#pageCompareTargetsDialog_close() */
    void pageTargetsDialog_close() {
        viewUpdated(this, "pageCompareTargetsDialog_close");
    }

    /** page
     * @see View#pageSaveListDialog_close() */
    void pageSaveListDialog_close() {
        viewUpdated(this, "pageSaveListDialog_close");
    }

    /** @see View#startSelection(Point) */
    void startSelection(Point point) {
        viewUpdated(this, "startSelection", point);
    }

    /** @see View#endSelection(Point) */
    void endSelection(Point point) {
        viewUpdated(this, "endSelection", point);
    }

    /** @see View#continueSelection(Point) */
    void continueSelection(Point point) {
        viewUpdated(this, "continueSelection", point);
    }

    /** @see View#moveCursor(int, int) */
    void moveCursor(int x, int y) {
        viewUpdated(this, "moveCursor", x, y);
    }

    /** @see View#updateGraphics() */
    void updateGraphics() {
        viewUpdated(this, "updateGraphics");
    }

    /** @see View#findOutline(Order) */
    void findOutline(Order order) {
        viewUpdated(this, "findOutline", order);
    }

    /** @see View#findExpand(Order) */
    void findExpand(Order order) {
        viewUpdated(this, "findExpand", order);
    }

    /** @see View#setTitle(String) */
    void setTitle(String title) {
        viewUpdated(this, "setTitle", title);
    }

    /** @see View#pagePatternDialog() */
    void markPattern() {
        viewUpdated(this, "pagePatternDialog");
    }

    /** @see View#displaySingleFile(String[]) */
    void displaySingleFile(String[] lines) {
        viewUpdated(this, "displaySingleFile", (Object) lines);
    }

    /** @see View#toExpand() */
    void toExpand() {
        viewUpdated(this, "toExpand");
    }

    /** @see View#toOutline() */
    void toOutline() {
        viewUpdated(this, "toOutline");
    }

    /** @see View#toSelection() */
    void toSelection() {
        viewUpdated(this, "toSelection");
    }

    /** @see View#displayException(Exception) */
    void displayException(IOException e) {
        viewUpdated(this, "displayException", e);
    }

    /** @see View#setNames(String) */
    void setNames(String name) {
        viewUpdated(this, "setNames", name);
    }

    /** @see View#setPaths(String) */
    void setPaths(String path) {
        viewUpdated(this, "setPaths", path);
    }

    /** @see View#redisplayExpandedBefore() */
    void redisplayExpandedBefore() {
        viewUpdated(this, "redisplayExpandedBefore");
    }

    /** @see View#redisplayOutlineAfter(List, Pair) */
    void redisplayOutlineAfter(List<Pair> pairs, Pair current) {
        viewUpdated(this, "redisplayOutlineAfter", pairs, current);
    }

    /** @see View#redisplayOutlineBefore(String, String) */
    void redisplayOutlineBefore(String leftFilePath, String rightFilePath) {
        viewUpdated(this, "redisplayOutlineBefore", leftFilePath, rightFilePath);
    }

    /** @see View#pageMain() */
    void pageMain() {
        viewUpdated(this, "pageMain");
    }

    /** @see View#initMain(Model) */
    void initMain(Model model) {
        viewUpdated(this, "initMain", model);
    }
}

/* */
