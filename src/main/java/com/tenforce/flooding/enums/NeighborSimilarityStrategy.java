package com.tenforce.flooding.enums;

public enum NeighborSimilarityStrategy {

    ANCESTORS ("Ancestors"),
    DESCENDANTS ("Descendants"),
    AVERAGE ("Average"),
    MAXIMUM ("Maximum"),
    MINIMUM ("Minimum");

    String label;

    NeighborSimilarityStrategy(String s)
    {
        label = s;
    }

    public String toString()
    {
        return label;
    }

}
