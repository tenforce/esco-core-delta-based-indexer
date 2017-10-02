package com.tenforce.esco.model;

public class MappingTarget {
    String clusterName;
    Double textualScore;
    Double contextualScore;
    Double floodingScore;

    String label;
    String sourceURI;
    String targetURI;
    String targetUUID;


    @Override
    public int hashCode() {
        int result = sourceURI != null ? sourceURI.hashCode() : 0;
        result = 31 * result + (targetURI != null ? targetURI.hashCode() : 0);
        return result;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Double getTextualScore() {
        return textualScore;
    }

    public void setTextualScore(Double textualScore) {
        this.textualScore = textualScore;
    }

    public Double getContextualScore() {
        return contextualScore;
    }

    public void setContextualScore(Double contextualScore) {
        this.contextualScore = contextualScore;
    }

    public Double getFloodingScore() {
        return floodingScore;
    }

    public void setFloodingScore(Double floodingScore) {
        this.floodingScore = floodingScore;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public String getTargetURI() {
        return targetURI;
    }

    public void setTargetURI(String targetURI) {
        this.targetURI = targetURI;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(String targetUUID) {
        this.targetUUID = targetUUID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappingTarget that = (MappingTarget) o;

        if (sourceURI != null ? !sourceURI.equals(that.sourceURI) : that.sourceURI != null) return false;
        return targetURI != null ? targetURI.equals(that.targetURI) : that.targetURI == null;

    }

}
