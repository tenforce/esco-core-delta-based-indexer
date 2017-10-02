package com.tenforce.esco.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.List;
import java.util.Map;

@SolrDocument
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {
    final public static String CONTEXT = "conceptSchemes";

    @Id
    @Field("uri")
    @Indexed(name = "uri", type = "string", searchable = true)
    private String uri;

    @Field
    private String uuid;

    @Field
    private Float score;

    @Field
    @Indexed(name = "conceptSchemes", type = "string", searchable = true)
    private List<String> conceptSchemes;

    @Field("preferredLabel_*")
    private Map<String, String> preferredLabel;

    @Field("labels_*")
    private Map<String, List<String>> labels;

    @Field("text")
    private String text;

    @Field
    private Boolean isMappable;

    @Field
    private String skillType;


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getConceptSchemes() {
        return conceptSchemes;
    }

    public void setConceptSchemes(List<String> conceptSchemes) {
        this.conceptSchemes = conceptSchemes;
    }

    public Map<String, String> getPreferredLabel() {
        return preferredLabel;
    }

    public void setPreferredLabel(Map<String, String> preferredLabel) {
        this.preferredLabel = preferredLabel;
    }

    public Map<String, List<String>> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, List<String>> labels) {
        this.labels = labels;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }


    public Boolean getMappable() {
        return isMappable;
    }

    public void setMappable(Boolean mappable) {
        isMappable = mappable;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    /**
     * Helpers
     */
    public String getPreferredLabel(String locale){
        return getPreferredLabel().get(getPreferredLabelField(locale));
    }


    public static String getPreferredLabelField(String locale) {
        return "preferredLabel_"+locale;
    }

    public static String getLabelsField(String locale) {
        return "labels_"+locale;
    }

    public static String getTextField() {
        return "text";
    }

    public static String getSearchTextField() {
        return "search_text";
    }

    public static String getSearchTitleField(String locale) {
        return "search_title_"+locale;
    }

    public static String getSearchLabelsField(String locale) {
        return "search_labels_"+locale;
    }

    public static String getNGramSearchLabelsField(String locale) {
        return "ngram_search_labels_"+locale;
    }


    @Override
    public String toString() {
        return "Item{" +
                "uri='" + uri + '\'' +
                ", uuid='" + uuid + '\'' +
                ", score=" + score +
                ", conceptSchemes=" + conceptSchemes +
                ", preferredLabel=" + preferredLabel +
                ", labels=" + labels +
                ", text=" + text +
                '}';
    }
}
