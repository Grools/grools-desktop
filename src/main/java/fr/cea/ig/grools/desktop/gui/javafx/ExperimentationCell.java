package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import lombok.NonNull;

public class ExperimentationCell extends Cell {
    public ExperimentationCell( @NonNull String id ) {
        super( id, new Ellipse( 50, 50) );
        getStyleClass().add("observation_cell");
    }
}
