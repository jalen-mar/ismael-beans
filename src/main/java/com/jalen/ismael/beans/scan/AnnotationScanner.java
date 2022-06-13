package com.jalen.ismael.beans.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.jalen.ismael.beans.annotation.Autowired;
import com.jalen.ismael.beans.annotation.Bean;
import com.jalen.ismael.beans.annotation.Configuration;
import com.jalen.ismael.beans.annotation.ConstructorMethod;
import com.jalen.ismael.beans.annotation.Param;
import com.jalen.ismael.beans.config.Arguments;
import com.jalen.ismael.beans.config.BeanConfig;
import com.jalen.ismael.beans.config.Constants;
import com.jalen.ismael.beans.config.DependConfig;
import com.jalen.ismael.beans.data.BasicStructure;
import com.jalen.ismael.beans.data.BeanHolder;
import com.jalen.ismael.beans.data.Structure;
import com.jalen.ismael.utils.AnnotationUtils;
import com.jalen.ismael.utils.MethodUtils;
import com.jalen.ismael.utils.ReflectUtils;

public class AnnotationScanner implements Scanner { 
    private final static Map<Class<?>, BeanNameConverter> converters = new HashMap<>();

    private final List<String> pendingClassNames;
    private final String packageName;
    private final CountDownLatch scanCtl;
    private final BeanHolder beanHolder;

    public AnnotationScanner(List<String> pendingClassNames, String packageName, BeanHolder beanHolder, CountDownLatch scanCtl) {
        this.pendingClassNames = pendingClassNames;
        this.packageName = packageName;
        this.beanHolder = beanHolder;
        this.scanCtl = scanCtl;
    }
    
    @Override
    public void run() {
        try {
            List<String> initializationClass = new ArrayList<>();
            for (String targetClassName : pendingClassNames) {
                if (targetClassName.startsWith(packageName)) {
                    initializationClass.add(targetClassName);
                    Class<?> targetClass = ReflectUtils.getClass(targetClassName);
                    Annotation annotation = AnnotationUtils.getAnnotation(targetClass, Bean.class);
                    if (annotation != null) {
                        preBeanConfig(targetClass, annotation);
                        continue;
                    }
                    annotation = AnnotationUtils.getAnnotation(targetClass, Configuration.class);
                    if (annotation != null) {
                        preBeanConfig(targetClass, null);
                        Method[] methods = targetClass.getDeclaredMethods();
                        for (Method m : methods) {
                            annotation = AnnotationUtils.getAnnotation(m, Bean.class);
                            if (annotation != null) {
                                preBeanConfigFactory(targetClass, m, annotation);
                            }
                        }
                    }
                }
            }
            pendingClassNames.removeAll(initializationClass);
        } finally {
            scanCtl.countDown();
        }
    }

    private static String getBeanName(Annotation annotation) {
        Class<BeanNameConverter> converterClass = MethodUtils.invokeAnnotation(annotation, Constants.CLASSNAME_CONVERTER, Bean.class);
        if (converterClass != BeanNameConverter.class) {
            BeanNameConverter converter = converters.get(converterClass);
            if (converter == null) {
                converter = ReflectUtils.newInstance(converterClass);
                converters.put(converterClass, converter);
            }
            return converter.convert(annotation);
        } else {
            return MethodUtils.invokeAnnotation(annotation, Constants.FIELD_NAME, Bean.class);
        }
    }

    private static void preArguments(Class<?>[] parameterTypes, Annotation[][] annotations, BeanConfig beanConfig) {
        String[] argumentName = null;
        int count = parameterTypes.length;
        for (int i = 0; i < count; i++) {
            Annotation[] as = annotations[i];
            if (as.length != 0) {
                for (Annotation a : as) {
                    if (a instanceof Param || a.annotationType().isAnnotationPresent(Param.class)) {
                        if (argumentName == null) {
                            argumentName = new String[count];
                        }
                        argumentName[i] = MethodUtils.invoke(a, Constants.FIELD_VALUE, null);
                        break;
                    }
                }
            }
        }
        if (count != 0) {
            beanConfig.setConstructorArguments(new Arguments(parameterTypes, argumentName));
        }
    }

