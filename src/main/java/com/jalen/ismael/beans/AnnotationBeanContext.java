package com.jalen.ismael.beans;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jalen.ismael.beans.annotation.CodeblockScanner;
import com.jalen.ismael.beans.config.DependConfig;
import com.jalen.ismael.beans.config.ResolvableType;
import com.jalen.ismael.beans.config.ScanConfig;
import com.jalen.ismael.beans.data.BeanHolder;
import com.jalen.ismael.beans.factory.MultipleBeanFactory;
import com.jalen.ismael.beans.scan.AnnotationScanner;
import com.jalen.ismael.utils.FieldUtils;
import com.jalen.ismael.utils.MethodUtils;

public class AnnotationBeanContext extends BeanContext { 
    private final BeanHolder beanHolder = new BeanHolder();
    private MultipleBeanFactory defaultBeanFactory;

    @Override
    protected void init(ScanConfig scanConfig) {
        Method method = MethodUtils.getMethod(scanConfig.getProviderClass(), CodeblockScanner.class);
        Map<Integer, List<String>> pendingClassNameContainer = new TreeMap<>();
        int blockCount = scanConfig.getBlockCount();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(scanConfig.getBlockCount());
        try {
            for (String packageName : scanConfig.getPackages()) {
                CountDownLatch scanCtl = new CountDownLatch(blockCount);
                try {
                    for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
                        List<String> pendingClassNames = pendingClassNameContainer.get(blockIndex);
                        if (pendingClassNames == null) {
                            pendingClassNames = MethodUtils.invoke(scanConfig.getProviderInstance(), method, blockIndex);
                            pendingClassNameContainer.put(blockIndex, pendingClassNames);
                        }
                        executor.execute(new AnnotationScanner(pendingClassNames, packageName, beanHolder, scanCtl));
                    }
                    scanCtl.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.toString());
                }
            }
            pendingClassNameContainer.clear();
        } finally {
            executor.shutdown();
        }
        defaultBeanFactory = new MultipleBeanFactory(beanHolder);
    }

    @Override
    public <T> T getBean(String name) {
        return defaultBeanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, String... args) {
        return defaultBeanFactory.getBean(name, args);
    }

    @Override
    public <T> T getBean(String name, Class<?> structureClass) {
        return defaultBeanFactory.getBean(name, structureClass);
    }

    @Override
    public <T> T getBean(String name, Class<?> structureClass, String... args) {
        return defaultBeanFactory.getBean(name, structureClass, args);
    }

    @Override
    public <T> T getBean(Class<?> cls) {
        return defaultBeanFactory.getBean(cls);
    }

    @Override
    public <T> T getBean(Class<?> cls, String... args) {
        return defaultBeanFactory.getBean(cls, args);
    }

    @Override
    public <T> T getBean(Class<?> cls, Class<?> structureClass) {
        return defaultBeanFactory.getBean(cls, structureClass);
    }

    @Override
    public <T> T getBean(Class<?> cls, Class<?> structureClass, String... args) {
        return defaultBeanFactory.getBean(cls, structureClass, args);
    }

    @Override
    public <T> T getBean(ResolvableType type) {
        return defaultBeanFactory.getBean(type);
    }

    @Override
    public <T> T getBean(ResolvableType type, String... args) {
        return defaultBeanFactory.getBean(type, args);
    }

    @Override
    public <T> T getBean(ResolvableType type, Class<?> structureClass) {
        return defaultBeanFactory.getBean(type, structureClass);
    }

    @Override
    public <T> T getBean(ResolvableType type, Class<?> structureClass, String... args) {
        return defaultBeanFactory.getBean(type, structureClass, args);
    }

    @Override
    public void inject(Object targetObject) {
        DependConfig[] dependConfigs = AnnotationScanner.scanAutowired(targetObject.getClass());
        if (dependConfigs != null && dependConfigs.length != 0) {
            for (DependConfig dependConfig : dependConfigs) {
                Object dependBean;
                if (dependConfig.byType()) {
                    if (dependConfig.isAssignableFromStructure()) {
                        dependBean = defaultBeanFactory.getBean(ResolvableType.forRawClass(dependConfig.getDependClass(), dependConfig.getClassTypeName()), dependConfig.getStructureClass(), dependConfig.getArguments());
                    } else {
                        dependBean = defaultBeanFactory.getBean(ResolvableType.forRawClass(dependConfig.getDependClass(), dependConfig.getClassTypeName()), dependConfig.getArguments());
                    }
                } else {
                    if (dependConfig.isAssignableFromStructure()) {
                        dependBean = defaultBeanFactory.getBean(dependConfig.getName(), dependConfig.getStructureClass(), dependConfig.getArguments());
                    } else {
                        dependBean = defaultBeanFactory.getBean(dependConfig.getName(), dependConfig.getArguments());
                    }
                }
                FieldUtils.setValue(targetObject, dependConfig.getFieldName(), dependBean);
            }
        }
    }

    @Override
    public void register4Annotation(Object targetObject) {
        defaultBeanFactory.register4Annotation(targetObject);
    }

    @Override
    public void register4Annotation(Object targetObject, String name) {
        defaultBeanFactory.register4Annotation(targetObject, name);
    }

    @Override
    public void register4Annotation(Class<?> targetClass, Object targetObject) {
        defaultBeanFactory.register4Annotation(targetClass, targetObject);
    }

    @Override
    public void register4Annotation(Class<?> targetClass, Object targetObject, String name) {
        defaultBeanFactory.register4Annotation(targetClass, targetObject, name);
    }
}
