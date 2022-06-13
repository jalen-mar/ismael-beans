package com.jalen.ismael.beans.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jalen.ismael.annotation.IsmaelPlugin;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@IsmaelPlugin
public @interface BeanFactoryPlugin { 
    
}
