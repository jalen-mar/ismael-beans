package com.jalen.ismael.beans.factory;

import java.util.Map;

import com.jalen.ismael.beans.config.ResolvableType;
import com.jalen.ismael.beans.data.BasicStructure;
import com.jalen.ismael.beans.data.BeanHolder;

public class MultipleBeanFactory extends SingletonBeanFactory { 

    public MultipleBeanFactory(BeanHolder holder) {
        super(holder);
    }

    @Override
    public <T> T getBean(String name) {
        return super.getBean(name);
    }

    @Override
    public <T> T getBean(String name, String... args) {
        return super.getBean(name, args);
    }

    @Override
    public <T> T getBean(String name, Class<?> structureClass) {
        return getBean(name, structureClass, (String[]) null);
    }

    @Override
    public <T> T getBean(String name, Class<?> structureClass, String... args) {
        return null;
    }

    @Override
    public <T> T getBean(Class<?> cls) {
        return super.getBean(cls);
    }

    @Override
    public <T> T getBean(Class<?> cls, String... args) {
        return super.getBean(cls, args);
    }

    @Override
    public <T> T getBean(Class<?> cls, Class<?> structureClass) {
        return getBean(cls, structureClass, (String[]) null);
    }

    @Override
    public <T> T getBean(Class<?> cls, Class<?> structureClass, String... args) {
        return getBean(ResolvableType.forRawClass(cls), structureClass, (String[]) null);
    }

    @Override
    public <T> T getBean(ResolvableType type) {
        return super.getBean(type);
    }

    @Override
    public <T> T getBean(ResolvableType type, String... args) {
        return super.getBean(type, args);
    }

    @Override
    public <T> T getBean(ResolvableType type, Class<?> structureClass) {
        return getBean(type, structureClass, (String[]) null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(ResolvableType type, Class<?> structureClass, String... args) {
        Object object = super.getBean(type, structureClass, args);
        if (BasicStructure.isEmpty(object)) {
            String classTypeName;
            if (Map.class.isAssignableFrom(structureClass)) {
                classTypeName = structureClass.getName() + "<java.lang.String, " + type.toString() + ">";
            } else {
                classTypeName = structureClass.getName() + "<" + type.toString() + ">";
            }
            object = getBean(ResolvableType.forRawClass(structureClass, classTypeName, true), args);
        }
        return (T) object;
    }
    
}
