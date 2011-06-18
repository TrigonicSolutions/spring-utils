# Spring Framework Utilities

This project intends to collect frequently requested utilities for and extensions to the Spring Framework.

## Gradle Usage

    repositories {
        add(new org.apache.ivy.plugins.resolver.URLResolver()) {
            name = 'GitHub Spring Utils Repo'
            addArtifactPattern 'http://cloud.github.com/downloads/AlanKrueger/spring-utils/[module]-[revision].[ext]'
        }
        mavenCentral()
    }

    dependencies {
        classpath 'com.trigonic:spring-utils:0.5'
    }


## Spring XML Usage

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:util="http://www.springframework.org/schema/util"
        xmlns:xutils="http://trigonic.com/schema/spring/xutils"
        xsi:schemaLocation="
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd        
            http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd
            http://trigonic.com/schema/spring/xutils http://trigonic.com/schema/spring/xutils.xsd">

        ...
    
        <!-- importing an optional resource -->
        <xutils:import resource="foo.xml" optional="true" />    
    
        <!-- importing a resource with an alternate if it doesn't exist -->
        <xutils:import resource="foo.xml" alternate="bar.xml" />    
    
        <!-- the same where both are optional -->
        <xutils:import resource="foo.xml" alternate="bar.xml" optional="true" />    

        ...
        
    </beans>

## Embedded Web Context Usage

The EmbeddedWebContextConnector provides a connection between a running application's ApplicationContext and an embedded
WebApplicationContext.  This is useful when running Jetty and Tomcat in embedded mode.

In the hosting application, add a EmbeddedWebContextRegistrar to the ApplicationContext (or manually call
EmbeddedWebContextConnector.registerAppContext and EmbeddedWebContextConnector.unregisterAppContext) to register
the ApplicationContext you want to use as the parent.

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:util="http://www.springframework.org/schema/util"
        xmlns:xutils="http://trigonic.com/schema/spring/xutils"
        xsi:schemaLocation="
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
            http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd
            http://trigonic.com/schema/spring/xutils http://trigonic.com/schema/spring/xutils.xsd">

        ...

        <bean class="com.trigonic.utils.spring.context.EmbeddedWebContextRegistrar">
            <property name="embeddedWebContext" value="foobar" />
        </bean>

        ...

    </beans>

In the web application's web.xml, add EmbeddedWebContextConnector as a listener and set the embeddedWebContext context-param
to the name registered in the outer application context:

    <context-param>
        <param-name>embeddedWebContext</param-name>
        <param-value>foobar</param-value>
    </context-param>

    <listener>
        <listener-class>com.trigonic.utils.spring.context.EmbeddedWebContextConnector</listener-class>
    </listener>

When the WebApplicationContext initializes, it will have the hosting ApplicationContext as a parent and will be able to leverage
the beans defined there.

