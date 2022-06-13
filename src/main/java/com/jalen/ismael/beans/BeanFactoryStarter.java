package com.jalen.ismael.beans;

import java.lang.reflect.Method;
import java.util.Map;

import com.jalen.ismael.annotation.PluginCompleted;
import com.jalen.ismael.annotation.PluginExecuted;
import com.jalen.ismael.annotation.PluginPrepared;
import com.jalen.ismael.beans.annotation.BeanFactoryPlugin;
import com.jalen.ismael.beans.annotation.CodeblockCounter;
import com.jalen.ismael.beans.annotation.PackageProvider;
import com.jalen.ismael.beans.config.Constants;
import com.jalen.ismael.beans.config.ScanConfig;
import com.jalen.ismael.utils.MethodUtils;
import com.jalen.ismael.utils.ReflectUtils;

@BeanFactoryPlugin
public class BeanFactoryStarter { 
    @PluginPrepared
    public ScanConfig prepared(Map<String, Object> params) {
        ScanConfig scanConfig = new ScanConfig();
        Class<?> providerClass = (Class<?>) params.get(Constants.CLASSNAME_PROVIDER);
        scanConfig.setProviderClass(providerClass);
        Object providerInstance = ReflectUtils.newInstance(providerClass);
        scanConfig.setProviderInstance(providerInstance);
        Method method = MethodUtils.getMethod(providerClass, CodeblockCounter.class);
        if (method != null) {
            scanConfig.setBlockCount(MethodUtils.invoke(providerInstance, method));
        }
        method = MethodUtils.getMethod(providerClass, PackageProvider.class);
        if (String.class.isAssignableFrom(method.getReturnType())) {
            scanConfig.setPackages(new String[]{ MethodUtils.invoke(providerInstance, method) });
        } else {
            scanConfig.setPackages(MethodUtils.invoke(providerInstance, method));
        }
        return scanConfig;
    }
    
    @PluginExecuted
    public BeanContext executed(ScanConfig scanConfig) {
        BeanContext result = null;
        switch (scanConfig.getScanType()) {
            case Constants.SCAN_TYPE_ANNOTATION: {
                result = new AnnotationBeanContext();
            }
            break;
        }
        result.init(scanConfig);
        return result;
    }

    @PluginCompleted
    public void executed(Object configruationObject, BeanContext beanContext) {
        beanContext.inject(configruationObject);
    }
}
