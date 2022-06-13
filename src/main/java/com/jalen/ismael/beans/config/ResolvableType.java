package com.jalen.ismael.beans.config;

public class ResolvableType { 
    private final Class<?> targetCLass;
    private String classTypeName;
    private boolean prototype;

    private ResolvableType(Class<?> targetCLass, String classTypeName, boolean prototype) {
        this.targetCLass = targetCLass;
        this.classTypeName = classTypeName;
        this.prototype = prototype;
    }


    public boolean hasGenerics() {
        return !classTypeName.equals(targetCLass.getName());
    }

    public static ResolvableType forRawClass(Class<?> targetCLass) {
        return forRawClass(targetCLass, targetCLass.getName());
    }

    public static ResolvableType forRawClass(Class<?> targetCLass, String classTypeName) {
        return forRawClass(targetCLass, classTypeName, false);
    }

    public static ResolvableType forRawClass(Class<?> targetCLass, String classTypeName, boolean prototype) {
        return new ResolvableType(targetCLass, classTypeName, prototype);
    }

    public String getGenericsName() {
        if (hasGenerics()) {
            return classTypeName.substring(targetCLass.getName().length() + 1, classTypeName.length() - 1);
        }
        return "";
    }

    public boolean isPrototype() {
        return prototype;
    }

    public void setClassTypeName(String classTypeName) {
        this.classTypeName = classTypeName;
    }

    public boolean isAssignableFrom(Class<?> cls) {
        return targetCLass.isAssignableFrom(cls);
    }

    @Override
    public String toString() {
        return classTypeName;
    }
}
