package com.tenforce.esco.model.jsonapi.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

public class Source implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String pointer;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String parameter;

    public Source(String pointer, String parameter)
    {
        this.setPointer(pointer);
        this.setParameter(parameter);
    }

    public Source()
    {this(null, null);}

    public String getPointer() {
      return pointer;
    }

    public void setPointer(String pointer) {
        this.pointer = pointer;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}