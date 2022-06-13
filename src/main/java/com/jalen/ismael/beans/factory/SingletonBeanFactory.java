package com.jalen.ismael.beans.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.jalen.ismael.beans.config.Arguments;
import com.jalen.ismael.beans.config.BeanConfig;
import com.jalen.ismael.beans.config.DependConfig;
import com.jalen.ismael.beans.config.ResolvableType;
import com.jalen.ismael.beans.data.BasicStructure;
import com.jalen.ismael.beans.data.BeanHolder;
import com.jalen.ismael.beans.data.Structure;
import com.jalen.ismael.beans.scan.AnnotationScanner;
import com.jalen.ismael.utils.CollectionUtil;
import com.jalen.ismael.utils.FieldUtils;
import com.jalen.ismael.utils.MethodUtils;
import com.jalen.ismael.utils.ReflectUtils;

public class SingletonBeanFactory implements BeanFactory { 
    private final BeanHolder holder;

    public SingletonBeanFactory(BeanHolder holder) {
        this.holder = holder;
    }

    @Override
    public <T> T getBean(String name) {
        return getBean(name, (String[]) null);
    }

    @Override
    public <T> T getBean(String name, String... args) {
        return getTargetBean(name, null, args);
    }

    @SuppressWarnings("unchecked")
    private  <T> T getTargetBean(String name, String genericsClassName, String... args) {
        BeanConfig beanConfig = holder.getBeanConfig(name, genericsClassName, args);
        if (beanConfig != null) {
            return (T) getTargetBean(beanConfig, args);
        }
        return null;
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
        return getBean(cls, (String[]) null);
    }

    @Override
    public <T> T getBean(Class<?> cls, String... args) {
        return getBean(ResolvableType.forRawClass(cls), args);
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
        return getBean(type, (String[]) null);
    }

    @Override
    public <T> T getBean(ResolvableType type, String... args) {
        String name = holder.getBeanNameForType(type, args);
        if (name != null) {
            return getTargetBean(name, type.isPrototype() ? null : type.getGenericsName(), args);
        }
        return null;
    }

    @Override
    public <T> T getBean(ResolvableType type, Class<?> structureClass) {
        return getBean(type, structureClass, (String[]) null);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(ResolvableType type, Class<?> structureClass, String... args) {
        String[] names = holder.getBeanNamesForType(type, args);
        Structure<?> targetStructure;
        if (Structure.class.isAssignableFrom(structureClass)) {
            targetStructure = (Structure<?>) ReflectUtils.newInstance(structureClass);
        } else {
            targetStructure = new BasicStructure<T>(structureClass);
        }
        if (!CollectionUtil.isEmpty(names)) {
            for (String name : names) {
                targetStructure.put(name, getBean(name, args));
            }
        }
        return (T) targetStructure.clone();
    }
    
    protected Object getTargetBean(BeanConfig beanConfig, String[] args) {
        if (beanConfig.isSingleTen()) {
            if (beanConfig.getTargetBean() == null) {
                synchronized (beanConfig) {
                    if (beanConfig.getTargetBean() == null) {
                        beanConfig.setTargetBean(newTargetBean(beanConfig, args));
                    }
                }
            }
            return beanConfig.getTargetBean();
        } else {
            return newTargetBean(beanConfig, args);
        }
    }

    private Object newTargetBean(BeanConfig beanConfig, String[] args) {
        Object result;
        if (beanConfig.byFactory()) {
            result = newTargetBeanByFactory(beanConfig, args);
        } else {
            result = newTargetBeanByConstructor(beanConfig, args);
        }
        injectDependObject(result, beanConfig.getDependConfigs());
        return result;
    }

    private Object newTargetBeanByFactory(BeanConfig beanConfig, String[] args) {
        Object factoryBean = getBean(beanConfig.getFactoryName());
        if (factoryBean != null) {
            String methodName = beanConfig.getFactoryMethodName();
            Arguments arguments = beanConfig.getFactoryMethodArguments();
            return MethodUtils.invoke(factoryBean,
                    MethodUtils.getMethod(factoryBean, methodName, arguments != null ? arguments.getArgumentType() : null),
                    arguments != null ? arguments.getArgumentObject(this, args) : null);
        }
        return null;
    }

    private Object newTargetBeanByConstructor(BeanConfig beanConfig, String[] args) {
        Arguments arguments = beanConfig.getConstructorArguments();
        Class<?>[] argsType = null;
        Object[] argsObject = null;
        if (arguments != null) {
            argsType = arguments.getArgumentType();
            argsObject = arguments.getArgumentObject(this, args);
        }
        try {
            Constructor<?> constructor = beanConfig.getTargetClass().getConstructor(argsType);
            return constructor.newInstance(argsObject);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void injectDependObject(Object targetBean, DependConfig[] dependConfigs) {
        if (dependConfigs != null && dependConfigs.length != 0) {
            for (DependConfig dependConfig : dependConfigs) {
                Object dependBean;
                if (dependConfig.byType()) {
                    if (dependConfig.isAssignableFromStructure()) {
                        dependBean = getBean(ResolvableType.forRawClass(dependConfig.getDependClass(), dependConfig.getClassTypeName()), dependConfig.getStructureClass(), dependConfig.getArguments());
                    } else {
                        dependBean = getBean(ResolvableType.forRawClass(dependConfig.getDependClass(), dependConfig.getClassTypeName()), dependConfig.getArguments());
                    }
                } else {
                    if (dependConfig.isAssignableFromStructure()) {
                        dependBean = getBean(dependConfig.getName(), dependConfig.getStructureClass(), dependConfig.getArguments());
                    } else {
                        dependBean = getBean(dependConfig.getName(), dependConfig.getArguments());
                    }
                }
                FieldUtils.setValue(targetBean, dependConfig.getFieldName(), dependBean);
            }
        }
    }

    @Override
    public void register4Annotation(Object targetObject) {
        register4Annotation(targetObject, null);
    }

    @Override
    public void register4Annotation(Object targetObject, String name) {
        register4Annotation(targetObject.getClass(), targetObject, null);
    }

    @Override
    public void register4Annotation(Class<?> targetClass, Object targetObject) {
        register4Annotation(targetClass, targetObject, null);
    }

    @Override
    public void register4Annotation(Class<?> targetClass, Object targetObject, String name) {
        BeanConfig beanConfig = new BeanConfig(name, targetClass);
        beanConfig.setTargetBean(targetObject);
        AnnotationScanner.injectDependConfig(beanConfig);
        injectDependObject(targetObject, beanConfig.getDependConfigs());
        holder.register(beanConfig, null);
    }
}
