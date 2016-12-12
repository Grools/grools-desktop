package fr.cea.ig.grools.desktop.gui;

import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import fr.cea.ig.grools.logic.TruthValueSet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import fr.cea.ig.grools.reasoner.Reasoner;


import lombok.Getter;
import lombok.NonNull;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    
    private String lastValue;
    
    private final TreeItem<PriorKnowledgeRow> rootNode;
    private final TabPane tabPane;
    private static TreeItem<PriorKnowledgeRow> priorKnowledgeToTreeItem( @NonNull final PriorKnowledge pk ){
        final PriorKnowledgeRow.PriorKnowledgeRowBuilder pkr = PriorKnowledgeRow.builder();
        pkr.name( pk.getName() );
        pkr.description( pk.getDescription() );
        pkr.expectation( pk.getExpectation().toString() );
        pkr.approximatedExpectation( truthValueToTextMenu( Reasoner.expectationToTruthValueSet( pk.getExpectation( ) ).toString( ) ) );
        pkr.prediction( pk.getPrediction().toString() );
        pkr.approximatedPrediction( truthValueToTextMenu( Reasoner.predictionToTruthValueSet( pk.getPrediction() ).toString() ) );
        pkr.conclusion( pk.getConclusion().toString() );
        return new TreeItem<>( pkr.build() );
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
    
    private static Set<String> getSelectedMenu( @NonNull final Menu menu ){
        return menu.getItems()
                   .stream()
                   .filter(    i   -> i instanceof CheckMenuItem )
                   .map(       i   -> (CheckMenuItem)i )
                   .filter(    cmi -> cmi.isSelected() )
                   .map(       cmi -> cmi.getText() )
                   .collect( Collectors.toSet( ) );
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
    
    private Set<TreeItem<PriorKnowledgeRow>> getDefaultItems(){
        return reasoner.getTopsPriorKnowledges( )
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
                           ti.addEventHandler( TreeItem.branchExpandedEvent(), getBranchExpandedEventHandler() ); // lazy load
                           return ti;
                       } )
                       .collect( Collectors.toSet( ) );
    }
    
    private Set<PriorKnowledge> getConstrainedPriorKnowledge( @NonNull final PriorKnowledge pk, @NonNull final Set<String> predictions, @NonNull final Set<String> expectations, @NonNull final Set<String> conclusions, @NonNull final String toFind) {
        boolean hasMatched = false;
        final Set<PriorKnowledge> result = new HashSet<>(  );
        if(! predictions.isEmpty() )
            hasMatched = predictions.stream().anyMatch( tv -> Reasoner.predictionToTruthValueSet( pk.getPrediction( ) ).toString().equals( tv ) );
        if(! expectations.isEmpty() )
            hasMatched = expectations.stream().anyMatch( tv -> Reasoner.expectationToTruthValueSet( pk.getExpectation( ) ).toString().equals( tv ) );
        if(! conclusions.isEmpty() )
            hasMatched = conclusions.stream().anyMatch( c -> pk.getConclusion().toString().equals( c ) );
        if( ! toFind.isEmpty() )
            hasMatched = pk.getName().contains( toFind ) || pk.getDescription().contains( toFind );
        if( hasMatched )
            result.add( pk ) ;
        else{
            reasoner.getRelationsWithTarget( pk )
                    .stream()
                    .filter( relation -> relation.getSource() instanceof PriorKnowledge )
                    .map( relation -> (PriorKnowledge)relation.getSource() )
                    .map( priorKnowledge -> getConstrainedPriorKnowledge(priorKnowledge, predictions, expectations, conclusions, toFind) )
                    .flatMap( Collection::stream )
                    .collect( Collectors.toCollection( () -> result ) );
            
        }
        return result;
    }
    
    private Set<TreeItem<PriorKnowledgeRow>> getFilteredItems( @NonNull final Set<String> predictions, @NonNull final Set<String> expectations, @NonNull final Set<String> conclusions, @NonNull final String toFind){
        return reasoner.getTopsPriorKnowledges().stream()
                       .map( pk -> getConstrainedPriorKnowledge(pk, predictions, expectations, conclusions, toFind) )
                       .flatMap( Collection::stream )
                       .map( pk -> {
                           final TreeItem<PriorKnowledgeRow>         ti      = priorKnowledgeToTreeItem( pk );
                           final List<TreeItem<PriorKnowledgeRow>>   children= reasoner.getRelationsWithTarget( pk )
                                                                                       .stream()
                                                                                       .filter( relation -> relation.getSource( ) instanceof PriorKnowledge )
                                                                                       .map( relation -> priorKnowledgeToTreeItem( ( PriorKnowledge ) relation.getSource( ) ) )
                                                                                       .collect( Collectors.toList() );
                           ti.getChildren().addAll( children );
                           ti.addEventHandler( TreeItem.branchExpandedEvent(), getBranchExpandedEventHandler() ); // lazy load
                           return ti;
                       } )
                       .collect( Collectors.toSet() );
    }
    
    private Set<TreeItem<PriorKnowledgeRow>> getItems(){
        final Set<String> predictions   = getSelectedMenu( filterPrediction ).stream()
                                                                             .map( ResultTabContainer::textMenuToTruthValue )
                                                                             .map( tv -> tv.toString() )
                                                                             .collect( Collectors.toSet() );
        final Set<String> expectations  = getSelectedMenu( filterExpectation ).stream()
                                                                              .map( ResultTabContainer::textMenuToTruthValue )
                                                                              .map( tv -> tv.toString() )
                                                                              .collect( Collectors.toSet() );;
        final Set<String> conclusions   = getSelectedMenu( filterConclusion );
        final String toFind             = filterNameAndDescription.getText();
        final Set<TreeItem<PriorKnowledgeRow>> results;
        if( ! predictions.isEmpty() || !expectations.isEmpty() || ! conclusions.isEmpty() || ! toFind.isEmpty() )
            results = getFilteredItems( predictions, expectations, conclusions, toFind );
        else
            results = getDefaultItems();
        return results;
    }

    private void filterEvent( @NonNull final String newValue){
        final Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    
        sleeper.setOnSucceeded( event -> {
            if( lastValue == newValue ) { // Check if latest query as the same reference like this only the last query is run (within window of 1 sec)
//                System.out.println( newValue );
                final Set<TreeItem<PriorKnowledgeRow>> new_items = getItems( );
                rootNode.getChildren( ).setAll( new_items );
            }
        } );
        new Thread(sleeper).start();
    }
    
    private static TruthValueSet textMenuToTruthValue( @NonNull final String text ){
        final TruthValueSet result;
        switch( text ){
            case "True"    : result = TruthValueSet.T;break;
            case "False"   : result = TruthValueSet.F;break;
            case "Both"         : result = TruthValueSet.B;break;
            case "None"         :
            default:
                result = TruthValueSet.N;break;
        }
        return result;
    }
    
    private static String truthValueToTextMenu( @NonNull final String truthValue ){
        final String text;
        switch( truthValue ){
            case "{t}"      : text = "True";break;
            case "{f}"      : text = "False";break;
            case "{t,f}"    : text = "Both";break;
            case "{∅}"      :
            default:
                text = "None";break;
        }
        return text;
    }
    
    public ResultTabContainer( @NonNull final Stage resultTabStage, @NonNull final TabPane tabPane, @NonNull final Reasoner reasoner ) {
        this.id                 = counter.incrementAndGet( );
        this.resultTabStage     = resultTabStage;
        this.tabPane            = tabPane;
        this.reasoner           = reasoner;
        this.tab                = new Tab( "global results-"+Long.toString( id ) );
        this.rootNode           = new TreeItem<>();
        tab.setId( "global-results-tab-"+Long.toString( id ) );
        //resultTabStage.show();
    }
    
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
    
        final Set<TreeItem<PriorKnowledgeRow>> items = getItems();
        
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
    
        filterNameAndDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            lastValue = newValue;
            filterEvent( newValue );
        });
        filterPrediction.getItems().filtered( menuItem -> menuItem instanceof CheckMenuItem ).forEach( cmi -> cmi.setOnAction( event -> {
//            final TruthValueSet tv = textMenuToTruthValue(cmi.getText() );
            lastValue = cmi.getText();
            filterEvent( cmi.getText() );
        } )) ;
        filterExpectation.getItems().filtered( menuItem -> menuItem instanceof CheckMenuItem ).forEach( cmi -> cmi.setOnAction( event -> {
//            final TruthValueSet tv = textMenuToTruthValue(cmi.getText() );
            lastValue = cmi.getText();
            filterEvent( cmi.getText() );
        } )) ;
        filterConclusion.getItems().filtered( menuItem -> menuItem instanceof CheckMenuItem ).forEach( cmi -> cmi.setOnAction( event -> {
            lastValue = cmi.getText().toString();
            filterEvent( cmi.getText().toString() );
        } )) ;
        rootNode.getChildren().setAll( items );
        
        tableView.getColumns( )
                 .setAll( columnName, columnDescription, columnExpectation, columnApproximatedExpectation, columnPrediction, columnApproximatedPrediction, columnConclusion );
        tableView.setRoot( rootNode );
        tableView.setShowRoot( false );
    
        //TODO add event
        MenuItem menuVisualize          = new MenuItem( "Visualize");
        MenuItem menuCopy               = new MenuItem( "Copy");
        MenuItem menuAddAnExpectation   = new MenuItem( "Add an expectation");
        MenuItem menuRemoveAnExpectation= new MenuItem( "Remove an expectation");
        MenuItem menuAddAPrediction     = new MenuItem( "Add a prediction");
        MenuItem menuRemoveAPrediction  = new MenuItem( "Remove a prediction");

        menuCopy.setOnAction( event -> {
            final ObservableList< TreeTablePosition< PriorKnowledgeRow, ? > > observableList = tableView.getSelectionModel( ).getSelectedCells( );
            final String toCopy = observableList.stream()
                                                .map( tablepos -> {
                                                    final int col = tablepos.getColumn();
                                                    final int row = tablepos.getRow();
                                                    return (String)tableView.getColumns().get( col ).getCellData( row );
                                                } )
                                                .filter( obj -> obj != null )
                                                .collect( Collectors.joining(" ") );
            final ClipboardContent content = new ClipboardContent();
            content.putString( toCopy );
            Clipboard.getSystemClipboard( ).setContent( content );
        } );

        tableView.setContextMenu( new ContextMenu(menuVisualize, menuCopy,menuAddAnExpectation,menuRemoveAnExpectation, menuAddAPrediction, menuRemoveAPrediction) );
        
        tableView.getSelectionModel().clearSelection();
        //scrollPane.setContent( tableView );
        tab.setContent( scrollPane );
        tabPane.getTabs().add( tab );
        
    }
    
}
