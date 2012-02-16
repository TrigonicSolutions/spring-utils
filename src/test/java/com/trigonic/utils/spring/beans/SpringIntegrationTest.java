package com.trigonic.utils.spring.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import com.trigonic.utils.spring.context.XmlResourceApplicationContext;
import com.trigonic.utils.test.junit.DefaultLabelMaker;
import com.trigonic.utils.test.junit.LabelMaker;
import com.trigonic.utils.test.junit.LabelMakerFactory;
import com.trigonic.utils.test.junit.Parameterized;
import com.trigonic.utils.test.junit.Parameters;

@RunWith(Parameterized.class)
public class SpringIntegrationTest {
    private Resource appContextResource;

    public SpringIntegrationTest(Resource appContextResource) {
        this.appContextResource = appContextResource;
    }

    @Test
    public void performTest() throws IOException {
        boolean exceptionExpected = appContextResource.getURI().getPath().contains("/exception/");
        try {
            ApplicationContext appContext = new XmlResourceApplicationContext(appContextResource);
            String expected = appContext.getBean("expected", String.class);
            if (expected == null) {
                assertFalse(appContext.containsBean("which"));
            } else {
                assertEquals(expected, appContext.getBean("which", String.class));
            }
            assertFalse(exceptionExpected);
        } catch (BeanDefinitionParsingException e) {
            if (!exceptionExpected) {
                throw e;
            }
        }
    }
    
    @LabelMakerFactory
    public static LabelMaker getLabelMaker() {
        return new DefaultLabelMaker() {
            @Override
            public String getLabel(int index, Object[] parameters) {
                return ((Resource) parameters[0]).getFilename().split("\\.")[0];
            }
            
            @Override
            public String getTestName(int index, Object[] parameters, String methodName) {
                return methodName + "[" + getLabel(index, parameters) + "]";
            }
        };
    }

    @Parameters
    public static Resource[] getParameters() throws IOException {
        String testcasePackage = SpringIntegrationTest.class.getPackage().getName() + ".integ.testcase";
        String resourcePath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(testcasePackage));
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourcePath + "/**/*.xml";
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources(packageSearchPath);
    }
}
