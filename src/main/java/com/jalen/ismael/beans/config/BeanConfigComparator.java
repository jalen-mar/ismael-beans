package com.jalen.ismael.beans.config;

import java.util.Comparator;

public class BeanConfigComparator implements Comparator<BeanConfig> { 
    private final boolean desc;

    public BeanConfigComparator(boolean desc) {
        this.desc = desc;
    }

    @Override
    public int compare(BeanConfig o1, BeanConfig o2) {
        return desc ? (o1.getOrder() - o2.getOrder()) : (o2.getOrder() - o1.getOrder());
    }
}
