package com.jalen.ismael.beans.config;

import com.jalen.ismael.utils.ReflectUtils;
import com.jalen.ismael.utils.StringUtil;

public class BeanConfig implements Cloneable { 
    private String beanName;
    private final String className;
    private boolean isGenericsClass;

    private String factoryName;
    private String factoryMethodName;
    private Arguments factoryMethodArguments;
    private Arguments constructorArguments;
    private DependConfig[] dependConfigs;

    private int order;
    private int version;
    private int scopeType = ScopeType.SINGLETON;
    private Object targetBean;

    public BeanConfig(String beanName, Class<?> targetBeanClass) {
        className = targetBeanClass.getName();
        if (StringUtil.isNotEmpty(beanName)) {
            this.beanName = beanName;
        } else {
            this.beanName = className;
        }
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(ResolvableType type) {
        if (isGenericsClass) {
            this.beanName += "<" + type.getGenericsName() + ">";
        }
    }

    public Class<?> getTargetClass() {
        return ReflectUtils.getClass(className);
    }

    public boolean isGenericsClass() {
        return isGenericsClass;
    }

    public void setGenericsClass(boolean genericsClass) {
        isGenericsClass = genericsClass;
    }

    public boolean byFactory() {
        return StringUtil.isNotEmpty(factoryName) && StringUtil.isNotEmpty(factoryMethodName);
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    public Arguments getFactoryMethodArguments() {
        return factoryMethodArguments;
    }

    public void setFactoryMethodArguments(Arguments factoryMethodArguments) {
        this.factoryMethodArguments = factoryMethodArguments;
    }

    public Arguments getConstructorArguments() {
        return constructorArguments;
    }

    public void setConstructorArguments(Arguments constructorArguments) {
        this.constructorArguments = constructorArguments;
    }

    public Arguments getArguments() {
        return byFactory() ? factoryMethodArguments : constructorArguments;
    }

    public DependConfig[] getDependConfigs() {
        return dependConfigs;
    }

    public void setDependConfigs(DependConfig[] dependConfigs) {
        this.dependConfigs = dependConfigs;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isSingleTen() {
        return (scopeType & ScopeType.SINGLETON) == ScopeType.SINGLETON;
    }

    public void setScopeType(int scopeType) {
        this.scopeType = scopeType;
    }

    public Object getTargetBean() {
        return targetBean;
    }

    public void setTargetBean(Object targetBean) {
        this.targetBean = targetBean;
    }

    @Override
    public BeanConfig clone() {
        try {
            BeanConfig result = (BeanConfig) super.clone();
            result.targetBean = null;
            return result;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }
}
