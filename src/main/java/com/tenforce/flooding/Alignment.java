package com.tenforce.flooding;


import com.tenforce.esco.model.MappingTarget;
import com.tenforce.flooding.enums.MappingRelation;
import com.tenforce.flooding.enums.MappingStatus;
import com.tenforce.flooding.structs.Table2Map;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Alignment implements Collection<Mapping>
{

//Attributes

    //Term mappings organized in list
    private Vector<Mapping> maps;
    //Term mappings organized by source class (Source Id, Target Id, Mapping)
    private Table2Map<Integer,Integer,Mapping> sourceMaps;
    //Term mappings organized by target class (Target Id, Source Id, Mapping)
    private Table2Map<Integer,Integer,Mapping> targetMaps;
    //Whether the Alignment is internal
    private boolean internal;
    //Link to AML and the Ontologies
    private Taxonomy2Match source;
    private Taxonomy2Match target;

    /**
     * Creates a new Alignment instance.
     */
    public Alignment(Taxonomy2Match source, Taxonomy2Match target)
    {
        maps = new Vector<>(0,1);
        sourceMaps = new Table2Map<>();
        targetMaps = new Table2Map<>();
        this.source = source;
        this.target = target;
        internal = false;
    }

    public Map<String,Map<String,MappingTarget>> getAllMappingTargets(URIMap uris, HashMap<Integer,String> uuidMap) throws FileNotFoundException
    {
        Map<String,Map<String,MappingTarget>> result = new HashMap<>();
        for(Mapping m : maps){
            Integer sourceId = m.getSourceId();
            Integer targetId = m.getTargetId();
            MappingTarget current = new MappingTarget();

            String sourceURI = uris.getURI(sourceId);
            String targetURI = uris.getURI(targetId);

            current.setFloodingScore(m.getSimilarity());

            current.setSourceURI(uris.getURI(sourceId));
            current.setTargetURI(targetURI);
            current.setTargetUUID(uuidMap.get(targetId));
            current.setLabel(target.getName(targetId));

            Map mappings = new HashMap<>();
            if (result.containsKey(sourceURI)){
                mappings = result.get(sourceURI);
            }
            mappings.put(targetURI,current);


            result.put(targetURI,mappings);
        }

        return result;
    }

    /**
     * Adds a new Mapping to the Alignment if it is non-redundant
     * Otherwise, updates the similarity of the already present Mapping
     * to the maximum similarity of the two redundant Mappings
     * @param sourceId: the index of the source class to add to the Alignment
     * @param targetId: the index of the target class to add to the Alignment
     * @param sim: the similarity between the classes
     */
    public void add(int sourceId, int targetId, double sim)
    {
        add(sourceId,targetId,sim, MappingRelation.EQUIVALENCE);
    }

    /**
     * Adds a new Mapping to the Alignment if it is non-redundant
     * Otherwise, updates the similarity of the already present Mapping
     * to the maximum similarity of the two redundant Mappings
     * @param sourceId: the index of the source class to add to the Alignment
     * @param targetId: the index of the target class to add to the Alignment
     * @param sim: the similarity between the classes
     * @param r: the mapping relationship between the classes
     */
    public boolean add(int sourceId, int targetId, double sim, MappingRelation r)
    {
        //Unless the Alignment is internal, we can't have a mapping
        //involving entities that exist in both ontologies (they are
        //the same entity, and therefore shouldn't map with other
        //entities in either ontology)
        if(!internal && (source.contains(targetId) || target.contains(sourceId)))
            return false;

        //Construct the Mapping
        Mapping m = new Mapping(sourceId, targetId, sim, r);
        //If it isn't listed yet, add it
        if(!sourceMaps.contains(sourceId,targetId))
            return updateMapping(sourceId, targetId, m);
            //Otherwise update the similarity
        else
        {
            m = sourceMaps.get(sourceId,targetId);
            boolean check = false;
            if(m.getSimilarity() < sim)
            {
                m.setSimilarity(sim);
                check = true;
            }
            if(!m.getRelationship().equals(r))
            {
                m.setRelationship(r);
                check = true;
            }
            return check;
        }
    }


    /**
     * Saves the Alignment into a .tsv file in AML format
     * @param file: the output file
     */
    public void saveTSV(Taxonomy2Match source, Taxonomy2Match target, URIMap uris, String file) throws FileNotFoundException
    {
        PrintWriter outStream = new PrintWriter(new FileOutputStream(file));
        outStream.println("#Output Alignment File");
        outStream.println("Source URI\tSource Label\tTarget URI\tTarget Label\tSimilarity\tRelationship\tStatus");
        int count = 0;
        for(Mapping m : maps){
            Integer sourceId = m.getSourceId();
            Integer targetId = m.getTargetId();
            String out = uris.getURI(sourceId) + "\t" + source.getName(sourceId) +
                    "\t" + uris.getURI(targetId) + "\t" + target.getName(targetId) +
                    "\t" + m.getSimilarity() + "\t" + m.getRelationship().toString();
            if(!m.getStatus().equals(MappingStatus.UNKNOWN)){
                out += "\t" + m.getStatus();
            }
            count++;
            outStream.println(out);
        }
        outStream.close();
//        System.out.println("Added "+count+" items as final alignment");
    }


    private boolean updateMapping(int sourceId, int targetId, Mapping m) {
        maps.add(m);
        sourceMaps.add(sourceId, targetId, m);
        targetMaps.add(targetId, sourceId, m);
        return true;
    }

    /**
     * Adds a new Mapping to the Alignment if it is non-redundant
     * Otherwise, updates the similarity of the already present Mapping
     * to the maximum similarity of the two redundant Mappings
     * @param sourceId: the index of the source class to add to the Alignment
     * @param targetId: the index of the target class to add to the Alignment
     * @param sim: the similarity between the classes
     * @param r: the mapping relationship between the classes
     * @param s: the mapping status
     */
    public boolean addNonRedundant(int sourceId, int targetId, double sim, MappingRelation r, MappingStatus s)
    {
        //Unless the Alignment is internal, we can't have a mapping
        //involving entities that exist in both ontologies (they are
        //the same entity, and therefore shouldn't map with other
        //entities in either ontology)
        if(!internal && (source.contains(targetId) || target.contains(sourceId)))
            return false;

        //Construct the Mapping
        Mapping m = new Mapping(sourceId, targetId, sim, r);
        m.setStatus(s);
        //If it isn't listed yet, add it
        if(!sourceMaps.contains(sourceId,targetId))
        {
            return updateMapping(sourceId, targetId, m);
        }
        //Otherwise update the similarity
        else
        {
            m = sourceMaps.get(sourceId,targetId);
            boolean check = false;
            if(m.getSimilarity() < sim)
            {
                m.setSimilarity(sim);
                check = true;
            }
            if(!m.getRelationship().equals(r))
            {
                m.setRelationship(r);
                check = true;
            }
            if(!m.getStatus().equals(s))
            {
                m.setStatus(s);
                check = true;
            }
            return check;
        }
    }

//    /**
//     * Adds a new Mapping to the Alignment if it is non-redundant
//     * Otherwise, updates the similarity of the already present Mapping
//     * to the maximum similarity of the two redundant Mappings
//     * @param sourceURI: the URI of the source class to add to the Alignment
//     * @param targetURI: the URI of the target class to add to the Alignment
//     * @param sim: the similarity between the classes
//     */
//    public boolean add(String sourceURI, String targetURI, double sim)
//    {
//        return add(sourceURI,targetURI,sim,MappingRelation.EQUIVALENCE,MappingStatus.UNKNOWN);
//    }


    public boolean add(int id1, int id2, double sim, MappingRelation r, MappingStatus s)
    {
        if(id1 == -1 || id2 == -1)
            return false;
        if(source.contains(id1) && target.contains(id2))
            return addNonRedundant(id1,id2,sim,r,s);
        else if(source.contains(id2) && target.contains(id1))
            return addNonRedundant(id2,id1,sim,r,s);
        return false;
    }

    @Override
    public boolean add(Mapping m)
    {
        int sourceId = m.getSourceId();
        int targetId = m.getTargetId();
        double sim = m.getSimilarity();
        MappingRelation r = m.getRelationship();
        Mapping clone = new Mapping(m);
        //Unless the Alignment is internal, we can't have a mapping
        //involving entities that exist in both ontologies (they are
        //the same entity, and therefore shouldn't map with other
        //entities in either ontology)
        if(!internal && (source.contains(targetId) || target.contains(sourceId)))
            return false;

        //If it isn't listed yet, add it
        if(!sourceMaps.contains(sourceId,targetId))
        {
            return updateMapping(sourceId, targetId, clone);
        }
        //Otherwise update the similarity
        else
        {
            return updateSimilarity(sourceId, targetId, sim, r);
        }
    }

    private boolean updateSimilarity(int sourceId, int targetId, double sim, MappingRelation r) {
        Mapping m;
        m = sourceMaps.get(sourceId,targetId);
        boolean check = false;
        if(m.getSimilarity() < sim)
        {
            m.setSimilarity(sim);
            check = true;
        }
        if(!m.getRelationship().equals(r))
        {
            m.setRelationship(r);
            check = true;
        }
        return check;
    }

    @Override
    public boolean addAll(Collection<? extends Mapping> a)
    {
        boolean check = false;
        for(Mapping m : a)
            check = add(m) || check;
        return check;
    }


    /**
     * @return the average cardinality of this Alignment
     */
    public double cardinality()
    {
        double cardinality = 0.0;

        Set<Integer> sources = sourceMaps.keySet();
        for(Integer i : sources)
            cardinality += sourceMaps.keySet(i).size();

        Set<Integer> targets = targetMaps.keySet();
        for(Integer i : targets)
            cardinality += targetMaps.keySet(i).size();
        cardinality /= sources.size() + targets.size();

        return cardinality;
    }

    @Override
    public void clear()
    {
        maps = new Vector<>(0,1);
        sourceMaps = new Table2Map<>();
        targetMaps = new Table2Map<>();
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @param targetId: the index of the target class to check in the Alignment
     * @param r: the MappingRelation to check in the Alignment
     * @return whether the Alignment contains a Mapping between sourceId and targetId
     * with relationship r
     */
    public boolean contains(int sourceId, int targetId, MappingRelation r)
    {
        return sourceMaps.contains(sourceId, targetId) &&
                getRelationship(sourceId,targetId).equals(r);
    }

    @Override
    public boolean contains(Object o)
    {
        return o instanceof Mapping && contains(((Mapping)o).getSourceId(),
                ((Mapping)o).getTargetId(), ((Mapping)o).getRelationship());
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for(Object o : c)
            if(!contains(o))
                return false;
        return true;
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @param targetId: the index of the target class to check in the Alignment
     * @return whether the Alignment contains a Mapping between sourceId and targetId
     */
    public boolean containsMapping(int sourceId, int targetId)
    {
        return sourceMaps.contains(sourceId, targetId);
    }

    /**
     * @param m: the Mapping to check in the Alignment
     * @return whether the Alignment contains a Mapping with the same sourceId
     * and targetId as m (regardless of the mapping relation)
     */
    public boolean containsMapping(Mapping m)
    {
        return sourceMaps.contains(m.getSourceId(), m.getTargetId());
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @return whether the Alignment contains a Mapping for sourceId
     */
    public boolean containsSource(int sourceId)
    {
        return sourceMaps.contains(sourceId);
    }

    /**
     * @param targetId: the index of the target class to check in the Alignment
     * @return whether the Alignment contains a Mapping for targetId
     */
    public boolean containsTarget(int targetId)
    {
        return targetMaps.contains(targetId);
    }


    @Override
    public boolean equals(Object o)
    {
        return o instanceof Alignment && containsAll((Alignment)o);
    }

    /**
     * @param ref: the reference Alignment to evaluate this Alignment
     * @return the evaluation of this Alignment {# correct mappings, # conflict mappings}
     */
    public int[] evaluate(Alignment ref)
    {
        int[] count = new int[2];
        for(Mapping m : maps)
        {
            if(ref.contains(m))
            {
                count[0]++;
                m.setStatus(MappingStatus.CORRECT);
            }
            else if(ref.contains(m.getSourceId(),m.getTargetId(),MappingRelation.UNKNOWN))
            {
                count[1]++;
                m.setStatus(MappingStatus.UNKNOWN);
            }
            else
                m.setStatus(MappingStatus.INCORRECT);
        }
        return count;
    }

    /**
     * @param a: the base Alignment to which this Alignment will be compared
     * @return the gain (i.e. the fraction of new Mappings) of this Alignment
     * in comparison with the base Alignment
     */
    public double gain(Alignment a)
    {
        double gain = 0.0;
        for(Mapping m : maps)
            if(!a.containsMapping(m))
                gain++;
        gain /= a.size();
        return gain;
    }

    /**
     * @param a: the base Alignment to which this Alignment will be compared
     * @return the gain (i.e. the fraction of new Mappings) of this Alignment
     * in comparison with the base Alignment
     */
    public double gainOneToOne(Alignment a)
    {
        double sourceGain = 0.0;
        Set<Integer> sources = sourceMaps.keySet();
        for(Integer i : sources)
            if(!a.containsSource(i))
                sourceGain++;
        sourceGain /= a.sourceCount();
        double targetGain = 0.0;
        Set<Integer> targets = targetMaps.keySet();
        for(Integer i : targets)
            if(!a.containsTarget(i))
                targetGain++;
        targetGain /= a.targetCount();
        return Math.min(sourceGain, targetGain);
    }

    /**
     * @param index: the index of the Mapping to return in the list of Mappings
     * @return the Mapping at the input index (note that the index will change
     * during sorting) or null if the index falls outside the list
     */
    public Mapping get(int index)
    {
        if(index < 0 || index >= maps.size())
            return null;
        return maps.get(index);
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @param targetId: the index of the target class to check in the Alignment
     * @return the Mapping between the source and target classes or null if no
     * such Mapping exists
     */
    public Mapping get(int sourceId, int targetId)
    {
        return sourceMaps.get(sourceId, targetId);
    }

    /**
     * @param id1: the index of the first class to check in the Alignment
     * @return the Mapping between the classes or null if no such Mapping exists
     * in either direction
     */
    public Mapping getBidirectional(int id1, int id2)
    {
        if(sourceMaps.contains(id1, id2))
            return sourceMaps.get(id1, id2);
        else if(sourceMaps.contains(id2, id1))
            return  sourceMaps.get(id2, id1);
        else
            return null;
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @return the index of the target class that best matches source
     */
    public int getBestSourceMatch(int sourceId)
    {
        double max = 0;
        int target = -1;
        Set<Integer> targets = sourceMaps.keySet(sourceId);
        for(Integer i : targets)
        {
            double sim = getSimilarity(sourceId,i);
            if(sim > max)
            {
                max = sim;
                target = i;
            }
        }
        return target;
    }

    /**
     * @param targetId: the index of the target class to check in the Alignment
     * @return the index of the source class that best matches target
     */
    public int getBestTargetMatch(int targetId)
    {
        double max = 0;
        int source = -1;
        Set<Integer> sources = sourceMaps.keySet(targetId);
        for(Integer i : sources)
        {
            double sim = getSimilarity(i,targetId);
            if(sim > max)
            {
                max = sim;
                source = i;
            }
        }
        return source;
    }

    /**
     * @param sourceId: the index of the source class
     * @param targetId: the index of the target class
     * @return the index of the Mapping between the given classes in
     * the list of Mappings, or -1 if the Mapping doesn't exist
     */
    public int getIndex(int sourceId, int targetId)
    {
        if(sourceMaps.contains(sourceId, targetId))
            return maps.indexOf(sourceMaps.get(sourceId, targetId));
        else
            return -1;
    }

    /**
     * @param id1: the index of the first class
     * @param id2: the index of the second class
     * @return the index of the Mapping between the given classes in
     * the list of Mappings (in any order), or -1 if the Mapping doesn't exist
     */
    public int getIndexBidirectional(int id1, int id2)
    {
        if(sourceMaps.contains(id1, id2))
            return maps.indexOf(sourceMaps.get(id1, id2));
        else if(targetMaps.contains(id1, id2))
            return maps.indexOf(targetMaps.get(id1, id2));
        else
            return -1;
    }

    /**
     * @param id: the index of the class to check in the Alignment
     * @return the list of all classes mapped to the given class
     */
    public Set<Integer> getMappingsBidirectional(int id)
    {
        HashSet<Integer> mappings = new HashSet<Integer>();
        if(sourceMaps.contains(id))
            mappings.addAll(sourceMaps.keySet(id));
        if(targetMaps.contains(id))
            mappings.addAll(targetMaps.keySet(id));
        return mappings;
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @return the index of the target class that best matches source
     */
    public double getMaxSourceSim(int sourceId)
    {
        double max = 0;
        Set<Integer> targets = sourceMaps.keySet(sourceId);
        for(Integer i : targets)
        {
            double sim = getSimilarity(sourceId,i);
            if(sim > max)
                max = sim;
        }
        return max;
    }

    /**
     * @param targetId: the index of the target class to check in the Alignment
     * @return the index of the source class that best matches target
     */
    public double getMaxTargetSim(int targetId)
    {
        double max = 0;
        Set<Integer> sources = targetMaps.keySet(targetId);
        for(Integer i : sources)
        {
            double sim = getSimilarity(i,targetId);
            if(sim > max)
                max = sim;
        }
        return max;
    }

    /**
     * @param sourceId: the index of the source class in the Alignment
     * @param targetId: the index of the target class in the Alignment
     * @return the mapping relationship between source and target
     */
    public MappingRelation getRelationship(int sourceId, int targetId)
    {
        Mapping m = sourceMaps.get(sourceId, targetId);
        if(m == null)
            return null;
        return m.getRelationship();
    }

    /**
     * @param sourceId: the index of the source class in the Alignment
     * @param targetId: the index of the target class in the Alignment
     * @return the similarity between source and target
     */
    public double getSimilarity(int sourceId, int targetId)
    {
        Mapping m = sourceMaps.get(sourceId, targetId);
        if(m == null)
            return 0.0;
        return m.getSimilarity();
    }

    /**
     * @param sourceId: the index of the source class in the Alignment
     * @param targetId: the index of the target class in the Alignment
     * @return the similarity between source and target in percentage
     */
    public String getSimilarityPercent(int sourceId, int targetId)
    {
        Mapping m = sourceMaps.get(sourceId, targetId);
        if(m == null)
            return "0%";
        return m.getSimilarityPercent();
    }

    /**
     * @param sourceId: the index of the source class to check in the Alignment
     * @return the list of all target classes mapped to the source class
     */
    public Set<Integer> getSourceMappings(int sourceId)
    {
        if(sourceMaps.contains(sourceId))
            return sourceMaps.keySet(sourceId);
        return new HashSet<>();
    }

    /**
     * @return the list of all source classes that have mappings
     */
    public Set<Integer> getSources()
    {
        HashSet<Integer> sMaps = new HashSet<Integer>();
        sMaps.addAll(sourceMaps.keySet());
        return sMaps;
    }

    /**
     * @param targetId: the index of the target class to check in the Alignment
     * @return the list of all source classes mapped to the target class
     */
    public Set<Integer> getTargetMappings(int targetId)
    {
        if(targetMaps.contains(targetId))
            return targetMaps.keySet(targetId);
        return new HashSet<Integer>();
    }

    /**
     * @return the list of all target classes that have mappings
     */
    public Set<Integer> getTargets()
    {
        HashSet<Integer> tMaps = new HashSet<Integer>();
        tMaps.addAll(targetMaps.keySet());
        return tMaps;
    }

    @Override
    public int hashCode()
    {
        return maps.hashCode();
    }

    @Override
    public boolean isEmpty()
    {
        return maps.isEmpty();
    }

    @Override
    public Iterator<Mapping> iterator()
    {
        return maps.iterator();
    }


    @Override
    public boolean remove(Object o)
    {
        if(o instanceof Mapping && contains(o))
        {
            Mapping m = (Mapping)o;
            int sourceId = m.getSourceId();
            int targetId = m.getTargetId();
            sourceMaps.remove(sourceId, targetId);
            targetMaps.remove(targetId, sourceId);
            maps.remove(m);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean check = false;
        for(Object o : c)
            check = remove(o) || check;
        return check;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean check = false;
        for(Mapping m : this)
            if(!c.contains(m))
                check = remove(m) || check;
        return check;
    }

    @Override
    public int size()
    {
        return maps.size();
    }

    /**
     * Sorts the Alignment ascendingly
     */
    public void sortAscending()
    {
        Collections.sort(maps);
    }

    /**
     * Sorts the Alignment descendingly
     */
    public void sortDescending()
    {
        Collections.sort(maps,new Comparator<Mapping>()
        {
            //Sorting in descending order can be done simply by
            //reversing the order of the elements in the comparison
            public int compare(Mapping m1, Mapping m2)
            {
                return m2.compareTo(m1);
            }
        } );
    }

    /**
     * @return the number of source classes mapped in this Alignment
     */
    public int sourceCount()
    {
        return sourceMaps.keyCount();
    }

    /**
     * @return the number of target classes mapped in this Alignment
     */
    public int targetCount()
    {
        return targetMaps.keyCount();
    }

    @Override
    public Object[] toArray()
    {
        return maps.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return maps.toArray(a);
    }

//Private Methods

//    private void loadMappingsTSV(String file) throws Exception
//    {
//        BufferedReader inStream = new BufferedReader(new FileReader(file));
//        //First line contains the reference to AML
//        inStream.readLine();
//        //Second line contains the source ontology
//        inStream.readLine();
//        //Third line contains the target ontology
//        inStream.readLine();
//        //Fourth line contains the headers
//        inStream.readLine();
//        //And from the fifth line forward we have mappings
//        String line;
//        while((line = inStream.readLine()) != null)
//        {
//            String[] col = line.split("\t");
//            //First column contains the source uri
//            String sourceURI = col[0];
//            //Third contains the target uri
//            String targetURI = col[2];
//            //Fifth contains the similarity
//            String measure = col[4];
//            //Parse it, assuming 1 if a valid measure is not found
//            double similarity = 1;
//            if(measure != null)
//            {
//                try
//                {
//                    similarity = Double.parseDouble(measure);
//                    if(similarity < 0 || similarity > 1)
//                        similarity = 1;
//                }
//                catch(Exception ex){/*Do nothing - use the default value*/};
//            }
//            //The sixth column contains the type of relation
//            MappingRelation rel;
//            if(col.length > 5)
//                rel = MappingRelation.parseRelation(col[5]);
//                //For compatibility with previous tsv format without listed relation
//            else
//                rel = MappingRelation.EQUIVALENCE;
//            //The seventh column, if it exists, contains the status of the Mapping
//            MappingStatus st;
//            if(col.length > 6)
//                st = MappingStatus.parseStatus(col[6]);
//                //For compatibility with previous tsv format without listed relation
//            else
//                st = MappingStatus.UNKNOWN;
//            add(sourceURI, targetURI, similarity, rel, st);
//        }
//        inStream.close();
//    }
}