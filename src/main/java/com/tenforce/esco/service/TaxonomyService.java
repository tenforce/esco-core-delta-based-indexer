package com.tenforce.esco.service;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@CacheConfig(cacheNames = "mappings")
public class TaxonomyService extends SparqlService {


    public class FloodingItem{
        private String uri;
        private String uuid;
        private String title;
        private String text;
        private String broader;

        public FloodingItem(String uri,String title,String text, String broader) {
            this.uri = uri;
            this.text = text;
            this.broader = broader;
            this.title = title;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUri() {
            return uri;
        }

        public String getBroader() {
            return broader;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

    }

    public class RelationItem{
        private String source;
        private String relationshipType;
        private String target;

        public RelationItem(String source, String relationshipType, String target) {
            this.source = source;
            this.relationshipType = relationshipType;
            this.target = target;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    @Cacheable
    public Collection<FloodingItem> getFloodingItemsByUUID(String conceptSchemeUUID, String lang) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<FloodingItem> items = new ArrayList<>();
        RepositoryConnection conn = sparqlRepository.getConnection();
        try {

            String currentSparqlQuery = templates.getFlatTaxonomy(conceptSchemeUUID,lang);
            TupleQueryResult result = performQuery(conn, currentSparqlQuery);

            while(result.hasNext()){
                BindingSet bs = result.next();

                String uri = mandatoryGet(bs, "uri");
                /**
                 * Used mainly for flooding
                 */
                String broader = optionalGet(bs, "broader");
                String title = mandatoryGet(bs, "preferredLabel");
                String text = mandatoryGet(bs, "text");
                FloodingItem currentItem = new FloodingItem(uri,title,text,broader);
                currentItem.setUuid(optionalGet(bs, "uuid"));
                items.add(currentItem);
            }

            result.close();
        } finally {
            conn.close();
        }

        return items;
    }

    public Collection<RelationItem> getMappingItems(String sourceConceptSchemeUUID, String targetConceptSchemeUUID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<RelationItem> items = new ArrayList<>();
        RepositoryConnection conn = sparqlRepository.getConnection();
        try {
            String currentSparqlQuery = templates.getListMappingsByUUIDSparql(sourceConceptSchemeUUID, targetConceptSchemeUUID);
            TupleQueryResult result = performQuery(conn, currentSparqlQuery);
            while(result.hasNext()){
                BindingSet bs = result.next();
                items.add(new RelationItem(mandatoryGet(bs, "sourceURI"),mandatoryGet(bs, "matchType"),mandatoryGet(bs, "targetURI")));
            }
            result.close();
        } finally {
            conn.close();
        }
        return items;
    }


    public Collection<RelationItem> getRelationItems(String sourceConceptSchemeURI, String targetConceptSchemeURI) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<RelationItem> items = new ArrayList<>();
        RepositoryConnection conn = sparqlRepository.getConnection();
        try {
            String currentSparqlQuery = templates.getListRelationshipsSparql(sourceConceptSchemeURI, targetConceptSchemeURI);
            TupleQueryResult result = performQuery(conn, currentSparqlQuery);
            while(result.hasNext()){
                BindingSet bs = result.next();
                items.add(new RelationItem(mandatoryGet(bs, "sourceURI"),
                                            mandatoryGet(bs, "relationShipType"),
                                            mandatoryGet(bs, "targetUri")));
            }
            result.close();
        } finally {
            conn.close();
        }
        return items;
    }

//    public RDFGraph getRelationsBetweenTaxonomiesGraph(String sourceConceptSchemeURI, String targetConceptSchemeURI) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
//        Collection<RelationItem> relationItems = getRelationItems(sourceConceptSchemeURI, targetConceptSchemeURI);
//        RDFGraph rdfWriter = RDFGraph.getNew();
//
//        for (TaxonomyService.RelationItem currentItem:relationItems) {
//            rdfWriter.addRelation(
//                    currentItem.getSource(),
//                    currentItem.getRelationshipType(),
//                    currentItem.getTarget()
//            );
//        }
//        return rdfWriter;
//    }


}
