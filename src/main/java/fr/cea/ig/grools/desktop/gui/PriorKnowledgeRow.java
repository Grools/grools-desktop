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
    @Override
    public String toString(){
        return name + ": "                      + description   + " "
                    + "expectation: "           + expectation   + " " + approximatedExpectation + " "
                    + "prediction: "            + prediction    + " " + approximatedPrediction  + " "
                    + "conclusion: "            + conclusion    + " " + approximatedPrediction;
    }

    public String toJson(){
        return "{"+
                    name+ ": \""                      + description   + "\"\n"
                        + "expectation: \""           + expectation   + " " + approximatedExpectation + "\"\n"
                        + "prediction: \""            + prediction    + " " + approximatedPrediction  + "\"\n"
                        + "conclusion: \""            + conclusion    + " " + approximatedPrediction  + "\"\n" +
                "}";
    }
}
