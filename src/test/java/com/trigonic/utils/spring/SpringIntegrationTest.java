package com.trigonic.utils.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringIntegrationTest {
    @Test
    public void primary() {
        performTest("primary");
    }

    @Test
    public void alternate() {
        performTest("alternate");
    }

    @Test
    public void falloverAlternate() {
        performTest("alternate");
    }

    @Test
    public void primaryNotAlternate() {
        performTest("primary");
    }

    @Test(expected=BeanDefinitionParsingException.class)
    public void neither() {
        performTest(null);
    }

    @Test
    public void neitherButOptional() {
        performTest(null);
    }

    @Test(expected=BeanDefinitionParsingException.class)
    public void missing() {
        performTest(null);
    }

    @Test
    public void missingButOptional() {
        performTest(null);
    }

    void performTest(String which) {
        String testName = new Exception().getStackTrace()[1].getMethodName();
        ApplicationContext appContext = getAppContext(testName);
        if (which == null) {
            assertFalse(appContext.containsBean("which"));
        } else {
            assertEquals(which, getAppContext(testName).getBean("which", String.class));
        }
    }

    ApplicationContext getAppContext(String name) {
        String location = "testcase/" + name + ".xml";
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(location, getClass());
        appContext.refresh();
        return appContext;
    }
}
