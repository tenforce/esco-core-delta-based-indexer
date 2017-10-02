package com.tenforce.esco.service;

import com.tenforce.Application;
import com.tenforce.esco.exception.InvalidParameterException;
import com.tenforce.esco.exception.NoDataException;
import com.tenforce.esco.model.Item;
import com.tenforce.esco.model.MappingTarget;
import com.tenforce.flooding.Alignment;
import com.tenforce.flooding.NeighborSimilarityMatcher;
import com.tenforce.flooding.Taxonomy2Match;
import com.tenforce.flooding.URIMap;
import com.tenforce.flooding.enums.EntityType;
import com.tenforce.flooding.structs.Individual;
import com.tenforce.flooding.structs.RelationshipMap;
import org.apache.solr.client.solrj.SolrServerException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class MappingService {

    @Autowired
    private SolrService solrService;

    @Autowired
    private TaxonomyService taxonomyService;

    private static final Logger log = LoggerFactory.getLogger(MappingService.class);

    private URIMap urimap;
    private HashMap<Integer,String> uuidMap;

    private Integer ALTERNATIVE_TEXT;
    private RelationshipMap relationshipMap;


    @Value("${solr.structural.results.default}")
    private Integer defaultStructuralResultsNumber;

    @PostConstruct
    public void init(){
        urimap = new URIMap();
        this.ALTERNATIVE_TEXT = urimap.addURI("http://www.w3.org/2008/05/skos-xl#literalForm",EntityType.ANNOTATION);
        this.uuidMap = new HashMap<>();
        this.relationshipMap = new RelationshipMap();
    }

    // TODO must work with multiple languages. Currently, lang refers to the source's locale.
    public Map<String,Map<String,MappingTarget>> getAllMappingTargets(String sourceUUID, String targetUUID, String lang) throws Exception {
        Alignment alignment = getStructuralAlignment(sourceUUID,targetUUID,lang);
        return alignment.getAllMappingTargets(urimap,uuidMap);
    }

    public Alignment getStructuralAlignment(String sourceUUID, String targetUUID, String lang) throws Exception {

        Collection<TaxonomyService.FloodingItem> sourceItems = taxonomyService.getFloodingItemsByUUID(sourceUUID, lang);
        Taxonomy2Match sourceTaxonomy = getTaxonomy2Match(sourceItems);

        Collection<TaxonomyService.FloodingItem> targetItems = taxonomyService.getFloodingItemsByUUID(targetUUID, lang);
        Taxonomy2Match targetTaxonomy = getTaxonomy2Match(targetItems);

        NeighborSimilarityMatcher matcher = new NeighborSimilarityMatcher(relationshipMap,false);

        Alignment alignment = new Alignment(sourceTaxonomy,targetTaxonomy);

        try{
            addPreviousMappingsAsAlignment(alignment,sourceUUID,targetUUID);
        } catch (NoDataException e){
            log.warn(e.getMessage());
        }

        return matcher.extendAlignment(urimap,alignment,0.8);
    }

    /**
     * Untested
     */
    public void addPreviousMappingsAsAlignment(Alignment alignment, String sourceConceptSchemeUUID,String targetConceptSchemeUUID) throws RepositoryException, MalformedQueryException, QueryEvaluationException, NoDataException {
        int count = 0;
        for (TaxonomyService.RelationItem mapping:taxonomyService.getMappingItems(sourceConceptSchemeUUID,targetConceptSchemeUUID)) {
            int sourceId = toClass(mapping.getSource());
            int targetId = toClass(mapping.getTarget());
            if (sourceId!=targetId){
                alignment.add(sourceId,targetId,1);
                count++;
            }
        }
        if (count==0){
            throw new NoDataException("Could not configure structural matching, no "+sourceConceptSchemeUUID+" -> "+targetConceptSchemeUUID+" mappings found");
        }

        log.info("Added "+count+" mappings as alignment");
    }


    /**
     * Do not use, except for testing.
     * Alignment should be based on real data, not textual similarity.
     * Use addPreviousMappingsAsAlignment() in stead.
     */
    public void addTextualSimilarityAsAlignment(Alignment alignment, Collection<TaxonomyService.FloodingItem> itemsToMatch, String targetSearchContext, long threshold, String lang) throws SolrServerException {
        int count = 0;
        for (TaxonomyService.FloodingItem sourceItem:itemsToMatch) {
            String searchText = sourceItem.getTitle();
            List<Item> matches = solrService.doBoostedQueryMappable(targetSearchContext,lang,searchText , defaultStructuralResultsNumber);
            String sourceURI = sourceItem.getUri();
            int sourceId = toClass(sourceURI);
            for (Item targetItem:matches) {
                String targetURI = targetItem.getUri();
                int targetId = toClass(targetURI);
                if (targetItem.getScore()>threshold){
                    if (sourceId!=targetId){
                        alignment.add(sourceId,targetId,1);
                        count++;
                    }
                }
            }
        }
        if (count==0){
            throw new InvalidParameterException("Could not perform structural matching, no results from lexical matching, are the contents of "+targetSearchContext+" indexed ?");
        }

        log.info("Added "+count+" mappings as alignment");
    }

    /**
     * Getting taxonomies
     */
    private Taxonomy2Match getTaxonomy2Match(Collection<TaxonomyService.FloodingItem> items) throws Exception {
        Taxonomy2Match result = new Taxonomy2Match();
        for (TaxonomyService.FloodingItem currentItem:items) {
            String uri = currentItem.getUri();
            // Pref Label
            String title = currentItem.getTitle();
            if (null==title){
                throw new InvalidParameterException("No pref label");
            }

            Integer id = toClass(uri);
            String broaderURI = currentItem.getBroader();
            if (null!=broaderURI){
                relationshipMap.addDirectSubclass(id,toClass(broaderURI));
            }
            Individual individual = new Individual(id,title);
            // Alt labels
            String text = currentItem.getText();
            if (null!=text){
                individual.addDataValue(ALTERNATIVE_TEXT,text);
            }

            // Add uuids, sad but true.
            String uuid = currentItem.getUuid();
            if (null!=uuid){
                uuidMap.put(id,uuid);
            }

            result.addIndividual(id,individual);
        }
        return result;
    }

    private Integer toClass(String uri){
        return urimap.addURI(uri, EntityType.CLASS);
    }

}
