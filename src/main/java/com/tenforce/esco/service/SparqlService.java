package com.tenforce.esco.service;

import com.tenforce.esco.config.LanguageConfig;
import com.tenforce.esco.config.SparqlTemplates;
import com.tenforce.esco.exception.DataConsistencyError;
import com.tenforce.esco.model.ConceptScheme;
import com.tenforce.esco.model.Item;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static org.openrdf.query.QueryLanguage.SPARQL;

@Component
public class SparqlService {

    /**
     * I/O Operations Sparql
     */
    private static final Logger logger = LoggerFactory.getLogger(SparqlService.class);

    @Autowired
    protected Repository sparqlRepository;

    @Autowired
    protected SparqlTemplates templates;

    @Autowired
    private LanguageConfig languageConfig;

    @org.springframework.beans.factory.annotation.Value("${sparql.applicationGraph}")
    private String applicationGraph;

    @PostConstruct
    public void initializeSparqlRepository() throws RepositoryException {
        this.sparqlRepository.initialize();
    }

    /**
     * Crud Operations on Documents
     */
    public Collection<Item> getItemsByUris(Collection<String> urisToRetrieve) throws Exception {
        return getItems(urisToRetrieve,languageConfig.getIndexableLanguages());
    }

    public Vector<String> getDetectedDocumentUrisToReindex(Collection<String> uris) throws Exception {
        return getVector(templates.getDetectDocumentUrisToReindexSparql(uris),"uri");
    }

    public Vector<String> getAllUrisByConceptSchemeUUID(String conceptSchemeUUID) throws Exception {
        return getVector(templates.getListUrisConceptSchemeUUIDSparql(conceptSchemeUUID),"uri");
    }

    public Vector<String> getAllUrisByConceptSchemeURI(String conceptSchemeURI) throws Exception {
        return getVector(templates.getListUrisConceptSchemeURISparql(conceptSchemeURI),"uri");
    }

