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

import java.util.Set;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Provides an extended <tt>&lt;xutils:import&gt;</tt> element that allows optional importing and fallback to a default.
 */
public class ImportBeanDefinitionParser extends AbstractBeanDefinitionParser {
    /**
     * This attribute indicates the primary resource to import.
     */
    public static final String RESOURCE_ATTRIBUTE = "resource";

    /**
     * This attribute, when true using {@link Boolean#parseBoolean(String)}, only imports the specified resource if it
     * exists.
     */
    public static final String OPTIONAL_ATTRIBUTE = "optional";

    /**
     * This attribute indicates an alternate resource to import if the primary resource doesn't exist. This will be
     * required over the primary resource unless <tt>optional</tt> is true.
     */
    public static final String ALTERNATE_ATTRIBUTE = "alternate";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        XmlReaderContext readerContext = parserContext.getReaderContext();
        String primaryLocation = element.getAttribute(RESOURCE_ATTRIBUTE);
        if (!StringUtils.hasText(primaryLocation)) {
            readerContext.error("Resource location must not be empty", element);
            return null;
        }

        String alternateLocation = element.getAttribute(ALTERNATE_ATTRIBUTE);
        boolean optional = Boolean.parseBoolean(element.getAttribute(OPTIONAL_ATTRIBUTE));

        String currentLocation = primaryLocation;
        try {
            Set<Resource> actualResources = ImportHelper.importResource(readerContext.getReader(),
                    readerContext.getResource(), currentLocation);

            if (actualResources.isEmpty() && alternateLocation != null) {
                currentLocation = alternateLocation;
                actualResources = ImportHelper.importResource(readerContext.getReader(), readerContext.getResource(),
                        currentLocation);
            }

            if (actualResources.isEmpty() && !optional) {
                readerContext.error("Primary location [" + primaryLocation + "]"
                        + (alternateLocation == null ? "" : " and alternate location [" + alternateLocation + "]")
                        + " are not optional", element);
                return null;
            }

            Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
            readerContext.fireImportProcessed(primaryLocation, actResArray, readerContext.extractSource(element));
        } catch (BeanDefinitionStoreException ex) {
            readerContext.error("Failed to import bean definitions from location [" + currentLocation + "]", element,
                    ex);
        }

        return null;
    }

}
