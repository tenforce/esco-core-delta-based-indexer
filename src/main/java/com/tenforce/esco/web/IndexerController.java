package com.tenforce.esco.web;

import com.tenforce.esco.model.ConceptScheme;
import com.tenforce.esco.model.DeltaRecord;
import com.tenforce.esco.service.SolrService;
import com.tenforce.esco.service.SparqlService;
import com.tenforce.esco.service.asynchronous.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Controller
@RequestMapping("/index")
public class IndexerController {

    @Autowired
    private SolrService solrService;

    @Autowired
    private SparqlService sparqlService;

    @Autowired
    private Queues uriBasedQueues;

    @Value("${sparql.applicationGraph}")
    private String applicationGraph;

    private static final Logger logger = LoggerFactory.getLogger(IndexerController.class);



    @RequestMapping(path = "/update/timetest",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResourceSupport timetest() throws Exception {
        solrService.clearIndex();
        long startTime = System.currentTimeMillis();

        this.updateConceptScheme("16130956-533c-11e6-89a4-a439968efbe3",null);

        long endTime = System.currentTimeMillis();

        String response = ("That took " + (endTime - startTime) + " milliseconds");
        return new Resource<>(response);
    }


    /**
     * Consult state of indexing.
     */
    @RequestMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResourceSupport allConceptSchemes() throws Exception {
        List<ConceptScheme> storedConceptSchemes = sparqlService.getAllConceptSchemes();
        Map<String,Long> indexedConceptSchemes = solrService.getIndexedConceptSchemes();

        List<Resource<ConceptScheme>> resources = new ArrayList<>();
        for (ConceptScheme conceptScheme:storedConceptSchemes){
            String uuid = conceptScheme.getUuid();
            Long indexedElementsCount = indexedConceptSchemes.get(uuid);
            if (indexedElementsCount==null){
                indexedElementsCount = (long)0;
            }
            conceptScheme.setConceptsIndexed(indexedElementsCount);
            Resource<ConceptScheme> resource = new Resource<>(conceptScheme);
            resource.add(linkTo(methodOn(IndexerController.class).updateConceptScheme(conceptScheme.getUuid(), null)).withRel("index:updateConceptScheme"));
            resource.add(linkTo(methodOn(IndexerController.class).updateConceptScheme(null, conceptScheme.getUrl())).withRel("index:updateConceptScheme"));

            resources.add(resource);
        }
        logger.debug("Served indexing status.");
        return new Resources<>(resources);
    }

    /**
     * Update the index.
     */

    /**
     * Clear the index and reindex all concept schemes in the triplestore.
     */
    @RequestMapping(path = "/update/all",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResourceSupport updateAll() throws Exception {
        long startTime = System.currentTimeMillis();

        solrService.clearIndex();

        // Find all concept schemes
        List<ConceptScheme> storedConceptSchemes = sparqlService.getAllConceptSchemes();

        // Collect all uris of concepts to index in a set
        Set<String> allUris = new HashSet<>();
        for(ConceptScheme conceptScheme: storedConceptSchemes){
            Vector<String> uris = sparqlService.getAllUrisByConceptSchemeUUID(conceptScheme.getUuid());
            allUris.addAll(uris);
        }

        // Index the concepts
        Map<String, Object> response = uriBasedQueues.enqueueUrisToReindex(new Vector<>(allUris));

        // Get the state of indexing for each scheme
        Map<String,Long> indexedConceptSchemes = solrService.getIndexedConceptSchemes();
        for(ConceptScheme conceptScheme: storedConceptSchemes){
            String uuid = conceptScheme.getUuid();
            Long indexedElementsCount = indexedConceptSchemes.get(uuid);
            indexedElementsCount = indexedElementsCount == null ? 0 : indexedElementsCount;
            conceptScheme.setConceptsIndexed(indexedElementsCount);
        }
        long endTime = System.currentTimeMillis();
        response.put("Time", (endTime - startTime)/60000f + " minutes");
        response.put("ConceptSchemes", storedConceptSchemes);
        return new Resource<>(response);
    }




    /**
     * Clear and reindex a given concept scheme.
     */
    @RequestMapping(path = "/update/conceptScheme",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResourceSupport updateConceptScheme(
            @RequestParam(value = "uuid", required = false) String uuid,
            @RequestParam(value = "uri", required = false) String uri) throws Exception {
        if ((uuid == null) == (uri == null)) { // use only one
            throw new Exception("Set exactly one of the query parameters uuid/uri.");
        }

        if (uuid != null) {
            List<String> urisToProcess = sparqlService.getAllUrisByConceptSchemeUUID(uuid);
            return reindexUris(urisToProcess);
        } else /*uri != null*/ {
            List<String> urisToProcess = sparqlService.getAllUrisByConceptSchemeURI(uri);
            return reindexUris(urisToProcess);
        }
    }


    /**
     * Clear all indexed data from Solr
     */
    @RequestMapping(path = "/clear/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResourceSupport clearIndex() throws Exception {
        solrService.clearIndex();
        return new ResourceSupport();
    }

    /**
     * Update concepts based on a delta record.
     */
    @RequestMapping(path = "/update/delta",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResourceSupport updateDelta(@RequestBody DeltaRecord deltaRecord) throws Exception {
//        logger.debug("Received delta: " + deltaRecord);
        Set<String> uris = deltaRecord.getAffectedURIs(applicationGraph);
        if(uris.isEmpty()){
            logger.debug("Delta ignored. Nothing to reindex.");
            return new ResourceSupport();
        }else{
            logger.debug("Reindexing based on delta: " + uris);
            return reindexUris(new ArrayList<>(uris));
        }
    }

    private ResourceSupport reindexUris(List<String> uris) {
        return new Resource<>(uriBasedQueues.enqueueUrisToReindex(uris));
    }
}
