package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import lombok.NonNull;

public class ComputationCell extends Cell {
    public ComputationCell( @NonNull String id ) {
        super( id, new Polygon( 0d,5d,5d,0d,5d,10d,10d,5d) );
        getStyleClass().add("observation_cell");
    }
}