    /**
     * Gets indexed ConceptSchemes
     */
    public List<ConceptScheme> getAllConceptSchemes() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String sparqlQuery = templates.getListAllConceptSchemesSparql();
        List<ConceptScheme> results = new ArrayList<>();
        RepositoryConnection conn = sparqlRepository.getConnection();
        try {
            TupleQueryResult result = performQuery(conn, sparqlQuery);
            for (BindingSet bs : QueryResults.asList(result)) {
                ConceptScheme current = new ConceptScheme();
                current.setUuid(mandatoryGet(bs,"conceptScheme"));
                current.setUrl(optionalGet(bs,"conceptSchemeUri"));

                if (bs.getValue("indexableDocumentsCount")!=null){
                    current.setMultilingualVariants(Long.valueOf(bs.getValue("indexableDocumentsCount").stringValue()));
                }
                if (bs.getValue("tripleCount")!=null){
                    current.setNumberOfTriplesPointingToConceptScheme(Long.valueOf(bs.getValue("tripleCount").stringValue()));
                }
                results.add(current);
            }
        } finally {
            conn.close();
        }
        return results;
    }

    /**
     * Triplestore sparql support
     */
    private Vector<String> getVector(String sparqlQuery, String variableName) throws RepositoryException, QueryEvaluationException, MalformedQueryException {
            Vector<String> results = new Vector<>();
            RepositoryConnection conn = sparqlRepository.getConnection();
            try{
                TupleQueryResult result = performQuery(conn, sparqlQuery);
                for (BindingSet bs : QueryResults.asList(result)) {
                    results.add(mandatoryGet(bs,variableName));
                }
                return results;
            } finally {
                conn.close();
            }
    }

    public List<Map> getAllValues(String sparqlQuery) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<Map> results = new ArrayList<>();
        RepositoryConnection conn = sparqlRepository.getConnection();
        TupleQueryResult result = performQuery(conn, sparqlQuery);
        try {
            for (BindingSet bs : QueryResults.asList(result)) {
                Map<String,String> map = new HashMap<>();
                for (String currentName:bs.getBindingNames()){
                    map.put(currentName,bs.getValue(currentName).stringValue());
                }
                results.add(map);
            }
        } finally {
            conn.close();
        }
        return results;
    }

    /**
     * Used for testing consistency! Do not use.
     */
    protected Integer countBindings(String sparqlQuery) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        Integer results = 0;
        RepositoryConnection conn = sparqlRepository.getConnection();

        try {
            TupleQueryResult result = performQuery(conn, sparqlQuery);
            results = QueryResults.asList(result).size();
        } finally {
            conn.close();
        }
        return results;
    }

    private Collection<Item> getItems(Collection<String> urisToRetrieve, List<String> languagesToInclude) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        /**
         * The strategy is as follows:
         * We get rows for each occurrence in one language for a bunch of concepts.
         * Incrementally we group the descriptions of concepts in all languages using a map by URI.
         *
         * NOTE: This is surprisingly efficient and works well for both Virtuoso and OWLim.
         */
        Map<String,Item> items = new HashMap<>();

        RepositoryConnection conn = sparqlRepository.getConnection();

        for (String lang:languagesToInclude) {
            String query = templates.getListDocumentsSparql(urisToRetrieve,lang);
            TupleQueryResult result = performQuery(conn, query);
            while (result.hasNext()) {
                BindingSet bs = result.next();

                String uri = mandatoryGet(bs, "uri");

                Item item;
                if (!items.containsKey(uri)) {
                    // new item
                    item = new Item();
                    items.put(uri, item);
                    item.setUri(uri);

                    item.setUuid(optionalGet(bs, "uuid"));

                    String isMappable = optionalGet(bs, "isMappable");
                    item.setMappable(isMappable != null && isMappable.equals("true"));

                    String skillType = optionalGet(bs, "skillType");
                    item.setSkillType(skillType);

                    String conceptSchemesString = mandatoryGet(bs, "conceptSchemes");
                    item.setConceptSchemes(Arrays.asList(conceptSchemesString.split(",")));

                    Map<String, String> preferredLabels = new HashMap<>();
                    item.setPreferredLabel(preferredLabels);

                    Map<String, List<String>> labels = new HashMap<>();
                    item.setLabels(labels);
                } else {
                    // existing item
                    item = items.get(uri);
                }

                String preferredLabel = optionalGet(bs, "preferredLabel");
                if (preferredLabel != null) {
                    item.getPreferredLabel().put("preferredLabel_" + lang, preferredLabel);
                }

                String labels = optionalGet(bs, "labels");
                if (labels != null) {
                    item.getLabels().put("labels_" + lang, Arrays.asList(labels.split(",")));
                }

                String text = optionalGet(bs, "text");
                if (text != null && lang.equals("en")) {
                    item.setText(text);
                }
            }
            result.close();
        }
        conn.close();

        return items.values();
    }

    protected static String mandatoryGet(BindingSet bs, String propertyName){
        Value value = bs.getValue(propertyName);
        if (null==value){
            throw new DataConsistencyError("Property " + propertyName + " not found.");
        }
        return value.stringValue();
    }

    protected static String optionalGet(BindingSet bs, String propertyName){
        Value value = bs.getValue(propertyName);
        if (null==value){
            return null;
        }
        return value.stringValue();
    }



    /**
     * Detects a set of all the Uris.
     */
    protected void addInputStreamToTripleStore(Reader is, RDFFormat rdfFormat) throws RepositoryException, RDFParseException, IOException {
        RepositoryConnection conn = sparqlRepository.getConnection();
        try {
            Resource graphResource = conn.getRepository().getValueFactory().createURI(applicationGraph);
            conn.add(is,applicationGraph,rdfFormat,graphResource);
        } finally {
            conn.close();
        }
    }

    protected TupleQueryResult performQuery(RepositoryConnection conn, String currentSparqlQuery) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        TupleQuery query = conn.prepareTupleQuery(SPARQL, currentSparqlQuery);
        logger.debug("running query: "+ currentSparqlQuery);
        TupleQueryResult result = query.evaluate();
        logger.debug("finished running query");
        return result;
    }

    protected void executeUpdate(String sparqlQuery) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        RepositoryConnection conn = sparqlRepository.getConnection();
        try {
            logger.debug("performing update query: "+ sparqlQuery);
            Update update = conn.prepareUpdate(SPARQL, sparqlQuery);
            update.execute();
            logger.debug("performed update");
        } finally {
            conn.close();
        }
    }
}
