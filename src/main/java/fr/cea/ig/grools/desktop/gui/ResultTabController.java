package fr.cea.ig.grools.desktop.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import fr.cea.ig.grools.reasoner.Reasoner;


import lombok.Getter;
import lombok.NonNull;

import java.util.stream.Collectors;


public class ResultTabController {
    
    
    @NonNull @Getter
    private final Stage resultTabStage;
    
    @NonNull @Getter
    private final Reasoner reasoner;
    
    private final Tab                                   tab;
    private final TableView<PriorKnowledgeRow>          resultTableView;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnName;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnDescription;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnExpectation;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnApproximatedExpectation;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnPrediction;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnApproximatedPrediction;
    private final TableColumn<PriorKnowledgeRow,String> tableColumnConclusion;
    //private final TableColumn<PriorKnowledgeRow,String> tableColumnLeafStatistics;
    private final ObservableList<PriorKnowledgeRow>     topPriorKnowledge;
    
    
    public ResultTabController( @NonNull final Stage resultTabStage, @NonNull final TabPane tabPane, @NonNull final Reasoner reasoner ) {
        this.resultTabStage                     = resultTabStage;
        this.reasoner                           = reasoner;
        this.tab                                = new Tab( );
        this.resultTableView                    = new TableView<>(  );
        this.tableColumnName                    = new TableColumn<>( "Name" );
        this.tableColumnDescription             = new TableColumn<>( "Description" );
        this.tableColumnExpectation             = new TableColumn<>( "Expectation" );
        this.tableColumnApproximatedExpectation = new TableColumn<>( "Approximated Expectation" );
        this.tableColumnPrediction              = new TableColumn<>( "Prediction" );
        this.tableColumnApproximatedPrediction  = new TableColumn<>( "Approximated Prediction" );
        this.tableColumnConclusion              = new TableColumn<>( "Conclusion" );
        //this.tableColumnLeafStatistics  = new TableColumn<>( "Leaf Statistics" );
        this.topPriorKnowledge          = FXCollections.observableArrayList();
        
        tableColumnName.setCellValueFactory(                        new PropertyValueFactory<>( "name" )                    );
        tableColumnDescription.setCellValueFactory(                 new PropertyValueFactory<>( "description" )             );
        tableColumnExpectation.setCellValueFactory(                 new PropertyValueFactory<>( "expectation" )             );
        tableColumnApproximatedExpectation.setCellValueFactory(     new PropertyValueFactory<>( "approximatedExpectation" ) );
        tableColumnPrediction.setCellValueFactory(                  new PropertyValueFactory<>( "prediction" )              );
        tableColumnApproximatedPrediction.setCellValueFactory(      new PropertyValueFactory<>( "approximatedPrediction" )  );
        tableColumnConclusion.setCellValueFactory(                  new PropertyValueFactory<>( "conclusion" )              );
        //tableColumnLeafStatistics.setCellValueFactory(  new PropertyValueFactory<>( "leaf_statistics" ) );
        
        topPriorKnowledge.addAll( reasoner.getTopsPriorKnowledges()
                                          .stream()
                                          .filter( pk -> (reasoner.getRelationsWithTarget( pk )).size() > 0 )
                                          .map( pk -> {
                                                    final PriorKnowledgeRow.PriorKnowledgeRowBuilder pkr = PriorKnowledgeRow.builder();
                                                    pkr.name( pk.getName() );
                                                    pkr.description( pk.getDescription() );
                                                    pkr.expectation( pk.getExpectation().toString() );
                                                    pkr.approximatedExpectation( Reasoner.expectationToTruthValueSet( pk.getExpectation( ) ).toString( ) );
                                                    pkr.prediction( pk.getPrediction().toString() );
                                                    pkr.approximatedPrediction( Reasoner.predictionToTruthValueSet( pk.getPrediction() ).toString() );
                                                    pkr.conclusion( pk.getConclusion().toString() );
                                                    return  pkr.build();
                                                } )
                                          .collect( Collectors.toList( ) ) );
        resultTableView.setItems( topPriorKnowledge );
        resultTableView.getColumns().addAll( tableColumnName, tableColumnDescription, tableColumnExpectation, tableColumnApproximatedExpectation, tableColumnPrediction, tableColumnApproximatedPrediction, tableColumnConclusion );
        tab.setContent( resultTableView );
        tabPane.getTabs().add( tab );
        resultTabStage.show();
    }
}
