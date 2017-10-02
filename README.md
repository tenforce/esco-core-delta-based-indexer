Delta-Based-Indexer
===

Concepts are indexed on their description text for a language aware text search and some fields to do a drill down search on. Using Solr allows us to do lookups with Lucene syntax.

Some similarity measures to find concepts similar to a given concept are also implemented.

Indexing is made more efficient by allowing updates based on deltas (triplestore update records) from the delta service.

One can also just update everything in the triplestore or reload everything in a concept scheme.

Running
---

This is a standard spring-boot application. However, there are some EC constraints regarding configuration and libraries. Information on how to configure these can be found in the [Spring Boot template's README](https://git.tenforce.com/esco/spring-boot-template/blob/master/README.md).

Runs with Maven.

### Example:

    mvn spring-boot:run -Dsparql.applicationGraph=http://mu.semte.ch/application -Dsparql.virtuoso.enabled=true -Dsparql.virtuoso.endpoint=http://database:8890/sparql/ -Dspring.data.solr.host=http://solr:8080/solr/collection1/ -Desco.configuration.properties.file=/config/config_db.properties


HTTP endpoint
---

`/` Gives an overview of available routes with example queries based on indexed data.

### Indexing

`/index/all` Gives an overview of concept schemes in the triplestore and presents links to index them.

`/index/clear/all` Clears the index.

`/index/update/all` Clears the index and indexes all concept schemes in the triplestore.

`/index/update/conceptScheme` Indexes a concept scheme given by either `uuid` or `uri`.

`/index/update/delta` Updates the index based on a JSON delta record in the POST body. This endpoint is meant for the delta service.


### Searching

`/search/detail` Shows details of an indexed concept given by `uuid`.

`/search/textSearch` Search concepts for `text` in a certain concept scheme `conceptScheme`. Specify the `locale` of the search query. Optionally limit `numberOfResults`. `include` and `exclude` filters can be used to filter on indexed fields. These filters can be repeated to drill down the search. If the client API doesn't support this it's possible to use: `exclude=field:a+field:b` or `exclude=field:a field:b`.

### Similarity

`/similar/text` Find concepts of a `targetConceptScheme` similar to a given concept. The concept is given by its UUID with `sourceConceptUUID`. The result is based on a textual search, Lucene's MoreLikeThis. Specify the `locale` of the search text to be taken from the source concept. Optionally limit `numberOfResults`.

`/similar/full` Find concepts of `targetConceptScheme` similar to a given concept of `sourceConceptScheme`. The concept is given by its UUID with `sourceConceptUUID`. The result is based on a combination of all similarity measures. Specify the `locale` of the search text to be taken from the source concept.

The full similarity search also uses a metric based on the graph structure. This is not available in itself yet, as it is not fully tested and doesn't often give results. However, when a result is returned it's usually a good match so it's included in the full similarity metric.

Suggested improvements
---

The service could be abstracted so it can be used for other projects as well.
