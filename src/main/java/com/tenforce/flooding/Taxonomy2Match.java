package com.tenforce.flooding;


import com.tenforce.flooding.structs.Individual;

import java.util.HashMap;
import java.util.Set;

public class Taxonomy2Match
{

//Attributes
    //The map of indexes (Integer) -> Individuals in the ontology
    private HashMap<Integer,Individual> individuals;
    /**
     * Constructs an empty ontology
     */
    public Taxonomy2Match()
    {
        individuals = new HashMap<>();
    }

    /**
     * Adds an individual to the ontology (for use when
     * the individuals are not listed within the ontology)
     * @param index: the index of the individual
     * @param i: the individual to add to the ontology
     */
    public void addIndividual(int index, Individual i)
    {
        individuals.put(index,i);
    }

    /**
     * @param index: the index of the entity to search in the Ontology
     * @return whether the entity is contained in the Ontology
     */
    public boolean contains(int index)
    {
        return individuals.containsKey(index);
    }

    /**
     * @return the indexes of the Individuals in the Ontology
     */
    public Set<Integer> getIndividuals()
    {
        return individuals.keySet();
    }

    public String getName(int index)
    {
        if (individuals.get(index)==null){
            return "No name defined";
        }
        return individuals.get(index).getName();
    }

    /**
     * @return the number of Individuals in the Ontology
     */
    public int individualCount()
    {
        return individuals.size();
    }

}