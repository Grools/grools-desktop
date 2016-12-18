package fr.cea.ig.grools.desktop.gui.javafx;

import java.util.HashSet;
import java.util.Set;

//import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import lombok.Getter;
import lombok.NonNull;

public class Cell extends Pane {

    @Getter
    protected final String    cellId;
    @Getter
    protected final Set<Cell> cellChildren;
    @Getter
    protected final Set<Cell> cellParents;
    @Getter
    protected final Shape     figure;

    public Cell(@NonNull final String id ) {
        this(id, null); // root node do not have a figure
        getStyleClass().add( "root_cell" );
    }

    public Cell(@NonNull final String id, final Shape fig ) {
        cellId      = id;
        figure      = fig;
        cellChildren= new HashSet<>();
        cellParents = new HashSet<>();
        if( figure != null)
            getChildren().add(figure);
    }

    public void addCellChild(@NonNull final Cell cell) {
        cellChildren.add( cell );
    }

    public void addCellParent(@NonNull final Cell cell) {
        cellParents.add( cell );
    }

    public void removeCellChild(@NonNull final Cell cell) {
        cellChildren.remove( cell );
    }
    
    @Override
    public String toString(){
        return cellId;
    }
}