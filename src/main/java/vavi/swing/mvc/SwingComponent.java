/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.swing.mvc;

import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JWindow;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import vavi.beans.BeanUtil;


/**
 * SwingComponent.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2020/03/21 nsano initial version <br>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SwingComponent {

    /** @SwingControllerAction */
    String action() default "";

    /** view method */
    String view() default "";

    String mouseClicked() default "";

    String mouseReleased() default "";

    String mouseDragged() default "";

    String mousePressed() default "";

    String listSelectionListener() default "";

    String adjustmentListener() default "";

    String windowClosing() default "";

    /** bind to sub component */
    String sub() default "";

    /**
     * TODO when annotated to method
     */
    class Util {

        private static Logger logger = Logger.getLogger(Util.class.getName());

        private Util() {
        }

        /**
         * @param field {@link SwingComponent} annotated
         */
        public static String getAction(Field field) {
            SwingComponent target = field.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            return target.action();
        }

        /**
         * @param field {@link SwingComponent} annotated
         */
        public static String getView(Field field) {
            SwingComponent target = field.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            return target.view();
        }

        /**
         * @param component {@link SwingComponent} annotated
         */
        private static Object getSub(Object view, Field component) {
            SwingComponent target = component.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            if (!target.sub().isEmpty()) {
                return BeanUtil.getValue(target.sub(), BeanUtil.getFieldValue(component, view));
            } else {
                return BeanUtil.getFieldValue(component, view);
            }
        }

        /** */
        private static Method getMethod(Object controller, String name) {
            for (Method method : XController.Util.getSwingControllerActionMethods(controller)) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
            return null;
        }

        /**
         * @param component {@link SwingComponent} annotated
         */
        private static void setListSelectionListener(Object controller, Object view, Field component) {
            SwingComponent target = component.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            if (!target.listSelectionListener().isEmpty()) {
                Method method = getMethod(controller, target.listSelectionListener());
                if (method.getParameterCount() == 0) {
                    JList.class.cast(BeanUtil.getFieldValue(component, view)).addListSelectionListener(ev -> {
                        BeanUtil.invoke(method, controller);
                    });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.listSelectionListener() + "()");
                } else {
                    JList.class.cast(BeanUtil.getFieldValue(component, view)).addListSelectionListener(ev -> {
                        BeanUtil.invoke(method, controller, ev);
                    });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.listSelectionListener() + "(ev)");
                }
            }
        }

        /**
         * @param component {@link SwingComponent} annotated
         */
        private static void setAdjustmentListener(Object controller, Object view, Field component) {
            SwingComponent target = component.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            Object subComponent = getSub(view, component);
            if (!target.adjustmentListener().isEmpty()) {
                Method method = getMethod(controller, target.adjustmentListener());
                JScrollBar.class.cast(subComponent).addAdjustmentListener(ev -> {
                    BeanUtil.invoke(method, controller, ev);
                });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + (subComponent.getClass().equals(component.getDeclaringClass()) ? "" : "." + subComponent.getClass().getSimpleName()) + " = ev -> " + view.getClass().getSimpleName() + "." + target.adjustmentListener() + "(ev)");
            }
        }

        /**
         * @param component {@link SwingComponent} annotated
         */
        private static void setWindowListener(Object controller, Object view, Field component) {
            SwingComponent target = component.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            if (!target.windowClosing().isEmpty()) {
                Method method_windowClosing = getMethod(controller, target.windowClosing());
                Object value = BeanUtil.getFieldValue(component, view);
                WindowAdapter windowAdapter = new WindowAdapter() {
                    public void windowClosing(WindowEvent ev) {
                        BeanUtil.invoke(method_windowClosing, controller, ev);
                    }
                };
                if (JWindow.class.isInstance(value)) {
                    JWindow.class.cast(value).addWindowListener(windowAdapter);
                } else if (JFrame.class.isInstance(value)) {
                    JFrame.class.cast(value).addWindowListener(windowAdapter);
                } else {
                    throw new IllegalStateException(value.getClass().getName());
                }
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.windowClosing() + "(ev)");
            }
        }

        /**
         * @param component {@link SwingComponent} annotated
         */
        private static void setMouseMotionListener(Object controller, Object view, Field component) {
            SwingComponent target = component.getAnnotation(SwingComponent.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingComponent");
            }

            if (!target.mouseClicked().isEmpty() ||
                    !target.mouseReleased().isEmpty() ||
                    !target.mouseDragged().isEmpty() ||
                    !target.mousePressed().isEmpty()) {
                Method method_mouseClicked = getMethod(controller, target.mouseClicked());
                Method method_mouseReleased = getMethod(controller, target.mouseReleased());
                Method method_mouseDragged = getMethod(controller, target.mouseDragged());
                Method method_mousePressed = getMethod(controller, target.mousePressed());
                MouseInputListener mil = new MouseInputAdapter() {
                    public void mouseClicked(MouseEvent ev) {
                        if (method_mouseClicked != null) {
                            BeanUtil.invoke(method_mouseClicked, controller, ev);
                        }
                    }
                    public void mousePressed(MouseEvent ev) {
                        if (method_mousePressed != null) {
                            BeanUtil.invoke(method_mousePressed, controller, ev);
                        }
                    }
                    public void mouseDragged(MouseEvent ev) {
                        if (method_mouseDragged != null) {
                            BeanUtil.invoke(method_mouseDragged, controller, ev);
                        }
                    }
                    public void mouseReleased(MouseEvent ev) {
                        if (method_mouseReleased != null) {
                            BeanUtil.invoke(method_mouseReleased, controller, ev);
                        }
                    }
                };
                JComponent.class.cast(BeanUtil.getFieldValue(component, view)).addMouseListener(mil);
                JComponent.class.cast(BeanUtil.getFieldValue(component, view)).addMouseMotionListener(mil);
if (!target.mouseClicked().isEmpty()) logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.mouseClicked() + "(ev)");
if (!target.mouseReleased().isEmpty()) logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.mouseReleased() + "(ev)");
if (!target.mouseDragged().isEmpty()) logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.mouseDragged() + "(ev)");
if (!target.mousePressed().isEmpty()) logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + target.mousePressed() + "(ev)");
            }
        }

        /** */
        public static void addListeners(Object controller, Object view, Field component) {
            setListSelectionListener(controller, view, component);
            setAdjustmentListener(controller, view, component);
            setWindowListener(controller, view, component);
            setMouseMotionListener(controller, view, component);
        }
    }
}

/* */
