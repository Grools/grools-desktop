package fr.cea.ig.grools.desktop.gui.javafx;

import fr.cea.ig.grools.fact.Concept;
import fr.cea.ig.grools.fact.Observation;
import fr.cea.ig.grools.fact.ObservationType;
import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import fr.cea.ig.grools.reasoner.Mode;
import fr.cea.ig.grools.reasoner.Reasoner;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VisualizationTab implements TabContainer{
    private static final AtomicInteger counter = new AtomicInteger( );

    @Getter
    private final long      id;

    @NonNull
    @Getter
    private final Tab       tab;
    
    @Getter
    private final TabPane   tabPane;
    
    @Getter
    private final Stage stage;
    
    private final Reasoner reasoner;
    
    private final Set<Concept> roots;
    
    private final Graph graph;
    
    private final Set<Relation> relations;
    private final Set<Concept> concepts;
    
    public VisualizationTab( @NonNull final Stage stage, @NonNull final TabPane tabPane, @NonNull final Reasoner reasoner, @NonNull final Set<Concept> roots){
        this.id         = counter.incrementAndGet( );
        this.stage      = stage;
        this.tabPane    = tabPane;
        this.reasoner   = reasoner;
        this.roots      = roots;
        this.tab        = new Tab( "visualization-"+roots.stream().map( r -> r.getName() ).collect( Collectors.joining("-") ) );
        this.graph      = new Graph();
        this.relations  = roots.stream( )
                               .map( reasoner::getSubGraph )
                               .flatMap( Collection::stream )
                               .collect( Collectors.toSet( ) );
        this.concepts   = relations.stream( )
                                   .map( rel -> Arrays.asList( rel.getSource( ), rel.getTarget( ) ) )
                                   .flatMap( Collection::stream )
                                   .collect( Collectors.toSet( ) );
        tab.setId( "visualization-"+Long.toString( id )  );
        final Model model = graph.getModel();
        graph.beginUpdate();
        concepts.forEach( concept -> {
            // change these if else ... statements by a visitor pattern ?
            if( concept instanceof PriorKnowledge )
                model.addCell( concept.getName(), CellType.PRIORKNOWLEDGE );
            else if( concept instanceof Observation ){
                final Observation observation =  ( ( Observation ) concept );
                if( observation.getType() == ObservationType.COMPUTATION )
                    model.addCell( concept.getName(), CellType.COMPUTATION );
                else if( observation.getType() == ObservationType.CURATION )
                    model.addCell( concept.getName(), CellType.CURATION );
                else if( observation.getType() == ObservationType.EXPERIMENTATION )
                    model.addCell( concept.getName(), CellType.EXPERIMENTATION );
                else
                    System.err.println( "Unsupported observation type: "+  observation.getType());
            }
            else
                System.err.println( "Unsupported concept type: "+  concept.getClass().getName() );
        } );
        relations.forEach( r -> model.addEdge( r.getSource().getName(), r.getTarget().getName() ) );
        graph.endUpdate();
        final Layout layout = new HierarchicalLayout( graph );
        layout.execute();
        tab.setContent( graph.getScrollPane() );
        tabPane.getTabs().add( tab );
        System.out.println("done" );
    }
}
