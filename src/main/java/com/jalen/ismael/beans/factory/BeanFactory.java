package com.jalen.ismael.beans.factory;

import com.jalen.ismael.beans.config.ResolvableType;

public interface BeanFactory { 
    <T> T getBean(String name);

    <T> T getBean(String name, String... args);

    <T> T getBean(String name, Class<?> structureClass);

    <T> T getBean(String name, Class<?> structureClass, String... args);

    <T> T getBean(Class<?> cls);

    <T> T getBean(Class<?> cls, String... args);

    <T> T getBean(Class<?> cls, Class<?> structureClass);

    <T> T getBean(Class<?> cls, Class<?> structureClass, String... args);

    <T> T getBean(ResolvableType type);

    <T> T getBean(ResolvableType type, String... args);

    <T> T getBean(ResolvableType type, Class<?> structureClass);

    <T> T getBean(ResolvableType type, Class<?> structureClass, String... args);

    void register4Annotation(Object targetObject);

    void register4Annotation(Object targetObject, String name);

    void register4Annotation(Class<?> targetClass, Object targetObject);

    void register4Annotation(Class<?> targetClass, Object targetObject, String name);
}
