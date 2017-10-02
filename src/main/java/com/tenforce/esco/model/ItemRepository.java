package com.tenforce.esco.model;

import org.springframework.data.solr.repository.SolrCrudRepository;

import java.util.List;

/**
 * A repo to do standard CRUD operations on the SOLR database
 */
public interface ItemRepository extends SolrCrudRepository<Item, String> {

    List<Item> findByUuid(String text);

}