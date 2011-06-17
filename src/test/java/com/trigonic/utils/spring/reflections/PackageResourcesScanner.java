package com.trigonic.utils.spring.reflections;

import org.reflections.scanners.ResourcesScanner;

public class PackageResourcesScanner extends ResourcesScanner {
    private String prefix;
    
    public PackageResourcesScanner(String packageName) {
        this.prefix = packageName + ".";
    }
    
    @Override
    public boolean acceptsInput(String file) {
        return file.startsWith(prefix) && super.acceptsInput(file);
    }
}
