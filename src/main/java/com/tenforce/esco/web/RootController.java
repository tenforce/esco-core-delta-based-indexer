package com.tenforce.esco.web;

import com.tenforce.Application;
import com.tenforce.esco.model.DeltaRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Controller
@RequestMapping("/")
@PropertySource(Application.CONFIG_PLACEHOLDER)
public class RootController {

    private static final String exampleSearch = "Test";
    private static final String exampleLocale = "en";

    // One of the concepts. Used for example only.
    private static final String exampleSourceConceptUUID = "1612130c-533c-11e6-89a4-a439968efbe3";
    private static final String exampleSourceConceptSchemeURI = "http://data.europa.eu/esco/ConceptScheme/ISCO2008";
    private static final String exampleSourceConceptSchemeUUID = "82df8ed9-ed3a-45ae-a14f-b89f44c6e243";

    @Value("${solr.results.default}")
    private Integer defaultResultsNumber;

    @RequestMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResourceSupport root() throws Exception {
        ResourceSupport resource = new ResourceSupport();

        resource.add(linkTo(methodOn(IndexerController.class)
                .allConceptSchemes()).withRel("index:all"));

        resource.add(linkTo(methodOn(IndexerController.class)
                .clearIndex()).withRel("index:clear:all"));

        resource.add(linkTo(methodOn(IndexerController.class)
                .updateAll()).withRel("index:update:all"));

        resource.add(linkTo(methodOn(IndexerController.class)
                .updateConceptScheme(exampleSourceConceptSchemeUUID, null)).withRel("index:update:conceptScheme"));

        resource.add(linkTo(methodOn(IndexerController.class)
                .updateConceptScheme(null, exampleSourceConceptSchemeURI)).withRel("index:update:conceptScheme"));

        resource.add(linkTo(methodOn(IndexerController.class)
                .updateDelta(new DeltaRecord())).withRel("index:update:delta"));

        resource.add(linkTo(methodOn(SearchController.class)
                .getByUUID(exampleSourceConceptUUID)).withRel("search:detail"));

        resource.add(linkTo(methodOn(SearchController.class)
                .textSearch(exampleSourceConceptSchemeUUID,exampleSearch, exampleLocale,defaultResultsNumber, null, null)).withRel("search:textSearch"));

        resource.add(linkTo(methodOn(SearchController.class)
                .similarText(exampleSourceConceptSchemeUUID,exampleSourceConceptUUID, exampleLocale,defaultResultsNumber)).withRel("search:similar:text"));

        resource.add(linkTo(methodOn(SearchController.class)
                .similarFull(exampleSourceConceptUUID, exampleSourceConceptSchemeUUID,exampleSourceConceptSchemeUUID, exampleLocale)).withRel("search:similar:full"));

        return resource;
    }

}