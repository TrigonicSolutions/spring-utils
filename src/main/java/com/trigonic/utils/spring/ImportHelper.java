/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trigonic.utils.spring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * Helper to import bean definitions from another resource. This is tolerant of missing resources and allows the caller
 * to decide how to handle that condition. This is adapted from Spring's
 * <tt>DefaultBeanDefinitionDocumentReader.importBeanDefinitionResource</tt> (since that's not externally reachable) and
 * <tt>AbstractBeanDefinitionReader.loadBeanDefinitions</tt> (since we need to detect when the resource isn't
 * available).
 */
public class ImportHelper {
    private static final Log logger = LogFactory.getLog(ImportHelper.class);

    public static Set<Resource> importResource(XmlBeanDefinitionReader reader, Resource sourceResource, String location) {
        location = SystemPropertyUtils.resolvePlaceholders(location); // resolve system properties: e.g. "${user.dir}"
        Set<Resource> actualResources = new LinkedHashSet<Resource>(4);

        if (isAbsoluteLocation(location)) {
            importAbsoluteResource(reader, location, actualResources);
        } else {
            importRelativeResource(reader, sourceResource, location, actualResources);
        }

        return actualResources;
    }

    private static boolean isAbsoluteLocation(String location) {
        boolean absoluteLocation = false;
        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException ex) {
            // cannot convert to an URI, considering the location relative unless it has the "classpath*:" prefix
        }
        return absoluteLocation;
    }

    private static void importRelativeResource(XmlBeanDefinitionReader reader, Resource sourceResource,
            String location, Set<Resource> actualResources) {
        int importCount = 0;
        try {
            Resource relativeResource = sourceResource.createRelative(location);
            if (relativeResource.exists()) {
                importCount = reader.loadBeanDefinitions(relativeResource);
                actualResources.add(relativeResource);
            }
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Could not resolve current location [" + location + "]", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
        }
    }

    private static void importAbsoluteResource(XmlBeanDefinitionReader reader, String location,
            Set<Resource> actualResources) {
        ResourceLoader resourceLoader = reader.getResourceLoader();
        if (resourceLoader == null) {
            throw new BeanDefinitionStoreException("Cannot import bean definitions from location [" + location
                    + "]: no ResourceLoader available");
        }

        if (resourceLoader instanceof ResourcePatternResolver) {
            importAbsoluteResourcePattern(reader, location, actualResources, (ResourcePatternResolver) resourceLoader);
        } else {
            importSingleAbsoluteResource(reader, location, actualResources, resourceLoader);
        }
    }

    private static void importAbsoluteResourcePattern(XmlBeanDefinitionReader reader, String location,
            Set<Resource> actualResources, ResourcePatternResolver resourceLoader) {
        try {
            List<Resource> resources = new ArrayList<Resource>(Arrays.asList(resourceLoader.getResources(location)));
            int loadCount = 0;
            for (Resource resource : resources) {
                if (resource.exists()) {
                    loadCount += reader.loadBeanDefinitions(resource);
                    actualResources.add(resource);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("Could not resolve bean definition resource pattern [" + location
                    + "]", ex);
        }
    }

    private static void importSingleAbsoluteResource(XmlBeanDefinitionReader reader, String location,
            Set<Resource> actualResources, ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(location);
        if (resource.exists()) {
            int loadCount = reader.loadBeanDefinitions(resource);
            if (actualResources != null) {
                actualResources.add(resource);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
            }
        }
    }
}
