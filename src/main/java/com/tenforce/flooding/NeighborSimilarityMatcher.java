package com.tenforce.flooding;

import com.tenforce.flooding.enums.NeighborSimilarityStrategy;
import com.tenforce.flooding.structs.RelationshipMap;
import com.tenforce.flooding.structs.Table2Set;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NeighborSimilarityMatcher {

    //Links to ontology data structures
    private RelationshipMap rels;
    private Alignment input;
    private NeighborSimilarityStrategy strategy;
    private boolean direct;
    //The available CPU threads
    private int threads;

    public NeighborSimilarityMatcher(RelationshipMap relationshipMap, boolean directNeighbors)
    {
        rels = relationshipMap;
        strategy = NeighborSimilarityStrategy.ANCESTORS;
        direct = directNeighbors;
        threads = Runtime.getRuntime().availableProcessors();
    }

//Public Methods
    public Alignment extendAlignment(URIMap uriMap, Alignment a, double threshold)
    {
//        System.out.println("Extending Alignment with Neighbor Similarity Matcher");
        long time = System.currentTimeMillis()/1000;
        input = a;
        Table2Set<Integer,Integer> toMap = new Table2Set<>();
        for(int i = 0; i < input.size(); i++)
        {
            Mapping m = input.get(i);
            if(!uriMap.isClass(m.getSourceId()))
                continue;
            Set<Integer> sourceSubClasses = rels.getSubClasses(m.getSourceId(),true);
            Set<Integer> targetSubClasses = rels.getSubClasses(m.getTargetId(),true);
            for(Integer s : sourceSubClasses)
            {
                addTomap(toMap, targetSubClasses, s);
            }
            Set<Integer> sourceSuperClasses = rels.getSuperClasses(m.getSourceId(),true);
            Set<Integer> targetSuperClasses = rels.getSuperClasses(m.getTargetId(),true);
            for(Integer s : sourceSuperClasses)
            {
                addTomap(toMap, targetSuperClasses, s);
            }
        }
        Alignment maps = mapInParallel(a,toMap,threshold);
        time = System.currentTimeMillis()/1000 - time;
//        System.out.println("Finished in " + time + " seconds");
        return maps;
    }

    public Alignment rematch(Taxonomy2Match source, Taxonomy2Match target, Alignment a, double threshold)
    {
//        System.out.println("Computing Neighbor Similarity");
        long time = System.currentTimeMillis()/1000;
        input = a;
        Alignment maps = new Alignment(source,target);
        Table2Set<Integer,Integer> toMap = new Table2Set<>();
        for(Mapping m : a)
        {
            int sId = m.getSourceId();
            int tId = m.getTargetId();

            if (sId==tId){
//                System.out.println(String.format("Warning input mapping contains %s -> %s",sId,tId));
            } else {
                toMap.add(sId, tId);
            }

        }
        maps.addAll(mapInParallel(maps,toMap,threshold));
        time = System.currentTimeMillis()/1000 - time;
//        System.out.println("Finished in " + time + " seconds");
        return maps;
    }


    private void addTomap(Table2Set<Integer, Integer> toMap, Set<Integer> targetSubClasses, Integer s) {
        if(input.containsSource(s))
            return;
        for(Integer t : targetSubClasses)
        {
            if(input.containsTarget(t))
                continue;
            toMap.add(s, t);
        }
    }
//Private Methods

    //Maps a table of classes in parallel, using all available threads
    private Alignment mapInParallel(Alignment maps, Table2Set<Integer,Integer> toMap, double thresh)
    {
        ArrayList<MappingTask> tasks = new ArrayList<>();
        for(Integer i : toMap.keySet()){
            for(Integer j : toMap.get(i)){
                tasks.add(new MappingTask(i,j));
            }
        }
        List<Future<Mapping>> results;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        try
        {
            results = exec.invokeAll(tasks);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            results = new ArrayList<>();
        }
        exec.shutdown();
        for(Future<Mapping> fm : results)
        {
            try
            {
                Mapping m = fm.get();
                if(m.getSimilarity() >= thresh)
                    maps.add(m);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return maps;
    }

    //Computes the neighbor structural similarity between two terms by
    //checking for mappings between all their ancestors and descendants
    private double mapTwoTerms(int sId, int tId)
    {
        if (sId==tId){
            return 0;
        }

        double parentSim = 0.0;
        double childrenSim = 0.0;
        if(!strategy.equals(NeighborSimilarityStrategy.DESCENDANTS))
        {
            double parentTotal = 0.0;
            Set<Integer> sourceParents = rels.getSuperClasses(sId,direct);
            Set<Integer> targetParents = rels.getSuperClasses(tId,direct);
            for(Integer i : sourceParents)
            {
                parentTotal += 0.5 / rels.getDistance(sId,i);
                for(Integer j : targetParents)
                    parentSim += input.getSimilarity(i,j) /
                            Math.sqrt(rels.getDistance(sId,i) * rels.getDistance(tId, j));
            }
            for(Integer i : targetParents)
                parentTotal += 0.5 / rels.getDistance(tId,i);
            parentSim /= parentTotal;
        }
        if(!strategy.equals(NeighborSimilarityStrategy.ANCESTORS))
        {
            double childrenTotal = 0.0;
            Set<Integer> sourceChildren = rels.getSubClasses(sId,direct);
            Set<Integer> targetChildren = rels.getSubClasses(tId,direct);
            for(Integer i : sourceChildren)
            {
                childrenTotal += 0.5 / rels.getDistance(i,sId);
                for(Integer j : targetChildren)
                    childrenSim += input.getSimilarity(i,j) /
                            Math.sqrt(rels.getDistance(i,sId) * rels.getDistance(j,tId));
            }
            for(Integer i : targetChildren)
                childrenTotal += 0.5 / rels.getDistance(i,tId);
            childrenSim /= childrenTotal;
        }
        if(strategy.equals(NeighborSimilarityStrategy.ANCESTORS))
            return parentSim;
        else if(strategy.equals(NeighborSimilarityStrategy.DESCENDANTS))
            return childrenSim;
        else if(strategy.equals(NeighborSimilarityStrategy.MINIMUM))
            return Math.min(parentSim,childrenSim);
        else if(strategy.equals(NeighborSimilarityStrategy.MAXIMUM))
            return Math.max(parentSim,childrenSim);
        else
            return (parentSim + childrenSim)*0.5;
    }

    //Callable class for mapping two classes
    private class MappingTask implements Callable<Mapping>
    {
        private int source;
        private int target;

        MappingTask(int s, int t)
        {
            source = s;
            target = t;
        }

        @Override
        public Mapping call()
        {
            double similarity =mapTwoTerms(source,target);
//            System.out.println(source+" -> "+target+" "+similarity);
            return new Mapping(source,target,similarity);
        }
    }

}