    public static void injectDependConfig(BeanConfig beanConfig) {
        beanConfig.setDependConfigs(scanAutowired(beanConfig.getTargetClass()));
    }

    public static DependConfig[] scanAutowired(Class<?> targetClass) {
        Field[] fields = targetClass.getDeclaredFields();
        List<DependConfig> dependConfigs = new ArrayList<>();
        for (Field field : fields) {
            Annotation annotation = AnnotationUtils.getAnnotation(field, Autowired.class);
            if (annotation != null) {
                Class<?> fieldClass = field.getType();
                Type fieldType = field.getGenericType();
                String classTypeName = fieldClass.getName();
                String structureClassName = null;
                if (BasicStructure.isSupport(fieldClass) || Structure.class.isAssignableFrom(targetClass) ) {
                    if (fieldType instanceof ParameterizedType) {
                        structureClassName = classTypeName;
                        Type[] types = ((ParameterizedType) fieldType).getActualTypeArguments();
                        Type targetType = types[types.length - 1];
                        if (targetType instanceof Class<?>) {
                            fieldClass = (Class<?>) targetType;
                            classTypeName = fieldClass.getName();
                        } else {
                            fieldClass = (Class<?>) ((ParameterizedType) targetType).getRawType();
                        }
                        fieldType = targetType;
                    }
                }
                if (fieldType instanceof ParameterizedType) {
                    classTypeName = fieldType.toString();
                }
                DependConfig dependConfig = new DependConfig(getBeanName(annotation), fieldClass);
                dependConfig.setStructureClassName(structureClassName);
                dependConfig.setClassTypeName(classTypeName);
                dependConfig.setFieldName(field.getName());
                String[] args = MethodUtils.invokeAnnotation(annotation, Constants.FIELD_ARGS, Autowired.class);
                if (args.length != 0) {
                    dependConfig.setArguments(args);
                }
                dependConfigs.add(dependConfig);
            }
        }
        return dependConfigs.isEmpty() ? null : dependConfigs.toArray(new DependConfig[dependConfigs.size()]);
    }

    private void preBeanConfig(Class<?> targetClass, Annotation annotation) {
        BeanConfig beanConfig;
        if (annotation != null) {
            beanConfig = new BeanConfig(getBeanName(annotation), targetClass);
            beanConfig.setOrder(MethodUtils.invokeAnnotation(annotation, Constants.FIELD_ORDER, Bean.class));
            beanConfig.setVersion(MethodUtils.invokeAnnotation(annotation, Constants.FIELD_VERSION, Bean.class));
            beanConfig.setScopeType(MethodUtils.invokeAnnotation(annotation, Constants.FIELD_SCOPE, Bean.class));
            Constructor<?> constructor = ReflectUtils.getConstructor(targetClass, ConstructorMethod.class);
            if (constructor != null) {
                preArguments(constructor.getParameterTypes(), constructor.getParameterAnnotations(), beanConfig);
            }
        } else {
            beanConfig = new BeanConfig(null, targetClass);
        }
        injectDependConfig(beanConfig);
        beanHolder.register(beanConfig, null);
    }

    private void preBeanConfigFactory(Class<?> factoryClass, Method m, Annotation annotation) {
        Class<?> returnClass = m.getReturnType();
        Type returnType = m.getGenericReturnType();
        String classTypeName = null;
        BeanConfig beanConfig = new BeanConfig(getBeanName(annotation), returnClass);
        beanConfig.setOrder(MethodUtils.invokeAnnotation(annotation, "order", Bean.class));
        beanConfig.setVersion(MethodUtils.invokeAnnotation(annotation, "version", Bean.class));
        beanConfig.setScopeType(MethodUtils.invokeAnnotation(annotation, "scope", Bean.class));
        beanConfig.setFactoryName(factoryClass.getName());
        beanConfig.setFactoryMethodName(m.getName());
        preArguments(m.getParameterTypes(), m.getParameterAnnotations(), beanConfig);
        if (returnType instanceof ParameterizedType) {
            classTypeName = returnType.toString();
            beanConfig.setGenericsClass(true);
        }
        injectDependConfig(beanConfig);
        beanHolder.register(beanConfig, classTypeName);
    }
}
