package com.tenforce.esco.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.Collection;

/**
 * Specialized class for SPARQL templates
 */
@Component
public class SparqlTemplates {

    @Value("${sparql.applicationGraph}")
    private String applicationGraph;

    @Autowired
    private VelocityEngine velocityEngine;

    private Template listAllConceptSchemes;
    private Template listDocuments;
    private Template detectDocumentUrisToReindex;
    private Template listUrisConceptSchemeUUID;
    private Template listUrisConceptSchemeURI;
    private Template listDocumentsForFlatteningByUUID;
    private Template listRelationships;
    private Template listMappingByUUID;

    @PostConstruct
    public void setTemplates() {
        this.velocityEngine.init();
        this.listAllConceptSchemes = this.velocityEngine.getTemplate("listAllConceptSchemes.sparql");
        this.listDocuments = this.velocityEngine.getTemplate("listDocuments.sparql");
        this.detectDocumentUrisToReindex = this.velocityEngine.getTemplate("detectDocumentUrisToReindex.sparql");
        this.listUrisConceptSchemeUUID = this.velocityEngine.getTemplate("listUrisConceptSchemeUUID.sparql");
        this.listUrisConceptSchemeURI = this.velocityEngine.getTemplate("listUrisConceptSchemeURI.sparql");

        /**
         * Flattener
         */
        this.listDocumentsForFlatteningByUUID = this.velocityEngine.getTemplate("listDocumentsForFlatteningByUUID.sparql");
        this.listRelationships = this.velocityEngine.getTemplate("listRelationships.sparql");
        this.listMappingByUUID = this.velocityEngine.getTemplate("listMappingsByUUID.sparql");

    }

    /**
     * common
     */
    private String fillTemplate(Template template, Object... parameters){
        VelocityContext context = new VelocityContext();
        for (int i=0;i<parameters.length;i++){
            context.put("_"+i,parameters[i]);
        }
        StringWriter stringWriter = new StringWriter();
        template.merge( context, stringWriter );
        return stringWriter.toString();
    }
    /**
     * Used for flattening.
     */
    public String getFlatTaxonomy(String conceptSchemeURI, String locale){
        if (StringUtils.isBlank(locale) ){
            throw new InvalidParameterException("No locale given.");
        }
        return fillTemplate(listDocumentsForFlatteningByUUID,applicationGraph,conceptSchemeURI,locale);
    }


    /**
     * Used for structural matching.
     */
    public String getListRelationshipsSparql(String sourceConceptSchemeURI, String targetConceptSchemeURI){
        return fillTemplate(listRelationships,applicationGraph,sourceConceptSchemeURI,targetConceptSchemeURI);
    }


    public String getListMappingsByUUIDSparql(String sourceConceptSchemeUUID, String targetConceptSchemeUUID){
        return fillTemplate(listMappingByUUID,applicationGraph,sourceConceptSchemeUUID,targetConceptSchemeUUID);
    }

    /**
     * Gets data of a list of items (by uri), in a specific locale.
     */
    public String getListDocumentsSparql(Collection<String> urisToRetrieve,String locale){
        if (CollectionUtils.isEmpty(urisToRetrieve)){
            throw new InvalidParameterException("The list of uris to retrieve can't be empty.");
        }
        if (StringUtils.isBlank(locale) ){
            throw new InvalidParameterException("No locale given.");
        }
        String list = "<"+StringUtils.join(urisToRetrieve, ">,<")+">";
        return fillTemplate(listDocuments,applicationGraph,list,locale);
    }

    /**
     * Gets all concept schemes
     */
    public String getListAllConceptSchemesSparql(){
        return fillTemplate(listAllConceptSchemes,applicationGraph);
    }

    /**
     * Gets all items belonging to a concept scheme, specified by its UUID
     */
    public String getListUrisConceptSchemeUUIDSparql(String conceptScheme){
        if (StringUtils.isBlank(conceptScheme)){
            throw new InvalidParameterException("No conceptScheme given.");
        }
        return fillTemplate(listUrisConceptSchemeUUID,applicationGraph,conceptScheme);
    }

    /**
     * Gets all items belonging to a concept scheme, specified by its URI
     */
    public String getListUrisConceptSchemeURISparql(String conceptScheme){
        if (StringUtils.isBlank(conceptScheme)){
            throw new InvalidParameterException("No conceptScheme given.");
        }
        return fillTemplate(listUrisConceptSchemeURI,applicationGraph,conceptScheme);
    }

    /**
     * Detects which documents need to be reindexed.
     */
    public String getDetectDocumentUrisToReindexSparql(Collection<String> incomingUri){
        if (CollectionUtils.isEmpty(incomingUri)){
            throw new InvalidParameterException("The list of uris to retrieve can't be empty.");
        }
        String list = "<"+StringUtils.join(incomingUri, ">,<")+">";
        return fillTemplate(detectDocumentUrisToReindex,applicationGraph,list);
    }
}
