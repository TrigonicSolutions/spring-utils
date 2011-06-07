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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * Helper to import bean definitions from another resource. This is adapted from Spring's
 * <tt>DefaultBeanDefinitionDocumentReader.importBeanDefinitionResource</tt> since that's not externally reachable.
 */
public class ImportHelper {
    private static final Log logger = LogFactory.getLog(ImportHelper.class);

    public static Set<Resource> importResource(XmlBeanDefinitionReader reader, Resource sourceResource, String location) {
        // Resolve system properties: e.g. "${user.dir}"
        location = SystemPropertyUtils.resolvePlaceholders(location);
        Set<Resource> actualResources = new LinkedHashSet<Resource>(4);

        // Discover whether the location is an absolute or relative URI
        boolean absoluteLocation = false;
        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException ex) {
            // cannot convert to an URI, considering the location relative
            // unless it is the well-known Spring prefix "classpath*:"
        }

        // Absolute or relative?
        if (absoluteLocation) {
            int importCount = reader.loadBeanDefinitions(location, actualResources);
            if (logger.isDebugEnabled()) {
                logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
            }
        } else {
            // No URL -> considering resource location as relative to the
            // current file.
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

        return actualResources;
    }
}
