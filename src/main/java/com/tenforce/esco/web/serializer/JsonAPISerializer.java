package com.tenforce.esco.web.serializer;

import com.tenforce.esco.model.Item;
import com.tenforce.esco.model.MappingTarget;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


public class JsonAPISerializer {

    /**
     * Prepare the mapping suggestions data to be used as a JSON reply
     */
    public static List<Map<String,Object>> toJsonAPI(Collection<MappingTarget> response){
        List<Map<String,Object>> result = new ArrayList<>();
        for (MappingTarget mappingTarget:response){
            Map<String,Object> current = new HashMap<>();

            current.put("type","suggestions");

            Map<String, Object> attributes = getAttributeMap(mappingTarget);
            current.put("attributes",attributes);


            result.add(current);
        }

        // Sort in descending order based on combined score.
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                Double a_val = (Double) ((Map<String, Object>)a.get("attributes")).get("combinedScore");
                Double b_val = (Double) ((Map<String, Object>)b.get("attributes")).get("combinedScore");
                return -a_val.compareTo(b_val);
            }
        });

        return result;
    }

    public static Double getCombinedScore(MappingTarget mappingTarget){
        Double textualScore = mappingTarget.getTextualScore();
        Double contextualScore = mappingTarget.getContextualScore();
        Double floodingScore = mappingTarget.getFloodingScore();
        if (textualScore==null&&contextualScore==null&&floodingScore==null){
            return null;
        }
        return findMax(textualScore,contextualScore,floodingScore);
    }

    protected static double findMax(Double... vals) {
        double max = Double.NEGATIVE_INFINITY;
        for (Double d : vals) {
            if (d!=null){
                if (d > max) max = d;
            }
        }
        return max;
    }

    private static Map<String, Object> getAttributeMap(MappingTarget mappingTarget) {
        Map<String,Object> map = new HashMap<>();
        putOptional(map,"sourceUri",mappingTarget.getSourceURI());
        putOptional(map,"targetUri",mappingTarget.getTargetURI());
        putOptional(map,"targetUuid",mappingTarget.getTargetUUID());
        putOptional(map,"label",mappingTarget.getLabel());
        putOptional(map,"textualScore",mappingTarget.getTextualScore());
        putOptional(map,"contextualScore",mappingTarget.getContextualScore());
        putOptional(map,"floodingScore",mappingTarget.getFloodingScore());
        putOptional(map,"combinedScore",getCombinedScore(mappingTarget));
        return map;
    }

    private static void putOptional(Map<String,Object> map, String key, Object value){
        if (value != null){
            map.put(key,value);
        }
    }

    public static List<Map<String, Object>> toJsonAPI(List<Item> items, List<String> displayableLanguages) {
        List<Map<String,Object>> mapList = new ArrayList<>();
        for (Item currentItem:items){
            Map<String, Object> current = itemToMap(currentItem, displayableLanguages);
            mapList.add(current);
        }
        return mapList;
    }

    public static Map<String, Object> itemToMap(Item item, List<String> displayableLanguages) {
        Map<String,Object> map = new HashMap<>();
        map.put("id",item.getUuid());
        map.put("conceptSchemes", StringUtils.join(item.getConceptSchemes(), ","));

        Map<String,Object> attributes = new HashMap<>();
        putOptional(attributes,"score",item.getScore());
        attributes.put("uri",item.getUri());
        for (String lang: displayableLanguages){
            putOptional(attributes,"label_"+lang,item.getPreferredLabel(lang));
        }
        map.put("attributes",attributes);

        return map;
    }

}
