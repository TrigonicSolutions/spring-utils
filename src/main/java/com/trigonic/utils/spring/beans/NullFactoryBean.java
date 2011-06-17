package com.trigonic.utils.spring.beans;

import org.springframework.beans.factory.FactoryBean;

public class NullFactoryBean<T> implements FactoryBean<T> {
    private Class<T> type;
    
    public void setClass(Class<T> type) {
        this.type = type;
    }

    public Class<?> getObjectType() {
        return type;
    }
    
    public T getObject() throws Exception {
        return null;
    }

    public boolean isSingleton() {
        return true;
    }
}
