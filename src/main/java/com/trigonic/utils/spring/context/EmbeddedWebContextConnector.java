package com.trigonic.utils.spring.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

/**
 * This class allows linking application contexts between an outer application and inner servlet contexts hosted by an
 * embedded web server.
 * 
 * <p>
 * To use:
 * <ul>
 * <li>In the inner servlet context, specify a context-param in the web.xml file specifying <tt>embeddedWebContext</tt>
 * with a name for this context. Also add {@link EmbeddedWebContextConnector} as a listener.
 * <li>In the outer application, call {@link #registerAppContext(String, ApplicationContext)} for each embedded servlet
 * context before starting the embedded web server. The name specified should match the embeddedWebContext context-param
 * specified in the web.xml.
 * </ul>
 */
public class EmbeddedWebContextConnector extends ContextLoaderListener {
    private static final Map<String, ApplicationContext> appContextMap;

    static {
        appContextMap = new ConcurrentHashMap<String, ApplicationContext>();
    }

    /**
     * Registers the specified embedded web context.
     * 
     * @return the previously-registered {@link ApplicationContext} or null if nothing previously-registered
     */
    public static ApplicationContext registerAppContext(String embeddedWebContext, ApplicationContext appContext) {
        return appContextMap.put(embeddedWebContext, appContext);
    }

    /**
     * Unregisters the specified embedded web context.
     * 
     * @return the {@link ApplicationContext} that had been registered or null if nothing had been registered
     */
    public static ApplicationContext unregisterAppContext(String embeddedWebContext) {
        return appContextMap.remove(embeddedWebContext);
    }

    /**
     * Loads the parent {@link ApplicationContext} for the {@link ServletContext} by retrieving the previously
     * {@link #registerAppContext(String, ApplicationContext) registered} one for the embedded web context.
     */
    @Override
    protected ApplicationContext loadParentContext(ServletContext servletContext) {
        String name = servletContext.getInitParameter("embeddedWebContext");
        if (name == null) {
            throw new IllegalStateException("context param [embeddedWebContext] not specified for ["
                    + servletContext.getServletContextName() + "]");
        }

        ApplicationContext appContext = appContextMap.get(name);
        if (appContext == null) {
            throw new IllegalStateException("application context not registered for [" + name + "]");
        }

        return appContext;
    }
}
