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
        final Set<? extends Cell> layer = concepts.stream()
                                                  .filter( cell -> layers.stream()
                                                                         .noneMatch( cells -> cells.contains( cell ) ) )
                                                  .collect( Collectors.toSet( ) );
        if( ! layer.isEmpty() )
            layers.add( layer );
        final Set<? extends Cell> children = concepts.stream()
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
        int i = 0;
        final int margin = 10;
        for( Set<? extends Cell> layer : layers){
            final String cssClass = "layer-"+ Integer.toString( i );
            final int y = ( i * 50 ) + margin; // 50 is cell height normally or use a max size finder
            int j = 0;
            for( final Cell cell: layer){
                final int x = ( j * 50 ) + margin; // 50 is cell width normally or use a max size finder
                cell.relocate( x,y );
                cell.getStyleClass().add( cssClass );
            }
            i++;
        }

    }
}
