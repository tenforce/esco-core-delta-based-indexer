package com.tenforce.esco.service.asynchronous;


import com.google.common.collect.Lists;
import com.tenforce.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Utility classes to manage queues.
 */
@Component
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class Queues {

    @Autowired
    private IndexWorker indexWorker;

    @Value("${queues.partitionSize}")
    private Integer defaultChunkSize;

    @Value("${queues.enable}")
    private Boolean enableQueues;


    private static final Logger logger = LoggerFactory.getLogger(Queues.class);


    /**
     * Enqueue concepts to be reindexed.
     * @param uris URIs of the concepts
     * @param chunkSize Size of chunks in which URIs are sent to solr
     * @return A report response. Might contain useful information if something went wrong with indexing.
     */
    public Map<String,Object> enqueueUrisToReindex(List<String> uris, Integer chunkSize) {
        Map<String,Object> response = new HashMap<>();
        List<String> errors = new Vector<>();

        if(enableQueues){
            List<List<String>> chunks = Lists.partition(uris, chunkSize);
            response.put("message",String.format("Indexing [%s] uris using [%s] chunks.",uris.size(), chunks.size()));
            for (List<String> chunk: chunks){
                try {
                    indexWorker.update(chunk);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    errors.add("error: "+e.getMessage());
                }
            }
        }
        else{
            response.put("message",String.format("Indexing [%s] uris.",uris.size()));
            try {
                indexWorker.update(uris);
            } catch (Exception e) {
                logger.error(e.getMessage());
                errors.add("error: "+e.getMessage());
            }
        }

        if(!CollectionUtils.isEmpty(errors)){
            response.put("errors",errors);
        }

        return response;
    }

    /**
     * Enqueue concepts to be reindexed. The URIs are sent to solr in chunks. Default chunk size can be set in application.properties.
     * @param uris URIs of the concepts
     * @return A report response. Might contain useful information if something went wrong with indexing.
     */
    public Map<String,Object> enqueueUrisToReindex(List<String> uris) {
        return enqueueUrisToReindex(uris,defaultChunkSize);
    }

}