package com.trigonic.utils.spring.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.trigonic.utils.spring.context.XmlResourceApplicationContext;
import com.trigonic.utils.spring.reflections.PackageResourcesScanner;
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
    public static List<Resource> getParameters() {
        List<Resource> parameters = new ArrayList<Resource>();

        String testcasePackage = SpringIntegrationTest.class.getPackage().getName() + ".integ.testcase";
        Set<URL> urls = ClasspathHelper.getUrlsForPackagePrefix(testcasePackage);
        Scanner scanner = new PackageResourcesScanner(testcasePackage);
        Configuration configuration = new ConfigurationBuilder().setUrls(urls).setScanners(scanner);
        Reflections reflections = new Reflections(configuration);
        Set<String> keySet = reflections.getStore().get(PackageResourcesScanner.class).keySet();
        Set<String> testcases = reflections.getStore().get(PackageResourcesScanner.class, keySet.toArray(new String[keySet.size()]));

        for (String testcase : testcases) {
            parameters.add(new ClassPathResource(testcase));
        }

        return parameters;
    }
}
