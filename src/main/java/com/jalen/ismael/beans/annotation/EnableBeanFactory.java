package com.jalen.ismael.beans.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jalen.ismael.annotation.EnablePlugin;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnablePlugin(className = "com.jalen.ismael.beans.BeanFactoryStarter", order = -1000)
public @interface EnableBeanFactory { 
    Class<?> providerClassName();
}