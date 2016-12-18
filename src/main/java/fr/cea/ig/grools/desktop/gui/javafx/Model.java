package fr.cea.ig.grools.desktop.gui.javafx;

import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Model {

    private final Cell graphParent;

    @NonNull @Getter
    private final Set<Cell> allCells;

    @NonNull @Getter
    private final Set<Cell> addedCells;

    @NonNull @Getter
    private final Set<Cell> removedCells;


    @NonNull @Getter
    private final Set<Edge> allEdges;

    @NonNull @Getter
    private final Set<Edge> addedEdges;

    @NonNull @Getter
    private final Set<Edge> removedEdges;
    
    private final Map<String,Cell> cellMap; // <id,cell>



    public Model() {
        graphParent     = new Cell( "_ROOT_");
        allCells        = new HashSet<>();
        addedCells      = new HashSet<>();
        removedCells    = new HashSet<>();
        allEdges        = new HashSet<>();
        addedEdges      = new HashSet<>();
        removedEdges    = new HashSet<>();
        cellMap         = new HashMap<>(  );
    }

    public void clear() {

        allCells.clear();
        addedCells.clear();
        removedCells.clear();

        allEdges.clear();
        addedEdges.clear();
        removedEdges.clear();

        cellMap.clear();

    }

    public void clearAddedLists() {
        addedCells.clear();
        addedEdges.clear();
    }

    public void addCell(@NonNull final String id, @NonNull final CellType type) {

        switch (type) {

            case PRIORKNOWLEDGE:
                addCell(  new PriorKnowledgeCell( id ) );
                break;

            case COMPUTATION:
                addCell( new ComputationCell( id ) );
                break;

            case CURATION:
                addCell( new CurationCell( id ) );
                break;

            case EXPERIMENTATION:
                addCell( new ExperimentationCell( id ) );
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    private void addCell( @NonNull final Cell cell) {

        addedCells.add(cell);

        cellMap.put( cell.getCellId(), cell);

    }

    public void addEdge( @NonNull final String sourceId, @NonNull final String targetId) {

        final Cell sourceCell = cellMap.get( sourceId );
        final Cell targetCell = cellMap.get( targetId );

        final Edge edge = new Edge( sourceCell, targetCell);
        
        addedEdges.add( edge );
    }

    public <T extends Cell> Set<T> getCells( @NonNull final Class<T> type ){
        final Set<T> results = allCells.stream()
                                       .filter( type::isInstance )
                                       .map( type::cast )
                                       .collect( Collectors.toSet( ) );
        return addedCells.stream()
                         .filter( type::isInstance )
                         .map( type::cast )
                         .collect( (Collectors.toCollection(() -> results) ) );
    }

    public <T extends Cell> Set<Edge> getEdgesWithTargetType( @NonNull final Class<T> type ){
        final Set<Edge> results = allEdges.stream()
                                       .filter( edge -> type.isInstance( edge.getTarget() ) )
                                       .collect( Collectors.toSet( ) );
        return addedEdges.stream()
                         .filter(edge -> type.isInstance( edge.getTarget() ) )
                         .collect( (Collectors.toCollection(() -> results) ) );
    }

    public <T extends Cell> Set<Edge> getEdgesWithSourceType( @NonNull final Class<T> type ){
        final Set<Edge> results = allEdges.stream()
                                       .filter( edge -> type.isInstance( edge.getSource() ) )
                                       .collect( Collectors.toSet( ) );
        addedEdges.stream()
                  .filter(edge -> type.isInstance( edge.getSource() ) )
                  .collect( (Collectors.toCollection(() -> results) ) ); //TODO remove
        results.removeAll( removedEdges );
        return results;
    }

    public <T extends Cell> Set<T> getTopCells( @NonNull final Class<T> type ){
        final Set<Edge> edges = getEdgesWithSourceType(type); // not top
        final Set<Cell> sources = edges.stream().map( Edge::getSource ).collect( Collectors.toSet( ) );
        return getEdgesWithTargetType( type ).stream()
                                             .filter( edge ->! sources.contains( edge.getTarget() )  )
                                             .map( edge -> type.cast( edge.getTarget() ) )
                                             .collect( Collectors.toSet( ) );
    }

    public Set<Edge> getEdges( @NonNull final Cell source, @NonNull final Cell target ){
        final Set<Edge> results = allEdges.stream()
                                         .filter( edge -> edge.getSource() == source )
                                         .filter( edge -> edge.getTarget() == target )
                                         .collect( Collectors.toSet( ) );
        addedEdges.stream()
                  .filter( edge -> edge.getSource() == source )
                  .filter( edge -> edge.getTarget() == target )
                  .collect( Collectors.toCollection( () -> results ) );
        results.removeAll( removedEdges );
        return results;
    }

    /**
     * Attach all cells which don't have a parent to graphParent
     * @param cellList
     */
    public void attachOrphansToGraphParent( @NonNull final Set<Cell> cellList ) {

        cellList.stream()
                .filter( cell -> cell.getCellParents().size() == 0  )
                .forEach( graphParent::addCellChild );


    }

    /**
     * Remove the graphParent reference if it is set
     * @param cellList
     */
    public void disconnectFromGraphParent( @NonNull final Set<Cell> cellList) {
        cellList.forEach( graphParent::removeCellChild );
    }

    public void merge() {

        // cells
        allCells.addAll( addedCells);
        allCells.removeAll( removedCells);

        addedCells.clear();
        removedCells.clear();

        // edges
        allEdges.addAll( addedEdges);
        allEdges.removeAll( removedEdges);

        addedEdges.clear();
        removedEdges.clear();
        //TODO retract and insert corresponding concept from reasoner

    }
}