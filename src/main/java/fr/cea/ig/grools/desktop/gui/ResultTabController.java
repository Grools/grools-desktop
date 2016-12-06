package fr.cea.ig.grools.desktop.gui;

import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

import fr.cea.ig.grools.reasoner.Reasoner;


import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class ResultTabController {
    
    
    @NonNull @Getter
    private final Stage resultTabStage;
    
    @NonNull @Getter
    private final Reasoner reasoner;
    
    private final Tab                                   tab;
    private final TreeTableView<PriorKnowledgeRow>          resultTableView;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnName;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnDescription;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnExpectation;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnApproximatedExpectation;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnPrediction;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnApproximatedPrediction;
    private final TreeTableColumn<PriorKnowledgeRow,String> tableColumnConclusion;
    //private final TableColumn<PriorKnowledgeRow,String> tableColumnLeafStatistics;
    //private final ObservableList<PriorKnowledgeRow>     topPriorKnowledge;
    private final TreeItem<PriorKnowledgeRow> rootNode;
    private final Set<TreeItem<PriorKnowledgeRow>> items;


    private static TreeItem<PriorKnowledgeRow> priorKnowledgeToTreeItem( @NonNull final PriorKnowledge pk ){
        final PriorKnowledgeRow.PriorKnowledgeRowBuilder pkr = PriorKnowledgeRow.builder();
        pkr.name( pk.getName() );
        pkr.description( pk.getDescription() );
        pkr.expectation( pk.getExpectation().toString() );
        pkr.approximatedExpectation( Reasoner.expectationToTruthValueSet( pk.getExpectation( ) ).toString( ) );
        pkr.prediction( pk.getPrediction().toString() );
        pkr.approximatedPrediction( Reasoner.predictionToTruthValueSet( pk.getPrediction() ).toString() );
        pkr.conclusion( pk.getConclusion().toString() );
        return new TreeItem<>( pkr.build() );
    }

    private EventHandler<TreeItem.TreeModificationEvent<PriorKnowledgeRow>> getBranchExpandedEventHandler( ){
        return event -> {
            final TreeItem<PriorKnowledgeRow>                   ti          = event.getSource( );
            final ObservableList<TreeItem<PriorKnowledgeRow>>   tiChildren  = ti.getChildren();
            if( tiChildren != null ){
                tiChildren.forEach( tiChild -> {
                    final PriorKnowledgeRow pkr = tiChild.getValue( );
                    final PriorKnowledge    pk = reasoner.getPriorKnowledge( pkr.name );
                    final Set< Relation >   relations = reasoner.getRelationsWithTarget( pk  );
                    final Set< TreeItem< PriorKnowledgeRow > > subchildren = relations.stream( )
                                                                                      .filter(  relation -> relation.getSource( ) instanceof PriorKnowledge )
                                                                                      .map(     relation -> priorKnowledgeToTreeItem( ( PriorKnowledge ) relation.getSource( ) ) )
                                                                                      .collect( Collectors.toSet( ) );
                    tiChild.addEventHandler( TreeItem.branchExpandedEvent( ), getBranchExpandedEventHandler( ) );
                } );
            }
        };
    }

    public ResultTabController( @NonNull final Stage resultTabStage, @NonNull final TabPane tabPane, @NonNull final Reasoner reasoner ) {
        this.resultTabStage                     = resultTabStage;
        this.reasoner                           = reasoner;
        this.tab                                = new Tab( );
        this.resultTableView                    = new TreeTableView<>(  );
        this.tableColumnName                    = new TreeTableColumn<>( "Name" );
        this.tableColumnDescription             = new TreeTableColumn<>( "Description" );
        this.tableColumnExpectation             = new TreeTableColumn<>( "Expectation" );
        this.tableColumnApproximatedExpectation = new TreeTableColumn<>( "Approximated Expectation" );
        this.tableColumnPrediction              = new TreeTableColumn<>( "Prediction" );
        this.tableColumnApproximatedPrediction  = new TreeTableColumn<>( "Approximated Prediction" );
        this.tableColumnConclusion              = new TreeTableColumn<>( "Conclusion" );
        //this.tableColumnLeafStatistics  = new TableColumn<>( "Leaf Statistics" );
        this.rootNode                           = new TreeItem<>();
        this.items                              = reasoner.getTopsPriorKnowledges( )
                                                          .stream()
                                                          .filter( pk -> (reasoner.getRelationsWithTarget( pk )).size() > 0 )
                                                          .map( pk -> {
                                                              final TreeItem<PriorKnowledgeRow>         ti      = priorKnowledgeToTreeItem( pk );
                                                              final List<TreeItem<PriorKnowledgeRow>>   children= reasoner.getRelationsWithTarget( pk )
                                                                                                                          .stream()
                                                                                                                          .filter( relation -> relation.getSource( ) instanceof PriorKnowledge )
                                                                                                                          .map( relation -> priorKnowledgeToTreeItem( ( PriorKnowledge ) relation.getSource( ) ) )
                                                                                                                          .collect( Collectors.toList() );
                                                              ti.getChildren().addAll( children );
                                                              ti.addEventHandler( TreeItem.branchExpandedEvent(), getBranchExpandedEventHandler() );
//                                                              ti.addEventHandler( TreeItem.branchExpandedEvent(), event -> {
//                                                                  ti.getChildren().stream().forEach(  child -> {
//                                                                      final PriorKnowledgeRow   pkr         = child.getValue();
//                                                                      final List<TreeItem<PriorKnowledgeRow>> subchildren = reasoner.getRelationsWithTarget( reasoner.getConcept( pkr.name ) )
//                                                                                                                      .stream()
//                                                                                                                      .filter( relation -> relation.getSource( ) instanceof PriorKnowledge )
//                                                                                                                      .map( relation -> priorKnowledgeToTreeItem( ( PriorKnowledge ) relation.getSource( ) ) )
//                                                                                                                      .collect( Collectors.toList() );
//                                                                      System.out.println("ok" );
//                                                                      ti.getChildren().addAll( subchildren );
//                                                                  } );
//                                                              } );
                                                              return ti;
                                                          } )
                                                          .collect( Collectors.toSet( ) );
        
        tableColumnName.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.name );
        } );
        tableColumnDescription.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.description );
        });
        tableColumnExpectation.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.expectation );
        });
        tableColumnApproximatedExpectation.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.approximatedExpectation );
        });
        tableColumnPrediction.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.prediction );
        });
        tableColumnApproximatedPrediction.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.approximatedPrediction );
        });
        tableColumnConclusion.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.conclusion );
        });
        //tableColumnLeafStatistics.setCellValueFactory(  new PropertyValueFactory<>( "leaf_statistics" ) );


