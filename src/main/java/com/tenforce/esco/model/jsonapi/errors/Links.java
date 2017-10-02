package com.tenforce.esco.model.jsonapi.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

public class Links implements Serializable {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String about;

    public Links(String about)
    {
        this.about = about;
    }

    public Links()
    {
        this("");
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}