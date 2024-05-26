/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.swing.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

import vavi.beans.BeanUtil;
import vavi.util.event.GenericEvent;


/**
 * XView.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2020/03/21 nsano initial version <br>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XView {

    /** */
    class Util {

        private static final Logger logger = Logger.getLogger(Util.class.getName());

        private Util() {
        }

        /**
         * @return {@link XView} annotated fields
         */
        public static Set<Field> getXViewActionFields(Object bean) {
            //
            XView annotation = bean.getClass().getAnnotation(XView.class);
            if (annotation == null) {
                throw new IllegalArgumentException("bean is not annotated with @XView");
            }

            // lambda
            Set<Field> viewActionFields = new HashSet<>();

            Class<?> clazz = bean.getClass();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    XViewAction action = field.getAnnotation(XViewAction.class);
                    if (action != null) {
                        viewActionFields.add(field);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            return viewActionFields;
        }

        /**
         * @return {@link XView} annotated methods
         */
        public static Set<Method> getXViewActionMethods(Object bean) {
            //
            XView annotation = bean.getClass().getAnnotation(XView.class);
            if (annotation == null) {
                throw new IllegalArgumentException("bean is not annotated with @XView");
            }

            //
            Set<Method> viewActionMethods = new HashSet<>();

            Class<?> clazz = bean.getClass();
            while (clazz != null) {
                for (Method field : clazz.getDeclaredMethods()) {
                    XViewAction action = field.getAnnotation(XViewAction.class);
                    if (action != null) {
                        viewActionMethods.add(field);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            return viewActionMethods;
        }

        /**
         * Entry point.
         * 1.1 bean#event.name = bean.@XViewAction-method(event.arg[0])
         * 1.2 bean#event.name = bean.@XViewAction-method(event.arg[0], event.arg[1])
         * 1.3 bean.@XViewAction-method()
         * 2.1 bean#event.name(event.arg[])
         * 2.2 bean#event.name(event.arg...)
         */
        @SuppressWarnings({"unchecked", "rawtypes",})
        public static void bind(Object bean, GenericEvent event) {
            // TODO check super classes
            XView xview = bean.getClass().getAnnotation(XView.class);
            if (xview == null) {
                throw new IllegalArgumentException("bean is not annotated with @XView");
            }

boolean done = false;
            for (Field field : getXViewActionFields(bean)) {
                if (field.getName().equals(event.getName())) { // TODO cache
                    if (field.getType().equals(Consumer.class)) {
                        ((Consumer) BeanUtil.getFieldValue(field, bean)).accept(event.getArguments()[0]);
done = true;
logger.info(field.getName() + ": " + field.getType().getSimpleName());
                    } else if (field.getType().equals(BiConsumer.class)) {
                        ((BiConsumer) BeanUtil.getFieldValue(field, bean)).accept(event.getArguments()[0],
                                                                                  event.getArguments()[1]);
done = true;
logger.info(field.getName() + ": " + field.getType().getSimpleName());
                    } else if (field.getType().equals(Runnable.class)) {
                        ((Runnable) BeanUtil.getFieldValue(field, bean)).run();
done = true;
logger.info(field.getName() + ": " + field.getType().getSimpleName());
                    } else {
logger.warning(field.getName() + ": " + field.getType().getName());
                    }
                }
            }

            for (Method method : getXViewActionMethods(bean)) {
                if (method.getName().equals(event.getName())) { // TODO cache
                    try {
                        if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isArray()) {
                            BeanUtil.invoke(method, bean, method.getParameterTypes()[0].cast(event.getArguments()));
                        } else {
                            BeanUtil.invoke(method,bean, event.getArguments());
                        }
done = true;
logger.info(bean.getClass().getSimpleName() + "." + method.getName() + "(" + Arrays.toString(method.getParameterTypes()).replaceAll("(class | interface |\\[|\\])", "") + ")");
                    } catch (Exception e) {
e.printStackTrace();
                        throw new IllegalStateException(method.getName() + "(" + Arrays.toString(method.getParameterTypes()).replaceAll("(class | interface |\\[|\\])", "") + "), " + method.getParameterCount());
                    }
                }
            }
if (!done) {
 throw new IllegalStateException(event.getName());
}
        }
    }
}
