package com.tenforce.esco.model;


import org.apache.solr.client.solrj.SolrQuery;

import java.util.ArrayList;


/**
 * Search filters for solr.
 * Modifies a SolrQuery to perform the filtering.
 * Works with Jackson, enabling specification of filters with JSON.
 */
public class SearchFilter {

    private ArrayList<String> include;
    private ArrayList<String> exclude;

    public SearchFilter() {
        this(null, null);
    }

    public SearchFilter(ArrayList<String> include, ArrayList<String> exclude) {
        this.include = include == null ? new ArrayList<String>() : include;
        this.exclude = exclude == null ? new ArrayList<String>() : exclude;
    }

    public ArrayList<String> getInclude() {
        return include;
    }

    public void setInclude(ArrayList<String> include) {
        this.include = include;
    }

    public void addInclude(String include) {
        this.include.add(include);
    }

    public ArrayList<String> getExclude() {
        return exclude;
    }

    public void setExclude(ArrayList<String> exclude) {
        this.exclude = exclude;
    }

    public void addExclude(String exclude) {
        this.exclude.add(exclude);
    }

    public void merge(SearchFilter that) {
        this.include.addAll(that.include);
        this.exclude.addAll(that.exclude);
    }

    public void applyFilter(SolrQuery query) {
        for(String i: include){
            query.addFilterQuery(i);
        }
        for(String e: exclude){
            query.addFilterQuery("-(" + e + ")");
        }
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "include=" + include +
                ", exclude=" + exclude +
                '}';
    }
}
