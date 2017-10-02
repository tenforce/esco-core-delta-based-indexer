package com.tenforce.esco.service;

import com.tenforce.Application;
import com.tenforce.esco.exception.InvalidParameterException;
import com.tenforce.esco.model.Item;
import com.tenforce.esco.model.ItemRepository;
import com.tenforce.esco.model.SearchFilter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class SolrService {

/**

 To do searches with SOLR:

    // Search a text:
    //    fq=conceptSchemes:xyxyxyxyxyxyx&sort=score+desc&fl=*,score&q=search_en:*TheTextToSearchFor*

    // Search a title starting with
    //    fq=conceptSchemes:xyxyxyxyxyxyx&sort=score+desc&fl=*,score&q=preferredLabel_en:*TheTextToSearchFor

**/

    /**
     * I/O operations with SOLR
     */
    private static final Logger logger = LoggerFactory.getLogger(SolrService.class);

    /**
     * TODO: go to the less verbose and paginable: http://projects.spring.io/spring-data-solr/
     */

    @Autowired
    private SolrServer solrServer;

    @Autowired
    private ItemRepository itemRepository;


    /**
     * Returns a list of all the distinct conceptSchemes
     */
    public Map<String,Long> getIndexedConceptSchemes() throws IOException, SolrServerException {
        String facetFieldName = Item.CONTEXT;
        SolrQuery query = new SolrQuery( "*:*" );
        query.setFacet(true);
        query.addFacetField(facetFieldName);
        query.setFacetLimit(-1);
        query.setFacetMinCount(1);
        query.setFacetSort("count");

        QueryResponse rsp = solrServer.query( query );

        logger.debug("getIndexedConceptSchemes {}",rsp.toString());

        FacetField facetField = rsp.getFacetField(facetFieldName);
        Map<String,Long> result = new HashMap<>();
        for (FacetField.Count count :facetField.getValues()){
            result.put(count.getName(),count.getCount());
        }
        return result;
    }

    /**
     * Index items
     */
    public Integer addToIndex(Collection<Item> itemsToIndex) {
        logger.debug("Flushing and indexing");
        itemRepository.save(itemsToIndex);
        Integer count = itemsToIndex.size();
        logger.debug("{} elements indexed", count);
        return count;
    }

    /**
     * Remove items from index
     */
    public void removeURIsFromIndex(List<String> uris)  {
        List<Item> toDelete = new ArrayList<>();
        for (String currentUri:uris){
            Item current = new Item();
            current.setUri(currentUri);
            toDelete.add(current);
        }
        itemRepository.delete(toDelete);
    }

    /**
     * Returns one document by uri
     */
    public Item getByUri(String uri) {
        return itemRepository.findOne(uri);
    }

    public List<Item> getByUUID(String uuid) throws SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery("uuid:"+ uuid);
        logger.debug("Solr query: {}",query.toString());
        QueryResponse rsp = solrServer.query( query );
        return rsp.getBeans(Item.class);
    }

    @Value("${solr.title.boost}")
    private Double searchTitleBoost;
    @Value("${solr.labels.boost}")
    private Double searchLabelsBoost;
    @Value("${solr.ngramlabels.boost}")
    private Double ngramSearchLabelsBoost;
    @Value("${solr.text.boost}")
    private Double searchTextBoost;
    /**
     * Do a solr search for text in both the job title and description.
     */
    public List<Item> doBoostedQueryMappable(String conceptScheme, String locale, String text, Integer numberOfResults) throws SolrServerException {
        SearchFilter filter = new SearchFilter();
        filter.addInclude("isMappable:true");
        return doBoostedQuery(conceptScheme, locale, text, numberOfResults, filter);
    }

    /**
     * Do a solr similarity search (mlt) for text in both the job title and description.
     * Only returns mappable items.
     */
    public List<Item> moreLikeThisMappable(String conceptScheme, String locale, String uuid, Integer numberOfResults) throws SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setFields("*,score");
        query.setRows(numberOfResults);
        query.setQuery("uuid:" + ClientUtils.escapeQueryChars(uuid));

        SearchFilter filter = new SearchFilter();
        filter.addInclude("isMappable:true");
        filter.addInclude(Item.CONTEXT +":"+ ClientUtils.escapeQueryChars(conceptScheme));
        filter.applyFilter(query);

        query.setRequestHandler("/" + MoreLikeThisParams.MLT);
        query.set(MoreLikeThisParams.MAX_DOC_FREQ, 500); // Ignore words that occur too often.
        query.set(MoreLikeThisParams.SIMILARITY_FIELDS,
                Item.getSearchTextField(),
                Item.getSearchTitleField(locale),
                Item.getLabelsField(locale),
                Item.getNGramSearchLabelsField(locale));
        query.set(MoreLikeThisParams.BOOST, true);

        logger.debug("Solr query: {}",query.toString());
        QueryResponse rsp = solrServer.query( query );
        return rsp.getBeans(Item.class);
    }



    /**
     * Do a solr search for text in both the job title and description.
     */
    public List<Item> doBoostedQuery(String conceptScheme, String locale, String text, Integer numberOfResults, SearchFilter searchFilter) throws SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setFilterQueries(Item.CONTEXT +":"+ ClientUtils.escapeQueryChars(conceptScheme));
        query.setFields("*,score");
        query.setRows(numberOfResults);
        query.setQuery("*:"+ escapeUserQueryChars(text));
        query.setParam("defType","dismax");

        searchFilter.applyFilter(query);

        query.setParam("qf"
                , Item.getSearchTitleField(locale)+"^"+searchTitleBoost
                , Item.getSearchLabelsField(locale)+"^"+searchLabelsBoost
                , Item.getNGramSearchLabelsField(locale)+"^"+ngramSearchLabelsBoost
//                , Item.getSearchTextField()+"^"+searchTextBoost
        );
        logger.debug("Solr query: {}",query.toString());
        QueryResponse rsp = solrServer.query( query );
        return rsp.getBeans(Item.class);
    }

    public Item getItemByUUID(String conceptUUID) {
        List<Item> item = itemRepository.findByUuid(conceptUUID);
        if (item.isEmpty()){
            throw new InvalidParameterException("UUID "+conceptUUID+" not found");
        }
        return item.iterator().next();
    }

    public void clearIndex() {
        itemRepository.deleteAll();
    }

    // Taken from ClientUtils.escapeQueryChars, but omitting the \" char, used for the special case
    private static String escapeUserQueryChars(String s) {
        if (null==s) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'
                    || c == '^' || c == '[' || c == ']' || c == '{' || c == '}' || c == '~'
                    || c == '*' || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'
                    || Character.isWhitespace(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

}
