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
    protected final String     cellId;
    protected final Set<Cell>  children;
    protected final Set<Cell>  parents;
    @Getter
    protected final Shape shape;

    public Cell(@NonNull final String id ) {
        this(id, null); // root node do not have a figure
        getStyleClass().add( "root_cell" );
    }

    public Cell(@NonNull final String id, final Shape fig ) {
        cellId      = id;
        shape       = fig;
        children    = new HashSet<>();
        parents     = new HashSet<>();
        //getChildren().add(shape);
    }

    public void addCellChild(@NonNull final Cell cell) {
        children.add(cell);
    }

    public Set<Cell> getCellChildren() {
        return children;
    }

    public void addCellParent(@NonNull final Cell cell) {
        parents.add(cell);
    }

    public Set<Cell> getCellParents() {
        return parents;
    }

    public void removeCellChild(@NonNull final Cell cell) {
        children.remove(cell);
    }
}