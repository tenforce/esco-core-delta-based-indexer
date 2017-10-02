package com.tenforce.esco.service.asynchronous;


import com.tenforce.Application;
import com.tenforce.esco.model.Item;
import com.tenforce.esco.service.SolrService;
import com.tenforce.esco.service.SparqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class IndexWorker {


    /**
     * NOTE: Asynchronous queues were deactivated and extracted while searching for a performance
     * problem while connecting to owlim, ... queues were not the problem,
     * but not really needed in the indexation process.
     * TODO Reflect this in project structure?
     */


    /**
     * TODO In the future, if we go out the single server scheme, these consumers can be turned
     * into several workers, to be scaled independently to tackle high loads.
     */


    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);

    @Autowired
    private SolrService solrService;

    @Value("${sparql.enableTransitiveChangeDetection}")
    private Boolean enableTransitiveChangeDetection;

    @Autowired
    private SparqlService sparqlService;

    /**
     * Index the concepts given by the URI in one call to sparql, and one call to solr.
     * @param incomingUris
     */
    public void update(List<String> incomingUris) throws Exception {
        List<String> urisToUpdate;

        // When processing a delta, some uris might identify concept fields rather than concepts
        if (enableTransitiveChangeDetection){
            urisToUpdate = sparqlService.getDetectedDocumentUrisToReindex(incomingUris);
        } else {
            urisToUpdate = incomingUris;
        }

        if(urisToUpdate.isEmpty()){
            logger.debug("No indexable uris to update.");
        }else{
            /**
             * Remove existing indexed items, and then reindex them
             */
            Collection<Item> itemsToIndex = sparqlService.getItemsByUris(urisToUpdate);
            logger.info("Got {} items to index.", itemsToIndex.size());
            solrService.removeURIsFromIndex(urisToUpdate);
            solrService.addToIndex(itemsToIndex);
        }
    }
}
