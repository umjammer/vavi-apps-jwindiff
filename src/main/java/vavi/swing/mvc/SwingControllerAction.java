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

import vavi.util.properties.annotation.Property;


/**
 * SwingControllerAction.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2020/03/21 nsano initial version <br>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SwingControllerAction {

    /** TODO use jelly syntax */
    String[] view() default {};

    /** */
    class Util {

        private Util() {
        }

        /**
         * @param field {@link Property} annotated
         */
        public static String[] getView(Field field) {
            SwingControllerAction target = field.getAnnotation(SwingControllerAction.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingControllerAction");
            }

            return target.view();
        }

        /**
         * @param method {@link Property} annotated
         */
        public static String[] getView(Method method) {
            SwingControllerAction target = method.getAnnotation(SwingControllerAction.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @SwingControllerAction");
            }

            return target.view();
        }
    }
}

/* */
