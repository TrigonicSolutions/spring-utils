package com.trigonic.utils.spring.context;

import static org.easymock.EasyMock.createNiceControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.easymock.IMocksControl;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.StaticWebApplicationContext;

public class EmbeddedWebContextConnectorTest {
    @Test
    public void happyPath() {
        IMocksControl mockControl = createNiceControl();
        ApplicationContext mockOuterContext = mockControl.createMock(ApplicationContext.class);
        ServletContext mockServletContext = mockControl.createMock(ServletContext.class);
        ServletContextEvent mockServletContextEvent = mockControl.createMock(ServletContextEvent.class);
        Enumeration<?> mockEnumeration = mockControl.createMock(Enumeration.class);
        
        expect(mockServletContextEvent.getServletContext()).andReturn(mockServletContext).anyTimes();
        expect(mockServletContext.getInitParameter("embeddedWebContext")).andReturn("myWebContext");
        expect(mockServletContext.getInitParameter("contextClass")).andReturn(StaticWebApplicationContext.class.getName());
        expect(mockServletContext.getInitParameterNames()).andReturn(mockEnumeration).anyTimes();
        expect(mockServletContext.getAttributeNames()).andReturn(mockEnumeration).anyTimes();
        mockControl.replay();
        
        assertNull(EmbeddedWebContextConnector.registerAppContext("myWebContext", mockOuterContext));
        EmbeddedWebContextConnector connector = new EmbeddedWebContextConnector();
        connector.contextInitialized(mockServletContextEvent);
        assertSame(mockOuterContext, ContextLoader.getCurrentWebApplicationContext().getParent());
        connector.contextDestroyed(mockServletContextEvent);
        assertSame(mockOuterContext, EmbeddedWebContextConnector.unregisterAppContext("myWebContext"));
        mockControl.verify();
    }

    @Test
    public void missingContextParam() {
        IMocksControl mockControl = createNiceControl();
        ServletContext mockServletContext = mockControl.createMock(ServletContext.class);
        ServletContextEvent mockServletContextEvent = mockControl.createMock(ServletContextEvent.class);
        
        expect(mockServletContextEvent.getServletContext()).andReturn(mockServletContext);
        expect(mockServletContext.getServletContextName()).andReturn("missingContextParamAhoy");
        mockControl.replay();
        
        EmbeddedWebContextConnector connector = new EmbeddedWebContextConnector();
        try {
            connector.contextInitialized(mockServletContextEvent);
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not specified"));
            assertTrue(e.getMessage().contains("[embeddedWebContext]"));
            assertTrue(e.getMessage().contains("[missingContextParamAhoy]"));
        }
        mockControl.verify();
    }

    @Test
    public void unregistered() {
        IMocksControl mockControl = createNiceControl();
        ServletContext mockServletContext = mockControl.createMock(ServletContext.class);
        ServletContextEvent mockServletContextEvent = mockControl.createMock(ServletContextEvent.class);
        
        expect(mockServletContextEvent.getServletContext()).andReturn(mockServletContext).anyTimes();
        expect(mockServletContext.getInitParameter("embeddedWebContext")).andReturn("myWebContext");
        mockControl.replay();
        
        EmbeddedWebContextConnector connector = new EmbeddedWebContextConnector();
        try {
            connector.contextInitialized(mockServletContextEvent);
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not registered"));
            assertTrue(e.getMessage().contains("[myWebContext]"));
        }
        mockControl.verify();
    }
}
