package com.tenforce.esco.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeltaRecord {

    private String query;

    private ArrayList<Delta> delta = new ArrayList<>();

    public DeltaRecord(){}

    /**
     * Collect affected URIs.
     */
    public Set<String> getAffectedURIs(String graph) {
        Set<String> uris = new HashSet<>();

        for (Delta d: delta) {
            if(d.graph.equals(graph) && d.type.equals("effective")) {
                for (Delta.Triple triple : d.inserts) {
                    uris.add(triple.s.value);
                }
                for (Delta.Triple triple : d.deletes) {
                    uris.add(triple.s.value);
                }
            }
        }
        return uris;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDelta(ArrayList<Delta> delta) {
        this.delta = delta;
    }

    @Override
    public String toString() {
        return "DeltaRecord{" +
                "query='" + query + '\'' +
                ", delta=" + delta +
                '}';
    }


    public static class Delta {
        private String type;

        private String graph;

        private ArrayList<Triple> inserts = new ArrayList<>();

        private ArrayList<Triple> deletes = new ArrayList<>();

        public Delta(){}

        public void setType(String type) {
            this.type = type;
        }

        public void setGraph(String graph) {
            this.graph = graph;
        }

        public void setInserts(ArrayList<Triple> inserts) {
            this.inserts = inserts;
        }

        public void setDeletes(ArrayList<Triple> deletes) {
            this.deletes = deletes;
        }

        @Override
        public String toString() {
            return "Delta{" +
                    "type='" + type + '\'' +
                    ", graph='" + graph + '\'' +
                    ", inserts=" + inserts +
                    ", deletes=" + deletes +
                    '}';
        }

        public static class Triple {
            private Term s, p, o;

            public Triple(){}

            public void setS(Term s) {
                this.s = s;
            }

            public void setP(Term p) {
                this.p = p;
            }

            public void setO(Term o) {
                this.o = o;
            }

            @Override
            public String toString() {
                return "(" + s + ", " + p + ", " + o + ")";
            }

            public static class Term {
                private String value, type;

                public Term(){}

                public void setValue(String value) {
                    this.value = value;
                }

                public void setType(String type) {
                    this.type = type;
                }

                @Override
                public String toString() {
                    return "Term{" + value + ": " + type + '}';
                }
            }
        }
    }
}
