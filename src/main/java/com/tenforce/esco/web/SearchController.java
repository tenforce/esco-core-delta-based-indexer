package com.tenforce.esco.web;

import com.tenforce.Application;
import com.tenforce.esco.config.LanguageConfig;
import com.tenforce.esco.model.Item;
import com.tenforce.esco.model.MappingTarget;
import com.tenforce.esco.model.SearchFilter;
import com.tenforce.esco.model.jsonapi.ResponseWrapper;
import com.tenforce.esco.service.MappingService;
import com.tenforce.esco.service.SolrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tenforce.esco.web.serializer.JsonAPISerializer.toJsonAPI;


@Controller
@RequestMapping("/search")
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class SearchController {

    @Autowired
    private LanguageConfig languageConfig;


    @Autowired
    private SolrService solrService;

    @Autowired
    private MappingService mappingService;

    @Value("${solr.results.default}")
    private Integer defaultResultsNumber;

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);


    /**
     * JsonAPI endpoints
     */


    /**
     * Show details of a concept given by its UUID.
     * @throws Exception
     */
    @RequestMapping(path = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity getByUUID(
            @RequestParam(value = "uuid") String uuid
    ) throws Exception {
        return wrapOk(solrService.getByUUID(uuid));
    }

    /**
     * Do a text search for concepts in a certain concept scheme.
     * "include" and "exclude" filter fields can be repeated to drill down the search.
     * If the client API doesn't support this it's possible to use:
     * "exclude=field:a+field:b" or "exclude=field:a field:b".
     *
     * @param conceptScheme The concept scheme to search in
     * @param text The text to search for
     * @param locale Language locale of the search term
     * @param numberOfResults Limit the number of results
     * @param include Only include results based on these filters. "field:value"
     * @param exclude Exclude results based on these filters. "field:value"
     * @return A list of possible matches
     * @throws Exception
     */
    @RequestMapping(path = "/textSearch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<ResponseWrapper> textSearch(
            @RequestParam(value = "conceptScheme") String conceptScheme,
            @RequestParam(value = "text") String text,
            @RequestParam(value = "locale") String locale,
            @RequestParam(value = "numberOfResults", required = false) Integer numberOfResults,
            @RequestParam(value = "include", required = false) ArrayList<String> include,
            @RequestParam(value = "exclude", required = false) ArrayList<String> exclude
            ) throws Exception {
        numberOfResults = numberOfResults == null ? defaultResultsNumber : numberOfResults;

        SearchFilter filter = new SearchFilter(include, exclude);

        return wrapOk(solrService.doBoostedQuery(conceptScheme, locale, text, numberOfResults, filter));
    }


    /**
     * Find concepts of targetConceptScheme similar to a given concept. The concept is given by its UUID.
     * The result is based on a textual search.
     * TODO Make locale agnostic?
     *
     * @param targetConceptScheme The concept scheme to search in
     * @param sourceConceptUUID The text to search for
     * @param locale Language locale of the search term
     * @param numberOfResults Limit the number of results
     * @return A list of possible matches
     * @throws Exception
     */
    @RequestMapping(path = "/similar/text", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<ResponseWrapper> similarText(
            @RequestParam(value = "targetConceptScheme") String targetConceptScheme,
            @RequestParam(value = "sourceConceptUUID") String sourceConceptUUID,
            @RequestParam(value = "locale") String locale,
            @RequestParam(value = "numberOfResults", required = false) Integer numberOfResults
    ) throws Exception {
        numberOfResults = numberOfResults == null ? defaultResultsNumber : numberOfResults;

//        Item source = solrService.getItemByUUID(sourceConceptUUID);
//        String sourceText = source.getPreferredLabel(locale);
//        return wrapOk(solrService.doBoostedQueryMappable(targetConceptScheme, locale, sourceText, numberOfResults));

        return wrapOk(solrService.moreLikeThisMappable(targetConceptScheme,locale, sourceConceptUUID,numberOfResults));
    }

    /**
     * Find concepts of targetConceptScheme similar to a given concept of sourceConceptScheme. The concept is given by its UUID.
     * The result is based on a combination of all similarity measures.
     *
     * TODO Make locale agnostic. A full search should search with all available information. Possibly keep this parameter to indicate the locale of the source to improve results, but not as a restriction.
     */
    @RequestMapping(value="/similar/full")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<ResponseWrapper> similarFull(
            @RequestParam(value = "sourceConceptUUID") String sourceConceptUUID,
            @RequestParam(value = "sourceConceptScheme") String sourceConceptScheme,
            @RequestParam(value = "targetConceptScheme") String targetConceptScheme,
            @RequestParam(value = "locale") String sourceLocale
    ) throws Exception {
        Item source = solrService.getItemByUUID(sourceConceptUUID);
        String sourceText = source.getPreferredLabel(sourceLocale);
        String sourceURI = source.getUri();

        Map<String,MappingTarget> mappings;
        Map<String,Map<String,MappingTarget>> allStructuralMappings = mappingService.getAllMappingTargets(sourceConceptScheme,targetConceptScheme,sourceLocale);
        mappings = allStructuralMappings.get(sourceURI);
        if (mappings == null){
            logger.debug("No structural matching results.");
            mappings = new HashMap<>();
        } else {
            logger.debug("{} structural matching results.", mappings.size());
        }

        // Add Lexical mappings
//        List<Item> lexical = solrService.doBoostedQueryMappable(targetConceptScheme, sourceLocale, sourceText, defaultResultsNumber);
        List<Item> lexical = solrService.moreLikeThisMappable(targetConceptScheme,sourceLocale, sourceConceptUUID,defaultResultsNumber);
        for (Item current:lexical){
            String currentTarget = current.getUri();
            MappingTarget mappingTarget = new MappingTarget();

            mappingTarget.setSourceURI(sourceURI);
            mappingTarget.setTargetURI(currentTarget);

            if (mappings.containsKey(currentTarget)){
                MappingTarget currentMapping = mappings.get(currentTarget);
                currentMapping.setTargetUUID(current.getUuid());
                currentMapping.setTextualScore((double)current.getScore());
                currentMapping.setLabel(current.getPreferredLabel(sourceLocale));
                currentMapping.setTargetURI(current.getUri());
                mappings.put(currentTarget,currentMapping);
            } else {
                mappingTarget.setTargetUUID(current.getUuid());
                mappingTarget.setTextualScore((double)current.getScore());
                mappingTarget.setLabel(current.getPreferredLabel(sourceLocale));
                mappingTarget.setTargetURI(current.getUri());
                mappings.put(currentTarget,mappingTarget);
            }
        }

        return wrap(toJsonAPI(mappings.values()));
    }

    private ResponseEntity<ResponseWrapper> wrapOk(List<Item> response) {
        return wrap(toJsonAPI(response,languageConfig.getIndexableLanguages()));
    }

    private static ResponseEntity<ResponseWrapper> wrap(List<Map<String, Object>> payload) {
        ResponseWrapper rw = new ResponseWrapper();
        rw.setResponse(payload);
        return new ResponseEntity<>(rw, HttpStatus.OK);
    }

}