package fr.cea.ig.grools.desktop.gui;

import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import fr.cea.ig.grools.reasoner.Reasoner;


import lombok.Getter;
import lombok.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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
    
    @FXML
    private ResourceBundle resources;
    
    @FXML
    private URL location;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML @Getter
    private TextField filterNameAndDescription;
    
    
    @FXML @Getter
    private Menu filterPrediction;
    
    @FXML @Getter
    private Menu filterExpectation;
    
    @FXML @Getter
    private Menu filterConclusion;
    
    @FXML @Getter
    private TreeTableView<PriorKnowledgeRow>          tableView;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnName;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnDescription;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnExpectation;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnApproximatedExpectation;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnPrediction;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnApproximatedPrediction;
    
    @FXML
    private TreeTableColumn<PriorKnowledgeRow,String> columnConclusion;
    
    private final TreeItem<PriorKnowledgeRow> rootNode;
    private final Set<TreeItem<PriorKnowledgeRow>> items;
    private final TabPane tabPane;
    
    @FXML
    void initialize() {
        assert scrollPane != null : "fx:id=\"scrollPane\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert filterNameAndDescription != null : "fx:id=\"scrollPane\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert filterPrediction != null : "fx:id=\"scrofilterPredictionllPane\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert filterExpectation != null : "fx:id=\"filterExpectation\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert filterConclusion != null : "fx:id=\"filterConclusion\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnApproximatedExpectation != null : "fx:id=\"columnApproximatedExpectation\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnPrediction != null : "fx:id=\"columnPrediction\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnApproximatedPrediction != null : "fx:id=\"columnApproximatedPrediction\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert tableView != null : "fx:id=\"tableView\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnDescription != null : "fx:id=\"columnDescription\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnExpectation != null : "fx:id=\"columnExpectation\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnConclusion != null : "fx:id=\"columnConclusion\" was not injected: check your FXML file 'tab_results.fxml'.";
        assert columnName != null : "fx:id=\"columnName\" was not injected: check your FXML file 'tab_results.fxml'.";
        final int widthColumnName                   = 150;
        final int widthColumnDescription            = 400;
        final int widthColumnTruthValue             = 100;
        final int widthColumnApproximatedTruthValue = 150;
        final int widthColumnConclusion             = 200;
        
        scrollPane.prefWidthProperty().bind( tabPane.prefWidthProperty() );
        scrollPane.prefHeightProperty().bind( tabPane.prefHeightProperty() );

//        tableView.getStylesheets().
        makeHeaderWrappable(columnApproximatedExpectation);
        makeHeaderWrappable(columnApproximatedPrediction);
        columnExpectation.setStyle( "-fx-alignment: CENTER;");
        columnApproximatedExpectation.setStyle( "-fx-alignment: CENTER;");
        columnPrediction.setStyle( "-fx-alignment: CENTER;");
        columnApproximatedPrediction.setStyle( "-fx-alignment: CENTER;");


        columnName.setMinWidth( widthColumnName );
        columnName.setMaxWidth( widthColumnName );
        columnName.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.name );
        } );

        columnDescription.setMinWidth( widthColumnDescription );
        resultTabStage.getScene().widthProperty().addListener(  event  -> {
            final double widthTotal =  columnName.getWidth()
                                        + columnDescription.getWidth()
                                        + columnExpectation.getWidth()
                                        + columnApproximatedExpectation.getWidth()
                                        + columnPrediction.getWidth()
                                        + columnApproximatedPrediction.getWidth()
                                        + columnConclusion.getWidth();
            final double widthTable = tableView.getWidth( );
            final double widthTmp = widthTable - widthTotal;
            if( widthTmp > 0){
                columnDescription.setPrefWidth( widthColumnDescription + widthTmp );
            }
            else{
                columnDescription.setPrefWidth( widthColumnDescription );
            }
        } );
//        columnDescription.setMaxWidth( 400 );
        columnDescription.setCellValueFactory( p -> {
            final PriorKnowledgeRow              pkr  = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.description );
        });

        columnDescription.setCellFactory( c -> {
            final TreeTableCell<PriorKnowledgeRow,String> cell = new TreeTableCell<>();
            final Text text = new Text(  );
            text.wrappingWidthProperty().bind(columnDescription.widthProperty());
            cell.setGraphic( text );text.textProperty().bind(cell.itemProperty());
            return cell;
        } );

        columnExpectation.setMinWidth( widthColumnTruthValue );
        columnExpectation.setMaxWidth( widthColumnTruthValue );
        columnExpectation.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.expectation );
        });

        columnApproximatedExpectation.setMinWidth( widthColumnApproximatedTruthValue );
        columnApproximatedExpectation.setMaxWidth( widthColumnApproximatedTruthValue );
        columnApproximatedExpectation.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.approximatedExpectation );
        });

        columnPrediction.setMinWidth( widthColumnTruthValue );
        columnPrediction.setMaxWidth( widthColumnTruthValue );
        columnPrediction.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.prediction );
        });

        columnApproximatedPrediction.setMinWidth( widthColumnApproximatedTruthValue );
        columnApproximatedPrediction.setMaxWidth( widthColumnApproximatedTruthValue );
        columnApproximatedPrediction.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.approximatedPrediction );
        });

        columnConclusion.setMinWidth( widthColumnConclusion );
        columnConclusion.setMaxWidth( widthColumnConclusion );
        columnConclusion.setCellValueFactory( p -> {
            final PriorKnowledgeRow pkr = p.getValue().getValue();
            return new ReadOnlyObjectWrapper<>( pkr.conclusion );
        });

        rootNode.getChildren().addAll( items );

        tableView.getColumns( )
                 .setAll( columnName, columnDescription, columnExpectation, columnApproximatedExpectation, columnPrediction, columnApproximatedPrediction, columnConclusion );
        tableView.setRoot( rootNode );
        tableView.setShowRoot( false );
        tableView.getSelectionModel().clearSelection();
        //scrollPane.setContent( tableView );
        tab.setContent( scrollPane );
        tabPane.getTabs().add( tab );
        
    }

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
        this.id                 = counter.incrementAndGet( );
        this.resultTabStage     = resultTabStage;
        this.tabPane            = tabPane;
        this.reasoner           = reasoner;
        this.tab                = new Tab( "global results-"+Long.toString( id ) );
        this.rootNode           = new TreeItem<>();
        this.items              = reasoner.getTopsPriorKnowledges( )
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

        tab.setId( "global-results-tab-"+Long.toString( id ) );
        //resultTabStage.show();
    }
}
