package com.tenforce.flooding.enums;

public enum MappingStatus
{
    INCORRECT	("-"),
    FLAGGED		("!"),
    UNKNOWN		("?"),
    CORRECT		("+");

    private String representation;

    MappingStatus(String rep)
    {
        representation = rep;
    }

    public String toString()
    {
        return representation;
    }

    public static MappingStatus parseStatus(String status)
    {
        for(MappingStatus rel : MappingStatus.values())
            if(status.equals(rel.toString()))
                return rel;
        return UNKNOWN;
    }
}