package com.jalen.ismael.beans.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jalen.ismael.beans.config.ScopeType;
import com.jalen.ismael.beans.scan.BeanNameConverter;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Bean { 
    String name() default "";
    int order() default 0;
    int scope() default ScopeType.SINGLETON;
    int version() default 0;
    Class<? extends BeanNameConverter> beanNameConverter() default BeanNameConverter.class;
}
