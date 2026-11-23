package com.gmao.itm.library_api.config;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatLocaleCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> connector.setMaxLocaleCount(50));
    }
}