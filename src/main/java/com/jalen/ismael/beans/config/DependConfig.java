package com.jalen.ismael.beans.config;

import com.jalen.ismael.utils.ReflectUtils;
import com.jalen.ismael.utils.StringUtil;

public class DependConfig { 
    private String name;
    private String className;
    private String classTypeName;
    private String structureClassName;
    private String[] arguments;

    private String fieldName;

    public DependConfig(String name, Class<?> cls) {
        className = cls.getName();
        if (StringUtil.isNotEmpty(name)) {
            this.name = name;
        }
    }

    public boolean isAssignableFromStructure() {
        return structureClassName != null;
    }

    public Class<?> getStructureClass() {
        return ReflectUtils.getClass(structureClassName);
    }

    public void setStructureClassName(String structureClassName) {
        this.structureClassName = structureClassName;
    }

    public boolean byType() {
        return StringUtil.isEmpty(name);
    }

    public String getName() {
        return name;
    }

    public Class<?> getDependClass() {
        return ReflectUtils.getClass(className);
    }

    public String getClassTypeName() {
        return classTypeName;
    }

    public void setClassTypeName(String classTypeName) {
        this.classTypeName = classTypeName;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
