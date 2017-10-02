package com.tenforce.esco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix="sparql") // Reads all that has prefix sparql from the application.yaml file.
public class LanguageConfig {

    private List<String> indexableLanguages;

    private String defaultLanguage;

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public List<String> getIndexableLanguages() {
        return indexableLanguages;
    }

    public void setIndexableLanguages(List<String> indexableLanguages) {
        this.indexableLanguages = indexableLanguages;
    }
}
