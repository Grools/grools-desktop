package fr.cea.ig.grools.desktop.gui;


import fr.cea.ig.grools.desktop.Tools;
import fr.cea.ig.grools.fact.Concept;
import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import fr.cea.ig.grools.logic.Conclusion;
import fr.cea.ig.grools.reasoner.ConceptGraph;
import fr.cea.ig.grools.reasoner.Mode;
import fr.cea.ig.grools.reasoner.Reasoner;
import fr.cea.ig.grools.reasoner.ReasonerImpl;
import fr.cea.ig.grools.reasoner.Verbosity;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ViewController implements Initializable {
    @Getter
    private final Stage primaryStage;
    
    
    @FXML
    private Pane main;
    
    @FXML
    private MenuBar menuBar;
    
    @FXML
    private Menu menuFile;
    
    // Load sub-menu
    @FXML
    private Menu menuLoad;
    
    @FXML
    private MenuItem menuLoadObservations;
    
    @FXML
    private MenuItem menuLoadGROOLS_dump;
    // end of sub-menu
    
    @FXML
    private MenuItem menuItemClose;
    
    // -- End of menu file --
    
    @FXML
    private Menu menuAnalyze;
    
    @FXML
    private MenuItem menuItemStartReasoning;
    
    // -- End of menu analyze --
    
    @FXML
    private Menu menuHelp;
    
    @FXML
    private MenuItem menuItemPreference;
    
    @FXML
    private MenuItem menuItemAbout;
    
    
    // -- End of menu help --
    
    @FXML
    private SplitPane mainSlpitPane;
    
    @FXML
    private Pane paneRight;

    @FXML @Getter
    private Menu filterPrediction;

    @FXML @Getter
    private Menu filterExpectation;

    @FXML @Getter
    private Menu filterConclusion;

    @FXML @Getter
    private TabPane tabPane;

    @FXML @Getter
    private TextField filterNameAndDescription;
    // -- End of paneRight --
    
    @FXML
    private Pane paneLeft;
    
    @FXML
    private Label labelRightStatus;
    
    @FXML
    private Label labelLeftStatus;

    private final Map< String, ResultTabContainer > tabContainerMap = new HashMap<>(  );


    private static Map<String,Number> subGraphStat( @NonNull final Concept concept, @NonNull final Reasoner reasoner ){
        final Set<Relation> relations = reasoner.getSubGraph( concept );
        final Set<PriorKnowledge> priorKnowledges = relations.stream()
                                                             .map( rel -> Arrays.asList( rel.getSource( ), rel.getTarget( ) ) )
                                                             .flatMap( Collection::stream )
                                                             .filter(    c -> c instanceof PriorKnowledge )
                                                             .map(       c -> (PriorKnowledge)c )
                                                             .collect( Collectors.toSet() );
        final Map<String,Number> stats = new TreeMap<>(  );

        final Set<PriorKnowledge> sources = relations.stream( )
                                                     .filter( relation -> relation.getSource() instanceof PriorKnowledge )
                                                     .filter( relation -> relation.getTarget() instanceof PriorKnowledge )
                                                     .map(    relation -> ( PriorKnowledge ) relation.getSource() )
                                                     .collect( Collectors.toSet( ) );


        stats.put( "nb concepts", priorKnowledges.size( ) );

        final Set<PriorKnowledge> targets = relations.stream( )
                                                     .filter( relation -> relation.getSource() instanceof PriorKnowledge )
                                                     .filter( relation -> relation.getTarget() instanceof PriorKnowledge )
                                                     .map(    relation -> ( PriorKnowledge ) relation.getTarget() )
                                                     .collect( Collectors.toSet( ) );

        final Set<PriorKnowledge> leaves = priorKnowledges.stream()
                                                          .filter( pk -> sources.contains( pk ) )
                                                          .filter( pk -> !targets.contains( pk ) )
                                                          .collect( Collectors.toSet( ) );


        stats.put( "nb leaf concepts", leaves.size( ) );
        Map<Conclusion, Long> conclusionsStats = leaves.stream( )
                                                       .map( leaf -> leaf.getConclusion() )
                                                       .collect(Collectors.groupingBy( Function.identity( ), Collectors.counting( ) ) );
        conclusionsStats.forEach( (k,v) -> stats.put( k.toString(), v ) );

        return stats;
    }
    
    private static Reasoner loadGROOLS_Dump( @NonNull final File file ) {
    Reasoner              reasoner = null;
    final FileInputStream fis;
    try {
        fis = new FileInputStream( file );
        final ObjectInputStream ois              = new ObjectInputStream( fis );
        final boolean           hasBeenProcessed = ois.readBoolean( );
        final Mode              mode             = ( Mode ) ois.readObject( );
        final Verbosity         verbosity        = ( Verbosity ) ois.readObject( );
        final ConceptGraph      graph            = ( ConceptGraph ) ois.readObject( );
        reasoner = new ReasonerImpl( graph, mode, verbosity, hasBeenProcessed );
    }
    catch ( ClassNotFoundException e ) {
        System.err.println( e.getMessage( ) );
        System.exit( 1 );
    }
    catch ( FileNotFoundException e ) {
        System.err.println( "File: " + file.toString( ) + "not found" );
        System.exit( 1 );
    }
    catch ( IOException e ) {
        System.err.println( "Can not read/write into " + file.toString( ) );
        System.exit( 1 );
    }
    return reasoner;
}
    public ViewController( @NonNull final Stage primaryStage ) {
        this.primaryStage = primaryStage;
    }
    
    
    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize( URL fxmlFileLocation, ResourceBundle resources ) {
        assert menuItemAbout != null : "fx:id=\"menuItemAbout\" was not injected: check your FXML file 'main.fxml'.";
        assert mainSlpitPane != null : "fx:id=\"mainSlpitPane\" was not injected: check your FXML file 'main.fxml'.";
        assert menuFile != null : "fx:id=\"menuFile\" was not injected: check your FXML file 'main.fxml'.";
        assert paneLeft != null : "fx:id=\"paneLeft\" was not injected: check your FXML file 'main.fxml'.";
        assert paneRight != null : "fx:id=\"paneRight\" was not injected: check your FXML file 'main.fxml'.";
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'main.fxml'.";
        assert menuItemStartReasoning != null : "fx:id=\"menuItemStartReasoning\" was not injected: check your FXML file 'main.fxml'.";
        assert menuLoadGROOLS_dump != null : "fx:id=\"menuLoadGROOLS_dump\" was not injected: check your FXML file 'main.fxml'.";
        assert labelLeftStatus != null : "fx:id=\"labelLeftStatus\" was not injected: check your FXML file 'main.fxml'.";
        assert menuAnalyze != null : "fx:id=\"menuAnalyze\" was not injected: check your FXML file 'main.fxml'.";
        assert menuItemPreference != null : "fx:id=\"menuItemPreference\" was not injected: check your FXML file 'main.fxml'.";
        assert menuLoad != null : "fx:id=\"menuLoad\" was not injected: check your FXML file 'main.fxml'.";
        assert menuLoadObservations != null : "fx:id=\"menuLoadObservations\" was not injected: check your FXML file 'main.fxml'.";
        assert labelRightStatus != null : "fx:id=\"labelRightStatus\" was not injected: check your FXML file 'main.fxml'.";
        assert menuItemClose != null : "fx:id=\"menuItemClose\" was not injected: check your FXML file 'main.fxml'.";
        assert menuHelp != null : "fx:id=\"menuHelp\" was not injected: check your FXML file 'main.fxml'.";

//        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
//        mainSlpitPane.prefHeightProperty().bind(primaryStage.heightProperty());

        // It is important to call it before adding ChangeListener to the tabPane to avoid NPE and
        // to be able fire the manual selection event below. Otherwise the 1st tab will be selected
        // with empty content.
        tabPane.getSelectionModel().clearSelection();


        menuLoadGROOLS_dump.setOnAction( event -> {
            final FileChooser fileChooser = new FileChooser();
            // Set extension filter
            final FileChooser.ExtensionFilter groolsFilter =  new FileChooser.ExtensionFilter("GROOLS files (*.grools)", "*.grools");
            fileChooser.getExtensionFilters()
                       .add(groolsFilter);
            fileChooser.setTitle( "Select GROOLS dump file" );
            final File                              groolsDump          = fileChooser.showOpenDialog( primaryStage );
            final Reasoner                          reasoner            = loadGROOLS_Dump( groolsDump );
            final ResultTabContainer                resultTabController = new ResultTabContainer( primaryStage, tabPane, reasoner );
            final Tab                               selectedTab         = resultTabController.getTab();
            final TreeTableView<PriorKnowledgeRow>  tableView           = resultTabController.getTableView();
            tabContainerMap.put( resultTabController.getTab().getId(), resultTabController  );
            tabPane.getSelectionModel().select( selectedTab );

            final TreeTableView.TreeTableViewSelectionModel< PriorKnowledgeRow > model = tableView.getSelectionModel( );
            model.selectedItemProperty().addListener( (obs, oldSelection, newSelection) -> {
                final ObservableList< TreeItem< PriorKnowledgeRow > > selectedTreeItems = model.getSelectedItems( );
                if( selectedTreeItems != null ){
                    final Map< String, Map< String, Number > > stats = selectedTreeItems.stream( )
                                                                                       .map( ti -> ti.getValue( ).getName( ) )
                                                                                       .map( name -> reasoner.getConcept( name ) )
                                                                                       .collect( HashMap< String, Map< String, Number > >::new,
                                                                                             ( m, c ) -> m.put( c.getName( ), subGraphStat( c, reasoner ) ),
                                                                                             ( m, u ) -> {
                                                                                        } );
                        paneLeft.getChildren( ).clear( );
                        final VBox vBoxRoot = new VBox( );
                        vBoxRoot.setFillWidth( true );
                        for ( final Map.Entry< String, Map< String, Number > > entry : stats.entrySet( ) ) {
                            final HBox headerBox = new HBox( );
                            final Label conceptLabel = new Label( entry.getKey( ) );
                            headerBox.setFillHeight( true );
                            conceptLabel.setPrefWidth( Control.USE_COMPUTED_SIZE );
                            conceptLabel.setPrefHeight( Control.USE_COMPUTED_SIZE );
                            headerBox.getChildren().add( conceptLabel );
                            HBox.setHgrow( conceptLabel, Priority.ALWAYS );
                            vBoxRoot.getChildren( ).add(headerBox);
                            vBoxRoot.getChildren( ).add( conceptLabel );
                            for ( final Map.Entry< String, Number > statistics : entry.getValue( ).entrySet( ) ) {
                                final HBox statBox = new HBox( );
                                statBox.setSpacing(10);
                                VBox.setMargin( statBox, new Insets( 10, 10, 10, 10 ) );
                                final TextField statField  = new TextField(  statistics.getKey( ) + ": " + statistics.getValue( ).toString( ) );
                                statField.setEditable(false);
                                statField.getStyleClass().add("copyable-label");
                                HBox.setHgrow( statField, Priority.ALWAYS );
                                statBox.getChildren( ).setAll( statField );
                                vBoxRoot.getChildren( ).add( statBox );
                            }
                        }
                        paneLeft.getChildren( ).clear();
                        paneLeft.getChildren( ).add( vBoxRoot );
                        vBoxRoot.prefWidthProperty().bind( paneLeft.widthProperty() );
                }
            }  );
            //tabPane.getSelectionModel().selectLast();
        } );
        menuItemPreference.setOnAction( event -> {
            final Stage                preferenceStage = new Stage();
            final PreferenceController controller      = new PreferenceController( preferenceStage );
            final Pane                 preferencePane  = Tools.loadFXML( "preference.fxml" , controller );
            preferenceStage.setTitle( "Preference" );
            preferenceStage.setScene(new Scene( preferencePane, preferencePane.getPrefWidth(), preferencePane.getPrefHeight() ) );
            preferenceStage.show();
            //((Node)(event.getSource())).getScene().getWindow().hide();
        } );
        menuItemStartReasoning.setOnAction( event -> {
            final Stage                        analyzeDialogBoxStage = new Stage( StageStyle.UTILITY );
            final AnalyzeDialogueBoxController controller            = new AnalyzeDialogueBoxController( analyzeDialogBoxStage );
            final Pane                         analyzeDialogBoxPane  = Tools.loadFXML( "analyze_dialog_box.fxml" , controller );
            analyzeDialogBoxStage.initModality( Modality.APPLICATION_MODAL );
            analyzeDialogBoxStage.setTitle( "Configuration" );
            analyzeDialogBoxStage.setScene(new Scene( analyzeDialogBoxPane, analyzeDialogBoxPane.getPrefWidth(), analyzeDialogBoxPane.getPrefHeight() ) );
            analyzeDialogBoxStage.setAlwaysOnTop( true );
            analyzeDialogBoxStage.setResizable(false);
            analyzeDialogBoxStage.toFront();
            analyzeDialogBoxStage.showAndWait();
        } );
        menuItemClose.setOnAction( event -> { primaryStage.close(); } );


    }
}
