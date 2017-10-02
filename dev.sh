#!/bin/bash

# exec docker-compose stop indexer
exec docker run -it --rm -v $PWD:/data -p 8895:8080 --link visualizationtool_db_1:database --link visualizationtool_solr_1:solr visualizationtool_indexer mvn spring-boot:run -Dsparql.namespace=http://mu.semte.ch/application -Dsparql.virtuoso.enabled=true -Dsparql.virtuoso.endpoint=http://database:8890/sparql/ -Dspring.data.solr.host=http://solr:8080/solr/collection1/ -Dserver.contextPath=/indexer
