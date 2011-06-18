package com.trigonic.utils.spring.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Provides a simple bean for managing the lifecycle of {@link EmbeddedWebContextConnector} registrations of embedded
 * web contexts. Assumes the specified context is scoped to the lifecycle of this bean, registering at initialization
 * and unregistering at destruction.
 */
public class EmbeddedWebContextRegistrar implements ApplicationContextAware, InitializingBean, DisposableBean {
    private ApplicationContext appContext;
    private String embeddedWebContext;

    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }

    @Required
    public void setEmbeddedWebContext(String embeddedWebContext) {
        this.embeddedWebContext = embeddedWebContext;
    }

    public void afterPropertiesSet() throws Exception {
        EmbeddedWebContextConnector.registerAppContext(embeddedWebContext, appContext);
    }

    public void destroy() throws Exception {
        EmbeddedWebContextConnector.unregisterAppContext(embeddedWebContext);
    }
}
