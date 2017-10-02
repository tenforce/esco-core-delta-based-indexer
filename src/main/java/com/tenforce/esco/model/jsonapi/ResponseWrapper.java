package com.tenforce.esco.model.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ResponseWrapper implements Serializable, Response {
    @JsonProperty("data")
    private Object response;

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
