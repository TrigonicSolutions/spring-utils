package com.trigonic.utils.spring.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Surprisingly, Spring doesn't seem to have an {@link AbstractXmlApplicationContext} that pulls from a set of
 * {@link Resource resources}, it only has {@link org.springframework.context.support.ClassPathXmlApplicationContext}
 * and {@link org.springframework.context.support.FileSystemXmlApplicationContext}, both of which construct themselves
 * with a set of {@link String strings}.  This provides an {@link ApplicationContext} that can be constructed with
 * a collection of {@link Resource resources}.
 */
public class XmlResourceApplicationContext extends AbstractXmlApplicationContext {
    private final Resource[] configResources;

    public XmlResourceApplicationContext(Resource... configResources) {
        this(configResources, null);
    }

    public XmlResourceApplicationContext(Resource[] configResources, ApplicationContext parent) {
        super(parent);
        this.configResources = configResources;
        refresh();
    }

    @Override
    protected Resource[] getConfigResources() {
        return configResources;
    }
}
