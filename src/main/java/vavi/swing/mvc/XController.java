/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.swing.mvc;

import java.awt.event.ActionEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractButton;

import vavi.beans.BeanUtil;


/**
 * XController.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2020/03/21 nsano initial version <br>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XController {

    /** */
    class Util {

        private static Logger logger = Logger.getLogger(Util.class.getName());

        private Util() {
        }

        /**
         * @return {@link XController} annotated fields
         */
        public static Set<Field> getSwingControllerActionFields(Object bean) {
            //
            XController propsEntity = bean.getClass().getAnnotation(XController.class);
            if (propsEntity == null) {
                throw new IllegalArgumentException("bean is not annotated with @XController");
            }

            // lambda
            Set<Field> swingControllerActionFields = new HashSet<>();

            Class<?> clazz = bean.getClass();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    SwingControllerAction action = field.getAnnotation(SwingControllerAction.class);
                    if (action != null) {
                        swingControllerActionFields.add(field);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            return swingControllerActionFields;
        }

        /**
         * @return {@link XController} annotated methods
         */
        public static Set<Method> getSwingControllerActionMethods(Object bean) {
            //
            XController propsEntity = bean.getClass().getAnnotation(XController.class);
            if (propsEntity == null) {
                throw new IllegalArgumentException("bean is not annotated with @XController");
            }

            //
            Set<Method> swingControllerActionMethods = new HashSet<>();

            Class<?> clazz = bean.getClass();
            while (clazz != null) {
                for (Method field : clazz.getDeclaredMethods()) {
                    SwingControllerAction action = field.getAnnotation(SwingControllerAction.class);
                    if (action != null) {
                        swingControllerActionMethods.add(field);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            return swingControllerActionMethods;
        }

        /**
         * @return {@link XView} annotated fields
         */
        public static Set<Field> getSwingComponentFields(Object bean) {
            //
            XView propsEntity = bean.getClass().getAnnotation(XView.class);
            if (propsEntity == null) {
                throw new IllegalArgumentException("bean is not annotated with @XView");
            }

            //
            Set<Field> swingComponentFields = new HashSet<>();

            Class<?> clazz = bean.getClass();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    SwingComponent component = field.getAnnotation(SwingComponent.class);
                    if (component != null) {
                        swingComponentFields.add(field);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            return swingComponentFields;
        }

        /**
         * Entry point.
         */
        public static void bind(Object controller, Object view) {
            // TODO check super classes
            XController xcontroller = controller.getClass().getAnnotation(XController.class);
            if (xcontroller == null) {
                throw new IllegalArgumentException("controller is not annotated with @XController");
            }
            XView xview = view.getClass().getAnnotation(XView.class);
            if (xview == null) {
                throw new IllegalArgumentException("view is not annotated with @XView");
            }

            for (Field component : getSwingComponentFields(view)) {

                String action = SwingComponent.Util.getAction(component);
                if (!action.isEmpty()) {
                    bindAction(action, component, controller, view);
                }

                String viewAction = SwingComponent.Util.getView(component);
                if (!viewAction.isEmpty()) {
                    bindViewAction(viewAction, component, view);
                }

                SwingComponent.Util.addListeners(controller, view, component);
            }
        }

        /** */
        private static void bindViewAction(String viewAction, Field component, Object view) {
            AbstractButton.class.cast(BeanUtil.getFieldValue(component, view)).addActionListener(ev -> {
                try {
                    Method method = BeanUtil.getMethodByNameOf(view.getClass(), viewAction); // TODO limit @XViewAction
logger.info(view.getClass().getSimpleName() + "." + viewAction); 
                    BeanUtil.invoke(method, view);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + view.getClass().getSimpleName() + "." + viewAction + "()");
        }

        /** */
        private static void bindAction(String action, Field component, Object controller, Object view) {
            for (Field field : getSwingControllerActionFields(controller)) {
                if (field.getName().equals(action)) { // TODO cache
                }
            }

            for (Method method : getSwingControllerActionMethods(controller)) {
//logger.info(controller.getClass().getSimpleName() + "." + method.getName() + ", " + action); 
                if (method.getName().equals(action)) { // TODO cache
                    if (method.getParameterCount() == 0) {
                        AbstractButton.class.cast(BeanUtil.getFieldValue(component, view)).addActionListener(ev -> {
                            BeanUtil.invoke(method, controller);
                        });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + controller.getClass().getSimpleName() + "." + action + "()"); 
                    } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(ActionEvent.class)) {
                        AbstractButton.class.cast(BeanUtil.getFieldValue(component, view)).addActionListener(ev -> {
                            BeanUtil.invoke(method, controller, ev);
                        });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + controller.getClass().getSimpleName() + "." + action + "(ev)"); 
                    } else {
                        AbstractButton.class.cast(BeanUtil.getFieldValue(component, view)).addActionListener(ev -> {
                            String[] viewParams = SwingControllerAction.Util.getView(method);
                            Object[] args = new Object[viewParams.length];
                            int i = 0;
                            for (String viewParam : viewParams) {
                                String[] parts = viewParam.split("\\.");
                                Object value = view;
                                for (String part : parts) {
                                    value = BeanUtil.getValue(part, value);
                                }
                                args[i++] = value;
                            }
                            BeanUtil.invoke(method, controller, args);
                        });
logger.info(view.getClass().getSimpleName() + "." + component.getName() + " = ev -> " + controller.getClass().getSimpleName() + "." + action + "(" + Arrays.toString(method.getParameterTypes()).replaceAll("(class | interface |\\[|\\])", "") + ")"); 
                    }
                }
            }
        }
    }
}

/* */
