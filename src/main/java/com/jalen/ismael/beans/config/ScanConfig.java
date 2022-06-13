package com.jalen.ismael.beans.config;

public class ScanConfig { 
    private int blockCount;
    private String[] packages;
    private Object providerInstance;
    private Class<?> providerClass;
    private int scanType;

    public ScanConfig() {
        blockCount = 1;
        scanType = Constants.SCAN_TYPE_ANNOTATION;
    }

    public int getBlockCount() {
        return blockCount;
    }
    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public String[] getPackages() {
        return packages;
    }
    public void setPackages(String[] packages) {
        this.packages = packages;
    }

    public Object getProviderInstance() {
        return providerInstance;
    }
    public void setProviderInstance(Object providerInstance) {
        this.providerInstance = providerInstance;
    }

    public Class<?> getProviderClass() {
        return providerClass;
    }
    public void setProviderClass(Class<?> providerClass) {
        this.providerClass = providerClass;
    }

    public int getScanType() {
        return scanType;
    }
    public void setScanType(int scanType) {
        this.scanType = scanType;
    }
}
