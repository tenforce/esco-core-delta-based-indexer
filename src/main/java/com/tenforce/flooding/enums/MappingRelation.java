package com.tenforce.flooding.enums;


public enum MappingRelation
{
    EQUIVALENCE	("=","equivalence"),
    SUPERCLASS	(">","superclass"),
    SUBCLASS	("<","subclass"),
    OVERLAP		("^","overlap"),
    UNKNOWN		("?","unknown");

    private String representation;
    private String label;

    MappingRelation(String rep, String l)
    {
        representation = rep;
        label = l;
    }

    public String getLabel()
    {
        return label;
    }

    public MappingRelation inverse()
    {
        if(this.equals(SUBCLASS))
            return SUPERCLASS;
        else if(this.equals(SUPERCLASS))
            return SUBCLASS;
        else
            return this;
    }

    public String toString()
    {
        return representation;
    }

    public static MappingRelation parseRelation(String relation)
    {
        if(relation.length() == 1)
        {
            for(MappingRelation rel : MappingRelation.values())
                if(relation.equals(rel.toString()))
                    return rel;
        }
        else
        {
            for(MappingRelation rel : MappingRelation.values())
                if(relation.equals(rel.getLabel()))
                    return rel;
        }
        return UNKNOWN;
    }
}
