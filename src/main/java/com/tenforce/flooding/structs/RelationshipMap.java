package com.tenforce.flooding.structs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class RelationshipMap
{

//Attributes

    //Relationships between classes
    //Hierarchical relations and property restrictions (with transitive closure)
    private Table3List<Integer,Integer,Relationship> ancestorMap; //Class -> Ancestor -> Relationship
    private Table3List<Integer,Integer,Relationship> descendantMap;	//Class -> Descendant -> Relationship

    //Relationships between individuals and classes
    private Table2Set<Integer,Integer> instanceOfMap; //Individual -> Class
    private Table2Set<Integer,Integer> hasInstanceMap; //Class -> Individual

    //Relationships between individuals
    private Table3Set<Integer,Integer,Integer> ancestorIndividuals; //'Child' or 'Source' -> Property -> 'Parent' or 'Target'
    private Table3Set<Integer,Integer,Integer> descendantIndividuals; //'Parent' or 'Target' -> Property -> 'Child' or 'Source'

    //Relationships between properties
    //Hierarchical and inverse relations
    private Table2Set<Integer,Integer> subProp; //Property -> SubProperty
    private Table2Set<Integer,Integer> superProp; //Property -> SuperProperty
    private Table2Set<Integer,Integer> inverseProp; //Property -> InverseProperty
    //Transitivity relations (transitive properties will be mapped to themselves)
    private Table2Set<Integer,Integer> transitiveOver; //Property1 -> Property2 over which 1 is transitive
    private HashSet<Integer> symmetric;

//Constructors

    /**
     * Creates a new empty RelationshipMap
     */
    public RelationshipMap()
    {
        descendantMap = new Table3List<>();
        ancestorMap = new Table3List<>();
        instanceOfMap = new Table2Set<>();
        hasInstanceMap = new Table2Set<>();
        ancestorIndividuals = new Table3Set<>();
        descendantIndividuals = new Table3Set<>();
        subProp = new Table2Set<>();
        superProp = new Table2Set<>();
        inverseProp = new Table2Set<>();
        transitiveOver = new Table2Set<>();
        symmetric = new HashSet<>();
    }

//Public Methods

    /**
     * Adds a direct relationship between two classes with a given property and restriction
     * @param child: the index of the child class
     * @param parent: the index of the parent class
     * @param prop: the property in the subclass relationship
     * @param rest: the restriction in the subclass relationship
     */
    public void addClassRelationship(int child, int parent, int prop, boolean rest)
    {
        addClassRelationship(child,parent,1,prop,rest);
    }

    /**
     * Adds a relationship between two classes with a given distance, property and restriction
     * @param child: the index of the child class
     * @param parent: the index of the parent class
     * @param distance: the distance (number of edges) between the classes
     * @param prop: the property in the subclass relationship
     * @param rest: the restriction in the subclass relationship
     */
    public void addClassRelationship(int child, int parent, int distance, int prop, boolean rest)
    {
        //Create the relationship
        Relationship r = new Relationship(distance,prop,rest);
        //Then update the MultiMaps
        descendantMap.add(parent,child,r);
        ancestorMap.add(child,parent,r);
    }

    /**
     * Adds a direct hierarchical relationship between two classes
     * @param child: the index of the child class
     * @param parent: the index of the parent class
     */
    public void addDirectSubclass(int child, int parent)
    {
        addClassRelationship(child,parent,1,-1,false);
    }

    /**
     * Adds an equivalence relationship between two classes with a given property and restriction
     * @param class1: the index of the first equivalent class
     * @param class2: the index of the second equivalent class
     * @param prop: the property in the subclass relationship
     * @param rest: the restriction in the subclass relationship
     */
    public void addEquivalence(int class1, int class2, int prop, boolean rest)
    {
        addClassRelationship(class1,class2,0,prop,rest);
        if(symmetric.contains(prop))
            addClassRelationship(class2,class1,0,prop,rest);
    }

    /**
     * Adds an equivalence relationship between two classes
     * @param class1: the index of the first equivalent class
     * @param class2: the index of the second equivalent class
     */
    public void addEquivalentClass(int class1, int class2)
    {
        addEquivalence(class1,class2,-1,false);
    }

    /**
     * Adds a relationship between two individuals through a given property
     * @param indiv1: the index of the first individual
     * @param prop: the property in the relationship
     * @param indiv2: the index of the second individual
     */
    public void addIndividualRelationship(int indiv1, int prop, int indiv2)
    {
        ancestorIndividuals.add(indiv1,prop,indiv2);
        descendantIndividuals.add(indiv2,prop,indiv1);
    }

    /**
     * Adds an instantiation relationship between an individual and a class
     * @param individualId: the index of the individual
     * @param classId: the index of the class
     */
    public void addInstance(int individualId, int classId)
    {
        instanceOfMap.add(individualId,classId);
        hasInstanceMap.add(classId,individualId);
    }

    /**
     * Adds a new inverse relationship between two properties if it doesn't exist
     * @param property1: the index of the first property
     * @param property2: the index of the second property
     */
    public void addInverseProp(int property1, int property2)
    {
        if(property1 != property2)
        {
            inverseProp.add(property1, property2);
            inverseProp.add(property2, property1);
        }
    }

    /**
     * Adds a relationship between two properties
     * @param child: the index of the child property
     * @param parent: the index of the parent property
     */
    public void addSubProperty(int child, int parent)
    {
        //Then update the MultiMaps
        subProp.add(parent,child);
        superProp.add(child,parent);
    }

    /**
     * @param prop: the property to set as symmetric
     */
    public void addSymmetric(int prop)
    {
        symmetric.add(prop);
    }

    /**
     * @param prop: the property to set as transitive
     */
    public void addTransitive(int prop)
    {
        transitiveOver.add(prop,prop);
    }

    /**
     * @param prop1: the property to set as transitive over prop2
     * @param prop2: the property over which prop1 is transitive
     */
    public void addTransitiveOver(int prop1, int prop2)
    {
        transitiveOver.add(prop1,prop2);
    }

    /**
     * @param classId: the id of the class to search in the map
     * @return the list of siblings of the given class with the given property
     */
    public Set<Integer> getAllSiblings(int classId)
    {
        Set<Integer> parents = getAncestors(classId,1);
        HashSet<Integer> siblings = new HashSet<Integer>();
        for(Integer i : parents)
        {
            for(Relationship r : getRelationships(classId,i))
            {
                Set<Integer> children = getDescendants(i,1,r.getProperty());
                for(Integer j : children)
                    if(j != classId)
                        siblings.add(j);
            }
        }
        return siblings;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @return the list of ancestors of the given class
     */
    public Set<Integer> getAncestors(int classId)
    {
        if(ancestorMap.contains(classId))
            return ancestorMap.keySet(classId);
        return new HashSet<>();
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param distance: the distance between the class and its ancestors
     * @return the list of ancestors at the given distance from the input class
     */
    public Set<Integer> getAncestors(int classId, int distance)
    {
        HashSet<Integer> asc = new HashSet<Integer>();
        if(!ancestorMap.contains(classId))
            return asc;
        for(Integer i : ancestorMap.keySet(classId))
            for(Relationship r : ancestorMap.get(classId, i))
                if(r.getDistance() == distance)
                    asc.add(i);
        return asc;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param prop: the relationship property between the class and its ancestors
     * @return the list of ancestors at the given distance from the input class
     */
    public Set<Integer> getAncestorsProperty(int classId, int prop)
    {
        HashSet<Integer> asc = new HashSet<Integer>();
        if(!ancestorMap.contains(classId))
            return asc;
        for(Integer i : ancestorMap.keySet(classId))
            for(Relationship r : ancestorMap.get(classId, i))
                if(r.getProperty() == prop)
                    asc.add(i);
        return asc;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param distance: the distance between the class and its ancestors
     * @param prop: the relationship property between the class and its ancestors
     * @return the list of ancestors of the input class that are at the given
     * distance and with the given property
     */
    public Set<Integer> getAncestors(int classId, int distance, int prop)
    {
        HashSet<Integer> asc = new HashSet<Integer>();
        if(!ancestorMap.contains(classId))
            return asc;
        for(Integer i : ancestorMap.keySet(classId))
            for(Relationship r : ancestorMap.get(classId, i))
                if(r.getDistance() == distance && r.getProperty() == prop)
                    asc.add(i);
        return asc;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @return the list of direct children of the given class
     */
    public Set<Integer> getChildren(int classId)
    {
        return getDescendants(classId,1);
    }

    /**
     * @param classes: the set the class to search in the map
     * @return the list of direct subclasses shared by the set of classes
     */
    public Set<Integer> getCommonSubClasses(Set<Integer> classes)
    {
        if(classes == null || classes.size() == 0)
            return null;
        Iterator<Integer> it = classes.iterator();
        Vector<Integer> subclasses = new Vector<Integer>(getSubClasses(it.next(),false));
        while(it.hasNext())
        {
            HashSet<Integer> s = new HashSet<Integer>(getSubClasses(it.next(),false));
            for(int i = 0; i < subclasses.size(); i++)
            {
                if(!s.contains(subclasses.get(i)))
                {
                    subclasses.remove(i);
                    i--;
                }
            }
        }
        for(int i = 0; i < subclasses.size()-1; i++)
        {
            for(int j = i+1; j < subclasses.size(); j++)
            {
                if(isSubclass(subclasses.get(i),subclasses.get(j)))
                {
                    subclasses.remove(i);
                    i--;
                    j--;
                }
                if(isSubclass(subclasses.get(j),subclasses.get(i)))
                {
                    subclasses.remove(j);
                    j--;
                }
            }
        }
        return new HashSet<>(subclasses);
    }

    /**
     * @param classId: the id of the class to search in the map
     * @return the list of descendants of the input class
     */
    public Set<Integer> getDescendants(int classId)
    {
        if(descendantMap.contains(classId))
            return descendantMap.keySet(classId);
        return new HashSet<>();
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param distance: the distance between the class and its ancestors
     * @return the list of descendants at the given distance from the input class
     */
    public Set<Integer> getDescendants(int classId, int distance)
    {
        HashSet<Integer> desc = new HashSet<>();
        if(!descendantMap.contains(classId))
            return desc;
        for(Integer i : descendantMap.keySet(classId))
            for(Relationship r : descendantMap.get(classId, i))
                if(r.getDistance() == distance)
                    desc.add(i);
        return desc;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param prop: the relationship property between the class and its ancestors
     * @return the list of descendants at the given distance from the input class
     */
    public Set<Integer> getDescendantsProperty(int classId, int prop)
    {
        HashSet<Integer> desc = new HashSet<Integer>();
        if(!descendantMap.contains(classId))
            return desc;
        for(Integer i : descendantMap.keySet(classId))
            for(Relationship r : descendantMap.get(classId, i))
                if(r.getProperty() == prop)
                    desc.add(i);
        return desc;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param distance: the distance between the class and its ancestors
     * @param prop: the relationship property between the class and its ancestors
     * @return the list of descendants of the input class at the given distance
     * and with the given property
     */
    public Set<Integer> getDescendants(int classId, int distance, int prop)
    {
        HashSet<Integer> desc = new HashSet<Integer>();
        if(!descendantMap.contains(classId))
            return desc;
        for(Integer i : descendantMap.keySet(classId))
            for(Relationship r : descendantMap.get(classId, i))
                if(r.getDistance() == distance && r.getProperty() == prop)
                    desc.add(i);
        return desc;
    }

    /**
     * @param child: the index of the child class
     * @param parent: the index of the parent class
     * @return the minimal distance between the child and parent,
     * or 0 if child==parent, or -1 if they aren't related
     */
    public int getDistance(int child, int parent)
    {
        if(child == parent)
            return 0;
        if(!ancestorMap.contains(child, parent))
            return -1;
        Vector<Relationship> rels = ancestorMap.get(child,parent);
        int distance = rels.get(0).getDistance();
        for(Relationship r : rels)
            if(r.getDistance() < distance)
                distance = r.getDistance();
        return distance;
    }

    /**
     * @param classId: the id of the class to search in the map
     * @return the list of equivalences of the given class
     */
    public Set<Integer> getEquivalences(int classId)
    {
        return getDescendants(classId, 0);
    }

    /**
     * @param classId: the id of the class to search in the map
     * @return the list of direct parents of the given class
     */
    public Set<Integer> getParents(int classId)
    {
        return getAncestors(classId,1);
    }

    /**
     * @param child: the id of the child class to search in the map
     * @param parent: the id of the parent class to search in the map
     * @return the relationships between the two classes
     */
    public Vector<Relationship> getRelationships(int child, int parent)
    {
        return ancestorMap.get(child).get(parent);
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param direct: whether to return all subclasses or just the direct ones
     * @return the list of direct or indirect subclasses of the input class
     */
    public Set<Integer> getSubClasses(int classId, boolean direct)
    {
        if(direct)
            return getDescendants(classId,1,-1);
        else
            return getDescendantsProperty(classId,-1);
    }

    /**
     * @param classId: the id of the class to search in the map
     * @param direct: whether to return all superclasses or just the direct ones
     * @return the list of direct or indirect superclasses of the input class
     */
    public Set<Integer> getSuperClasses(int classId, boolean direct)
    {
        if(direct)
            return getAncestors(classId,1,-1);
        else
            return getAncestorsProperty(classId,-1);
    }

    /**
     * @param child: the index of the child class
     * @param parent: the index of the parent class
     * @return whether the RelationshipMap contains an 'is_a' relationship between child and parent
     */
    public boolean isSubclass(int child, int parent)
    {
        if(!descendantMap.contains(parent,child))
            return false;
        Vector<Relationship> rels = descendantMap.get(parent,child);
        for(Relationship r : rels)
            if(r.getProperty() == -1)
                return true;
        return false;
    }

    /**
     * Compute the transitive closure of the RelationshipMap
     * by adding inherited relationships (and their distances)
     * This is an implementation of the Semi-Naive Algorithm
     */
    public void transitiveClosure()
    {
        //Transitive closure for class relations
        Set<Integer> t = descendantMap.keySet();
        int lastCount = 0;
        for(int distance = 1; lastCount != descendantMap.size(); distance++)
        {
            lastCount = descendantMap.size();
            for(Integer i : t)
            {
                Set<Integer> childs = getChildren(i);
                childs.addAll(getEquivalences(i));
                Set<Integer> pars = getAncestors(i,distance);
                for(Integer j : pars)
                {
                    Vector<Relationship> rel1 = getRelationships(i,j);
                    for(int k = 0; k < rel1.size(); k++)
                    {
                        Relationship r1 = rel1.get(k);
                        int p1 = r1.getProperty();
                        for(Integer h : childs)
                        {
                            Vector<Relationship> rel2 = getRelationships(h,i);
                            for(int l = 0; l < rel2.size(); l++)
                            {
                                Relationship r2 = rel2.get(l);
                                int p2 = r2.getProperty();
                                //We only do transitive closure if the property is the same (and transitive)
                                //for two relationships or one of the properties is 'is_a' (-1)
                                if(!(p1 == -1 || p2 == -1 || transitiveOver.contains(p2,p1)))
                                    continue;
                                int dist = r1.getDistance() + r2.getDistance();
                                int prop;
                                if(p1 == p2 || p1 != -1)
                                    prop = p1;
                                else
                                    prop = p2;
                                boolean rest = r1.getRestriction() && r2.getRestriction();
                                addClassRelationship(h, j, dist, prop, rest);
                            }
                        }
                    }
                }
            }
        }
    }

}