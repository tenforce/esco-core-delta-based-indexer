package com.tenforce;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.hateoas.RelProvider;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Arrays;

@Configuration
@EnableWebMvc
@EnablePluginRegistries(RelProvider.class)
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class Config extends WebMvcConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Value("${sparql.virtuoso.endpoint}")
    public String virtuosoEndpoint;

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterBean = new FilterRegistrationBean();
        filterBean.setFilter(new ShallowEtagHeaderFilter());
        filterBean.setUrlPatterns(Arrays.asList("*"));
        return filterBean;
    }

    @Bean
    public Repository getSparqlRepository() throws RepositoryException {
        String sparqlEndpoint = System.getenv("SPARQL_ENDPOINT");
        if (StringUtils.isNotBlank(sparqlEndpoint)) {
          log.info("using sparql endpoint {} from environment", sparqlEndpoint);
					return new SPARQLRepository(sparqlEndpoint);
				}
				else
  				return new SPARQLRepository(virtuosoEndpoint);
    }

}
