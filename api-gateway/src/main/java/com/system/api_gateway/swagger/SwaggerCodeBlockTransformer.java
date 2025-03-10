package com.system.api_gateway.swagger;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webflux.ui.SwaggerIndexPageTransformer;
import org.springdoc.webflux.ui.SwaggerWelcomeCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.reactive.resource.ResourceTransformerChain;
import org.springframework.web.reactive.resource.TransformedResource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class SwaggerCodeBlockTransformer extends SwaggerIndexPageTransformer {

    @Autowired
    private ResourceLoader resourceLoader;

    public SwaggerCodeBlockTransformer(SwaggerUiConfigProperties swaggerUiConfig,
                                       SwaggerUiOAuthProperties swaggerUiOAuthProperties,
                                       SwaggerWelcomeCommon swaggerWelcomeCommon,
                                       ObjectMapperProvider objectMapperProvider) {

        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public Mono<Resource> transform(ServerWebExchange request,
                                                Resource resource,
                                                ResourceTransformerChain transformer) {

        if (resource.toString().contains("swagger-ui.css")) {

            final InputStream is;
            try {

                is = resource.getInputStream();
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
            final InputStreamReader isr = new InputStreamReader(is);
            try (BufferedReader br = new BufferedReader(isr)) {

                final String customCss = this.appendToCss("classpath:config/swagger-ui.css");
                final String css = br.lines().collect(Collectors.joining()).concat(formatCss(customCss));
                final byte[] transformedContent = css.getBytes();
                return Mono.just(new TransformedResource(resource, transformedContent));
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }

        return super.transform(request, resource, transformer);
    }

    public String appendToCss(String cssFilePath) throws IOException {

        Resource resource = resourceLoader.getResource(cssFilePath);
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    public static String formatCss(String css) {

        return css.replaceAll("\\s*\\n\\s*", "").replaceAll("\\s*\\{\\s*", "{")
                .replaceAll("\\s*}\\s*", "}");
    }
}
