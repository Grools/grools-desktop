package fr.cea.ig.grools.desktop.gui;


import lombok.Builder;
import lombok.Getter;

@Builder
public final class PriorKnowledgeRow {
    
    @Getter
    public String name;
    @Getter 
    public String description;
    @Getter
    public String expectation;
    @Getter
    public String approximatedExpectation;
    @Getter
    public String prediction;
    @Getter
    public String approximatedPrediction;
    @Getter
    public String conclusion;
    @Getter
    public String leafStatistics;
    
}
