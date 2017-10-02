package com.tenforce.esco.model;

/**
 *
 We dont' count yet with the concept of dataset ( context in the literature ), so in the meantime we are obliged to
 mae use of the conceptschemes to manage our data, the complexity is that a concept can belong to many conceptschemes.
 (in skos, many resources can belong to a concept scheme, which acts as a particular view on the data, particularly a taxonomy)

 *
 * A concept scheme counts with a set of triples (numberOfTriplesPointingToConceptScheme)
 * from with we can build documents (multilingualDocuments). We can only index documents, not triples.
 */
public class ConceptScheme {

    private String uuid;
    private String url;

    private Long numberOfTriplesPointingToConceptScheme;
    private Long multilingualVariants;
    private Long conceptsIndexed;

    public String getUuid() {
        return uuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getNumberOfTriplesPointingToConceptScheme() {
        return numberOfTriplesPointingToConceptScheme;
    }

    public void setNumberOfTriplesPointingToConceptScheme(Long numberOfTriplesPointingToConceptScheme) {
        this.numberOfTriplesPointingToConceptScheme = numberOfTriplesPointingToConceptScheme;
    }

    public Long getMultilingualVariants() {
        return multilingualVariants;
    }

    public void setMultilingualVariants(Long multilingualVariants) {
        this.multilingualVariants = multilingualVariants;
    }

    public Long getConceptsIndexed() {
        return conceptsIndexed;
    }

    public void setConceptsIndexed(Long conceptsIndexed) {
        this.conceptsIndexed = conceptsIndexed;
    }
}
