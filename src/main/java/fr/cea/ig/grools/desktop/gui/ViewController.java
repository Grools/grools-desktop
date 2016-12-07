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
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    private TabPane paneRight;
    
    @FXML
    private Pane paneLeft;
    
    @FXML
    private Label labelRightStatus;
    
    @FXML
    private Label labelLeftStatus;

    private final Map<Tab,ResultTabContainer> tabContainerMap = new HashMap<>(  );


    private static Map<String,Float> subGraphStat( @NonNull final Concept concept, @NonNull final Reasoner reasoner ){
        final Set<Relation> relations = reasoner.getSubGraph( concept );
        final Set<PriorKnowledge> priorKnowledges = relations.stream()
                                                             .map( rel -> Arrays.asList( rel.getSource( ), rel.getTarget( ) ) )
                                                             .flatMap( Collection::stream )
                                                             .filter(    c -> c instanceof PriorKnowledge )
                                                             .map(       c -> (PriorKnowledge)c )
                                                             .collect( Collectors.toSet() );
        final Map<String,Float> stats = new TreeMap<>(  );

        final Set<PriorKnowledge> sources = relations.stream( )
                                                     .filter( relation -> relation.getSource() instanceof PriorKnowledge )
                                                     .filter( relation -> relation.getTarget() instanceof PriorKnowledge )
                                                     .map(    relation -> ( PriorKnowledge ) relation.getSource() )
                                                     .collect( Collectors.toSet( ) );


        stats.put( "nb concepts", ( float ) priorKnowledges.size( ) );

        final Set<PriorKnowledge> targets = relations.stream( )
                                                     .filter( relation -> relation.getSource() instanceof PriorKnowledge )
                                                     .filter( relation -> relation.getTarget() instanceof PriorKnowledge )
                                                     .map(    relation -> ( PriorKnowledge ) relation.getTarget() )
                                                     .collect( Collectors.toSet( ) );

        final Set<PriorKnowledge> leaves = priorKnowledges.stream()
                                                          .filter( pk -> sources.contains( pk ) )
                                                          .filter( pk -> !targets.contains( pk ) )
                                                          .collect( Collectors.toSet( ) );


        stats.put( "nb leaf concepts", ( float ) leaves.size( ) );
        Map<Conclusion, Long> conclusionsStats = leaves.stream( )
                                                       .map( leaf -> leaf.getConclusion() )
                                                       .collect(Collectors.groupingBy( Function.identity( ), Collectors.counting( ) ) );
        conclusionsStats.forEach( (k,v) -> stats.put( k.toString(), v.floatValue() ) );

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
        menuLoadGROOLS_dump.setOnAction( event -> {
            final FileChooser fileChooser = new FileChooser();
            // Set extension filter
            final FileChooser.ExtensionFilter groolsFilter =  new FileChooser.ExtensionFilter("GROOLS files (*.grools)", "*.grools");
            fileChooser.getExtensionFilters()
                       .add(groolsFilter);
            fileChooser.setTitle( "Select GROOLS dump file" );
            final File      groolsDump  = fileChooser.showOpenDialog( primaryStage );
            final Reasoner  reasoner    = loadGROOLS_Dump( groolsDump );
            final ResultTabContainer resultTabController = new ResultTabContainer( primaryStage, paneRight, reasoner );
            tabContainerMap.put( resultTabController.getTab(), resultTabController  );
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

        paneRight.getSelectionModel().selectedItemProperty().addListener( (obs,sourceTab,targetTab) -> {
            final ResultTabContainer result = tabContainerMap.get( targetTab );
            System.out.println("recupere onglet courant" );
            if( result != null ){
                final Reasoner reasoner = result.getReasoner();
                System.out.println( "recupere raisoneur" );
                final TreeTableView.TreeTableViewSelectionModel< PriorKnowledgeRow > model = result.getTableView( ).getSelectionModel( );
                System.out.println( model != null );
                if( model != null ) {
                    model.selectedItemProperty().addListener( (observableValue,oldValue,newValue) ->{
                        System.out.println( "click" );
                        final ObservableList< TreeItem< PriorKnowledgeRow > > selectedItems = model.getSelectedItems( );
                        final Map< String, Map< String, Float > > stats = selectedItems.stream( )
                                                                                       .map( ti -> ti.getValue( ).getName( ) )
                                                                                       .map( name -> reasoner.getConcept( name ) )
                                                                                       .collect( HashMap< String, Map< String, Float > >::new,
                                                                                                 ( m, c ) -> m.put( c.getName( ), subGraphStat( c, reasoner ) ),
                                                                                                 ( m, u ) -> {
                                                                                                 } );
                        paneLeft.getChildren( ).clear( );
                        final VBox vBoxRoot = new VBox( );
                        for ( final Map.Entry< String, Map< String, Float > > entry : stats.entrySet( ) ) {
                            final VBox vBox = new VBox( );
                            final HBox hBox = new HBox( );
                            final Label conceptLabel = new Label( entry.getKey( ) );
                            VBox.setMargin( hBox, new Insets( 10, 10, 10, 10 ) );
                            hBox.getChildren( ).add( conceptLabel );
                            for ( final Map.Entry< String, Float > statistics : entry.getValue( ).entrySet( ) ) {
                                final HBox statBox = new HBox( );
                                final Label statNameLabel = new Label( statistics.getKey( ) );
                                final Label statValueLabel = new Label( statistics.getValue( ).toString( ) );
                                statBox.getChildren( ).setAll( statNameLabel, statValueLabel );
                                hBox.getChildren( ).add( statBox );
                            }
                            vBox.getChildren( ).add( hBox );
                            vBoxRoot.getChildren( ).add( vBox );
                        }
                        paneLeft.getChildren( ).setAll( vBoxRoot );
                    });
                }

            }
        } );

    }
}
