package fr.cea.ig.grools.desktop.gui.javafx;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HierarchicalLayout extends Layout{
    private final Graph graph;

//    private final Reasoner reasoner;

    private final List<Set<? extends Cell>> layers;

    private void getLayers( @NonNull final Set<? extends Cell> concepts ){
        final Set<Cell> layer = concepts.stream()
                                        .filter( cell -> layers.stream()
                                                               .noneMatch( cells -> cells.contains( cell ) ) )
                                        .collect( Collectors.toSet( ) );
        if( ! layer.isEmpty() )
            layers.add( layer );
        final Set<Cell> children = concepts.stream()
                                           .map( Cell::getCellChildren )
                                           .flatMap( Collection::stream )
                                           .collect( Collectors.toSet( ) );
        if( ! children.isEmpty() )
            getLayers( children );

    }

    public HierarchicalLayout( @NonNull final Graph graphData ){
        graph   = graphData;
        layers  = new ArrayList<>(  );
    }

    @Override
    public void execute(){
        graph.beginUpdate();
        final Set<PriorKnowledgeCell >  tops = graph.getModel( )
                                                    .getTopCells( PriorKnowledgeCell.class );
        getLayers( tops );
        //TODO layer and cell positioning http://stackoverflow.com/questions/13861130/graph-hierarchical-layout-algorithm
    }
}
