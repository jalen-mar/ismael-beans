package com.jalen.ismael.beans.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jalen.ismael.beans.config.Arguments;
import com.jalen.ismael.beans.config.BeanConfig;
import com.jalen.ismael.beans.config.BeanConfigComparator;
import com.jalen.ismael.beans.config.ResolvableType;
import com.jalen.ismael.utils.CollectionUtil;
import com.jalen.ismael.utils.StringUtil;

public class BeanHolder { 
    private final Map<String, BeanConfig> beanConfig = new HashMap<>(64);
    private final Map<String, BeanConfig> accurateBeanConfig = new HashMap<>(8);
    private final Map<String, String[]> beanNameCache = new HashMap<>(64);

    public BeanConfig getBeanConfig(String name, String genericsClassName, String[] args) {
        BeanConfig result;
        String beanName = name;
        if (!CollectionUtil.isEmpty(args) || StringUtil.isNotEmpty(genericsClassName)) {
            if (StringUtil.isNotEmpty(genericsClassName)) {
                beanName += "<" + genericsClassName + ">";
            }
            beanName += StringUtil.toString(args);
            result = accurateBeanConfig.get(beanName);
            if (result == null) {
                synchronized (accurateBeanConfig) {
                    result = accurateBeanConfig.get(beanName);
                    if (result == null) {
                        result = beanConfig.get(name);
                        if (result != null) {
                            accurateBeanConfig.put(beanName, result = result.clone());
                        }
                    }
                }
            }
        } else {
            result = beanConfig.get(beanName);
        }
        return result;
    }

    public String getBeanNameForType(ResolvableType type, String[] args) {
        String[] names = getBeanNamesForType(type, args);
        if (names.length == 0) {
            return null;
        }
        if (names.length > 1) {
            throw new RuntimeException("expected single matching bean but found " + names.length + ": " + StringUtil.toString(names));
        }
        return names[0];
    }

    public String[] getBeanNamesForType(ResolvableType type, String[] args) {
        String[] result = beanNameCache.get(type.toString());
        if (result == null) {
            synchronized (beanNameCache) {
                result = beanNameCache.get(type.toString());
                if (result == null) {
                    if (type.isPrototype()) {
                        BeanConfig config = beanConfig.get(type.toString());
                        result = new String[config == null ? 0 : 1];
                        if (config != null) {
                            result[0] = config.getBeanName();
                        }
                    } else {
                        Collection<BeanConfig> targetBeanConfigs;
                        if (type.hasGenerics()) {
                            targetBeanConfigs = getBeanConfig(type);
                        } else {
                            targetBeanConfigs = beanConfig.values();
                        }
                        result = findBeanNameForType(targetBeanConfigs, type, args);
                    }
                    if (result.length != 0) {
                        beanNameCache.put(type.toString(), result);
                    }
                }
            }
        }
        return result;
    }

    private Collection<BeanConfig> getBeanConfig(ResolvableType type) {
        Iterator<BeanConfig> iterator = beanConfig.values().iterator();
        List<BeanConfig> targetBeanConfig = new ArrayList<>();
        while (iterator.hasNext()) {
            BeanConfig beanConfig = iterator.next();
            if (type.isAssignableFrom(beanConfig.getTargetClass()) && !beanConfig.isGenericsClass()) {
                targetBeanConfig.add(beanConfig);
            }
        }
        return targetBeanConfig;
    }

    private String[] findBeanNameForType(Collection<BeanConfig> beanConfig, ResolvableType type, String[] args) {
        Iterator<BeanConfig> iterator = beanConfig.iterator();
        List<BeanConfig> beanConfigList = new ArrayList<>();
        while (iterator.hasNext()) {
            BeanConfig config = iterator.next();
            if (type.isAssignableFrom(config.getTargetClass())) {
                Arguments arguments = config.getArguments();
                if ((args == null && arguments == null) ||
                        (args != null && arguments != null && arguments.isReference(args))) {
                    config = cacheBeanConfig(config, type);
                    beanConfigList.add(config);
                }
            }
        }
        List<String> result = new ArrayList<>();
        Collections.sort(beanConfigList, new BeanConfigComparator(true));
        for (BeanConfig config : beanConfigList) {
            result.add(config.getBeanName());
        }
        return StringUtil.toStringArray(result);
    }

    private BeanConfig cacheBeanConfig(BeanConfig config, ResolvableType type) {
        if (!type.hasGenerics()) {
            config = config.clone();
            config.setGenericsClass(type.hasGenerics());
            config.setBeanName(type);
            this.beanConfig.put(config.getBeanName(), config);
        }
        return config;
    }

    public void register(BeanConfig config, String classTypeName) {
        synchronized (beanConfig) {
            BeanConfig oldBeanConfig = beanConfig.remove(config.getBeanName());
            if (oldBeanConfig != null && oldBeanConfig.getVersion() > config.getVersion()) {
                config = oldBeanConfig;
            }
            config.setBeanName(ResolvableType.forRawClass(config.getTargetClass(), classTypeName));
            beanConfig.put(config.getBeanName(), config);
        }
    }
}
