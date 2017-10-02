package com.tenforce;

import com.tenforce.mu_semtech.db_support.DbConfig;
import com.tenforce.mu_semtech.db_support.DbSupport;
import org.apache.commons.lang3.StringUtils;
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
    public Repository getSparqlRepository(DbConfig dbConfig) throws RepositoryException {
        if (dbConfig == null){
            return new SPARQLRepository(virtuosoEndpoint);
        }
//      Uses out European commission Config.
        if (StringUtils.isEmpty(dbConfig.getId())) {
            return new SPARQLRepository(dbConfig.getUrl());
        }
        else {
            HTTPRepository repo = new HTTPRepository(dbConfig.getUrl(), dbConfig.getId());
            repo.setUsernameAndPassword(dbConfig.getUser(),dbConfig.getPassword());
            return repo;
        }
    }

    @Bean
    public DbConfig getDBConfig() {
      try {
        return new DbSupport().getDbConfig("mu_semtech_id");
      }
      catch (RuntimeException ex){
        return null;
      }
    }

}
