package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import lombok.NonNull;

public class ComputationCell extends Cell {
    public ComputationCell( @NonNull String id ) {
        super( id, new Polygon( 0d,25d,25d,0d,25d,50d,50d,25d) );
        getStyleClass().add("observation_cell");
    }
}