//        topPriorKnowledge.addAll( reasoner.getTopsPriorKnowledges()
//                                          .stream()
//                                          .filter( pk -> (reasoner.getRelationsWithTarget( pk )).size() > 0 )
//                                          .map( pk -> {
//                                                    final PriorKnowledgeRow.PriorKnowledgeRowBuilder pkr = PriorKnowledgeRow.builder();
//                                                    pkr.name( pk.getName() );
//                                                    pkr.description( pk.getDescription() );
//                                                    pkr.expectation( pk.getExpectation().toString() );
//                                                    pkr.approximatedExpectation( Reasoner.expectationToTruthValueSet( pk.getExpectation( ) ).toString( ) );
//                                                    pkr.prediction( pk.getPrediction().toString() );
//                                                    pkr.approximatedPrediction( Reasoner.predictionToTruthValueSet( pk.getPrediction() ).toString() );
//                                                    pkr.conclusion( pk.getConclusion().toString() );
//                                                    return  pkr.build();
//                                                } )
//                                          .collect( Collectors.toList( ) ) );

        //resultTableView.setItems( topPriorKnowledge );

        rootNode.getChildren().addAll( items );

        resultTableView.getColumns().setAll( tableColumnName, tableColumnDescription, tableColumnExpectation, tableColumnApproximatedExpectation, tableColumnPrediction, tableColumnApproximatedPrediction, tableColumnConclusion );
        resultTableView.setRoot( rootNode );
        resultTableView.setShowRoot( false );
        tab.setContent( resultTableView );
        tabPane.getTabs().add( tab );
        resultTabStage.show();
    }
}
