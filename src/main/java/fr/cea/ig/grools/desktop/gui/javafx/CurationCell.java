package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import lombok.NonNull;

public class CurationCell extends Cell {
    public CurationCell( @NonNull String id ) {
        super( id, new Circle( 50) );
        getStyleClass().add("observation_cell");
    }
}
