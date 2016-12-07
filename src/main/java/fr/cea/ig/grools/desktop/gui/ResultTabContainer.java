package fr.cea.ig.grools.desktop.gui;

import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import fr.cea.ig.grools.reasoner.Reasoner;


import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class ResultTabContainer {
    private static final AtomicInteger counter = new AtomicInteger( );

    @Getter
    private final long id;
    
    @NonNull @Getter
    private final Stage resultTabStage;
    
    @NonNull @Getter
    private final Reasoner reasoner;

    @NonNull @Getter
    private final Tab                                       tab;
    @NonNull @Getter
    private final TreeTableView<PriorKnowledgeRow>          tableView;
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
                    tiChild.getChildren().setAll( subchildren );
                    tiChild.addEventHandler( TreeItem.branchExpandedEvent( ), getBranchExpandedEventHandler( ) );
                } );
            }
        };
    }
    
    private static void makeHeaderWrappable(TreeTableColumn col) {
        final Label label = new Label( col.getText( ));
        label.setStyle("-fx-padding: 8px;");
        label.setWrapText(true);
        label.setAlignment( Pos.CENTER );
        label.setTextAlignment( TextAlignment.CENTER );
    
        final StackPane stack = new StackPane();
        stack.getChildren().add(label);
        stack.prefWidthProperty().bind(col.widthProperty().subtract(5));
        label.prefWidthProperty().bind(stack.prefWidthProperty());
        col.setGraphic(stack);
    }

    public ResultTabContainer( @NonNull final Stage resultTabStage, @NonNull final TabPane tabPane, @NonNull final Reasoner reasoner ) {
        this.id                                 = counter.incrementAndGet( );
        this.resultTabStage                     = resultTabStage;
        this.reasoner                           = reasoner;
        this.tab                                = new Tab( );
        this.tableView = new TreeTableView<>(  );
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
                                                              return ti;
                                                          } )
                                                          .collect( Collectors.toSet( ) );

        final int widthColumnName                   = 150;
        final int widthColumnDescription            = 400;
        final int widthColumnTruthValue             = 100;
        final int widthColumnApproximatedTruthValue = 150;
        final int widthColumnConclusion             = 200;

//        tableView.getStylesheets().
        makeHeaderWrappable(tableColumnApproximatedExpectation);
        makeHeaderWrappable(tableColumnApproximatedPrediction);
        tableColumnExpectation.setStyle( "-fx-alignment: CENTER;");
        tableColumnApproximatedExpectation.setStyle( "-fx-alignment: CENTER;");
        tableColumnPrediction.setStyle( "-fx-alignment: CENTER;");
        tableColumnApproximatedPrediction.setStyle( "-fx-alignment: CENTER;");


        tableColumnName.setMinWidth( widthColumnName );
        tableColumnName.setMaxWidth( widthColumnName );
        tableColumnName.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.name );
        } );

        tableColumnDescription.setMinWidth( widthColumnDescription );
        resultTabStage.getScene().widthProperty().addListener(  event  -> {
            final double widthTotal =  tableColumnName.getWidth()
                                        + tableColumnDescription.getWidth()
                                        + tableColumnExpectation.getWidth()
                                        + tableColumnApproximatedExpectation.getWidth()
                                        + tableColumnPrediction.getWidth()
                                        + tableColumnApproximatedPrediction.getWidth()
                                        + tableColumnConclusion.getWidth();
            final double widthTable = tableView.getWidth( );
            final double widthTmp = widthTable - widthTotal;
            if( widthTmp > 0){
                tableColumnDescription.setPrefWidth( widthColumnDescription + widthTmp );
            }
            else{
                tableColumnDescription.setPrefWidth( widthColumnDescription );
            }
        } );
//        tableColumnDescription.setMaxWidth( 400 );
        tableColumnDescription.setCellValueFactory( p -> {
            final PriorKnowledgeRow              pkr  = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.description );
        });

        tableColumnDescription.setCellFactory( c -> {
            final TreeTableCell<PriorKnowledgeRow,String> cell = new TreeTableCell<>();
            final Text text = new Text(  );
            text.wrappingWidthProperty().bind(tableColumnDescription.widthProperty());
            cell.setGraphic( text );text.textProperty().bind(cell.itemProperty());
            return cell;
        } );

        tableColumnExpectation.setMinWidth( widthColumnTruthValue );
        tableColumnExpectation.setMaxWidth( widthColumnTruthValue );
        tableColumnExpectation.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.expectation );
        });

        tableColumnApproximatedExpectation.setMinWidth( widthColumnApproximatedTruthValue );
        tableColumnApproximatedExpectation.setMaxWidth( widthColumnApproximatedTruthValue );
        tableColumnApproximatedExpectation.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.approximatedExpectation );
        });

        tableColumnPrediction.setMinWidth( widthColumnTruthValue );
        tableColumnPrediction.setMaxWidth( widthColumnTruthValue );
        tableColumnPrediction.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.prediction );
        });

        tableColumnApproximatedPrediction.setMinWidth( widthColumnApproximatedTruthValue );
        tableColumnApproximatedPrediction.setMaxWidth( widthColumnApproximatedTruthValue );
        tableColumnApproximatedPrediction.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.approximatedPrediction );
        });

        tableColumnConclusion.setMinWidth( widthColumnConclusion );
        tableColumnConclusion.setMaxWidth( widthColumnConclusion );
        tableColumnConclusion.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.conclusion );
        });

        rootNode.getChildren().addAll( items );

        tableView.getColumns( )
                 .setAll( tableColumnName, tableColumnDescription, tableColumnExpectation, tableColumnApproximatedExpectation, tableColumnPrediction, tableColumnApproximatedPrediction, tableColumnConclusion );
        tableView.setRoot( rootNode );
        tableView.setShowRoot( false );
        tab.setContent( tableView );
        tabPane.getTabs().add( tab );
        resultTabStage.show();
    }
}
