package com.trigonic.utils.spring.context;

import java.io.File;
import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Surprisingly, Spring doesn't seem to have an {@link AbstractXmlApplicationContext} that pulls from a set of
 * {@link Resource resources}, it only has {@link org.springframework.context.support.ClassPathXmlApplicationContext}
 * and {@link org.springframework.context.support.FileSystemXmlApplicationContext}, both of which construct themselves
 * with a set of {@link String strings}.  This provides an {@link ApplicationContext} that can be constructed with
 * a collection of {@link Resource resources}.
 */
public class XmlResourceApplicationContext extends AbstractXmlApplicationContext {
    private final Resource[] configResources;

    public XmlResourceApplicationContext(Object... configResources) {
        this(null, configResources);
    }

    public XmlResourceApplicationContext(ApplicationContext parent, Object... configResources) {
        super(parent);
        this.configResources = normalize(configResources);
        refresh();
    }

    @Override
    protected Resource[] getConfigResources() {
        return configResources;
    }
    
    private Resource[] normalize(Object[] resources) {
        Resource[] results = new Resource[resources.length];
        for (int i = 0; i < resources.length; ++i) {
            results[i] = normalize(resources[i]);
        }
        return results;
    }

    private Resource normalize(Object resource) {
        Resource result;
        if (resource instanceof Resource) {
            result = (Resource) resource;
        } else if (resource instanceof String) {
            result = getResource((String) resource);
        } else if (resource instanceof File) {
            result = new FileSystemResource((File) resource);
        } else if (resource instanceof URL) {
            result = new UrlResource((URL) resource);
        } else {
            throw new IllegalArgumentException("unable to convert " + resource.getClass().getName() + " to " + Resource.class.getName());
        }
        return result;
    }
}
