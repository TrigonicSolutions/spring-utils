package com.trigonic.utils.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.trigonic.utils.spring.context.XmlResourceApplicationContext;
import com.trigonic.utils.spring.junit.Parameterized;
import com.trigonic.utils.spring.junit.Parameterized.LabelMaker;
import com.trigonic.utils.spring.junit.Parameterized.LabelMakerFactory;
import com.trigonic.utils.spring.junit.Parameterized.SimpleLabelMaker;

@RunWith(Parameterized.class)
public class SpringIntegrationTest {
    private Resource appContextResource;

    public SpringIntegrationTest(Resource appContextResource) {
        this.appContextResource = appContextResource;
    }

    /*
     * @Test public void primary() { performTest("primary"); }
     * 
     * @Test public void alternate() { performTest("alternate"); }
     * 
     * @Test public void falloverAlternate() { performTest("alternate"); }
     * 
     * @Test public void primaryNotAlternate() { performTest("primary"); }
     * 
     * @Test(expected=BeanDefinitionParsingException.class) public void neither() { performTest(null); }
     * 
     * @Test public void neitherButOptional() { performTest(null); }
     * 
     * @Test(expected=BeanDefinitionParsingException.class) public void missing() { performTest(null); }
     * 
     * @Test public void missingButOptional() { performTest(null); }
     * 
     * @Test public void classpathMissingButOptional() { performTest(null); }
     */

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
        return new SimpleLabelMaker() {
            public String getLabel(int index, Object[] parameters) {
                return ((Resource) parameters[0]).getFilename().split("\\.")[0];
            }
        };
    }

    @Parameters
    public static List<Resource> getParameters() {
        List<Resource> parameters = new ArrayList<Resource>();

        String testcasePackage = SpringIntegrationTest.class.getPackage().getName() + ".testcase";
        Set<URL> urls = ClasspathHelper.getUrlsForPackagePrefix(testcasePackage);
        Configuration configuration = new ConfigurationBuilder().setUrls(urls).setScanners(new ResourcesScanner());
        Reflections reflections = new Reflections(configuration);
        Set<String> testcases = reflections.getResources(Pattern.compile(".*\\.xml"));

        for (String testcase : testcases) {
            parameters.add(new ClassPathResource(testcase));
        }

        return parameters;
    }
}
