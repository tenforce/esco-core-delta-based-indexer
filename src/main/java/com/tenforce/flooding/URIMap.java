package com.tenforce.flooding;


import com.tenforce.flooding.enums.EntityType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Set;

public class URIMap
{

//Attributes
    //The numeric index (Integer) <-> URI (String) maps of ontology entities
    private HashMap<Integer,String> indexURI;
    private HashMap<String,Integer> URIindex;
    //The numeric index (Integer) -> EntityType map of ontology entities
    private HashMap<Integer,EntityType> indexType;
    //The total number of stored URIs
    private int size;

//Constructors

    public URIMap()
    {
        indexURI = new HashMap<>();
        URIindex = new HashMap<>();
        indexType = new HashMap<>();
        size = 0;
    }

//Public Methods

    /**
     * @param uri: the URI to add to AML
     * @return the index of the added URI
     */
    public int addURI(String uri, EntityType t)
    {
        String newUri = uri;
        if(newUri.contains("%") || newUri.contains("&"))
        {
            try
            {
                newUri = URLDecoder.decode(newUri,"UTF-8");
            }
            catch(UnsupportedEncodingException e)
            {
                //Do nothing
            }
        }
        if(URIindex.containsKey(newUri))
            return URIindex.get(newUri);
        else
        {
            size++;
            Integer i = size;
            indexURI.put(i,newUri);
            URIindex.put(newUri,i);
            indexType.put(i, t);
            return size;
        }
    }

    /**
     * @param uri: the URI to search in AML
     * @return the index of the input URI
     */
    public int getIndex(String uri)
    {
        if(URIindex.containsKey(uri))
            return URIindex.get(uri);
        else
            return -1;
    }

    /**
     * @return the indexes in the URIMap
     */
    public Set<Integer> getIndexes()
    {
        return indexURI.keySet();
    }

    /**
     * @param index: the index of the entity to get the name
     * @return the local name of the entity with the given index
     */
    public String getLocalName(int index)
    {
        String uri = indexURI.get(index);
        if(uri == null)
            return null;
        int i = uri.indexOf("#") + 1;
        if(i == 0)
            i = uri.lastIndexOf("/") + 1;
        return uri.substring(i);
    }


    /**
     * @param index: the index to search in AML
     * @return the URI of the input index
     */
    public String getURI(int index)
    {
        if(indexURI.containsKey(index))
            return indexURI.get(index);
        else
            return null;
    }

    /**
     * @param index: the index of the Ontology entity
     * @return whether the entity is a Class
     */
    public boolean isClass(int index)
    {
        return indexType.get(index).equals(EntityType.CLASS);
    }

    /**
     * @return the number of entries in the URI map
     */
    public int size()
    {
        return indexURI.size();
    }
}