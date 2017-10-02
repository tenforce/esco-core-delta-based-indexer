package com.tenforce.esco.model.jsonapi.errors;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Error implements Serializable {
  private String id;
  private Links links;
  private HttpStatus status;
  private String code;
  private String title;
  private String detail;
  private Source source;
  private String meta;

  public Error(String id, Links links, HttpStatus status, String code, String title, String detail, Source source, String meta) {
    this.id = id;
    this.links = links;
    this.status = status;
    this.code = code;
    this.title = title;
    this.detail = detail;
    this.source = source;
    this.meta = meta;
  }

  public Error() {
    this(null, new Links(), null, null, null, null, new Source(), null);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Links getLinks() {
    return links;
  }

  @JsonGetter("links")
  public Links getLinksJSON() {
    if(StringUtils.isEmpty(this.getLinks().getAbout())) return null;
    return this.getLinks();
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public HttpStatus getStatus() {
    return status;
  }

  @JsonGetter("status")
  public String getStatusJSON() {
    if(status != null) return String.valueOf(status.value());
    else return null;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Source getSource() {
    return source;
  }

  @JsonGetter("source")
  public Source getSourceJSON() {
    if(StringUtils.isEmpty(this.getSource().getParameter()) && StringUtils.isEmpty(this.getSource().getPointer())) return null;
    return this.getSource();
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public void setSource(Source source) {
    this.source = source;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta(String meta) {
    this.meta = meta;
  }

  @JsonIgnore
  public String getAbout()
  {
    if(this.getLinks() == null) return null;
    return this.getLinks().getAbout();
  }

  @JsonIgnore
  public void setAbout(String about)
  {
    if(this.getLinks() != null) this.getLinks().setAbout(about);
  }

  @JsonIgnore
  public String getPointer()
  {
    if(this.getSource() == null) return null;
    return this.getSource().getPointer();
  }

  @JsonIgnore
  public void setPointer(String pointer)
  {
    if(this.getSource() != null) this.getSource().setPointer(pointer);
  }

  @JsonIgnore
  public String getParameter()
  {
    if(this.getSource() == null) return null;
    return this.getSource().getParameter();
  }

  @JsonIgnore
  public void setParameter(String parameter)
  {
    if(this.getSource() != null) this.getSource().setParameter(parameter);
  }
}
