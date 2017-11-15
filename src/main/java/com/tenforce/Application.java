package com.tenforce;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.WebApplicationInitializer;

@SpringBootApplication
@EnableCaching
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  static {
    try {
  		String solrUrl = System.getenv("SOLR_REPOSITORY_URL");
      if (StringUtils.isNotBlank(solrUrl)) {
        System.setProperty("spring.data.solr.host", solrUrl);
        log.info("Set solr host property to {} from environment", solrUrl);
      }
    }
    catch (RuntimeException e) {
      log.warn("No solr_home set from environment");
    }
  }

  public static final String CONFIG_PLACEHOLDER = "classpath:/application.properties";

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
