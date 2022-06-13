package com.jalen.ismael.beans;

import com.jalen.ismael.beans.config.ScanConfig;
import com.jalen.ismael.beans.factory.BeanFactory;

public abstract class BeanContext implements BeanFactory { 
    protected abstract void init(ScanConfig scanConfig);

    public abstract void inject(Object targetObject);
}
