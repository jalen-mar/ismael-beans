package com.jalen.ismael.beans.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jalen.ismael.beans.scan.BeanNameConverter;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired { 
    String name() default "";
    String[] args() default {};
    Class<? extends BeanNameConverter> beanNameConverter() default BeanNameConverter.class;
}
