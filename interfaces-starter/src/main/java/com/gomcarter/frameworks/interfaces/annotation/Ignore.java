package com.gomcarter.frameworks.interfaces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gomcarter 2020-07-14 08:19:15
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER,
        ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, ElementType.TYPE,
        ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE,
        ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
