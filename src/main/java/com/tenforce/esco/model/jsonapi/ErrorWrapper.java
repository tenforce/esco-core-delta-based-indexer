package com.tenforce.esco.model.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ErrorWrapper implements Serializable, Response {
  @JsonProperty("errors")
  private List<Error> listErrors;

  public ErrorWrapper(List<Error> listErrors)
  {
    this.listErrors = listErrors;
  }

  public ErrorWrapper()
  {
    this(new ArrayList<Error>());
  }

  public List<Error> getListErrors() {
    return listErrors;
  }

  public void setListErrors(List<Error> listErrors) {
    this.listErrors = listErrors;
  }

  public void addError(Error error)
  {
    if(this.getListErrors() == null) this.setListErrors(new ArrayList<Error>());
    this.getListErrors().add(error);
  }

  public Error getError(int index)
  {
    try{
      return this.getListErrors().get(index);
    }
    catch(Exception ex)
    {
      return null;
    }
  }
}
