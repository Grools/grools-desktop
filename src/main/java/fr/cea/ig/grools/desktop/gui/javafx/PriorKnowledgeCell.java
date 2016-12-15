package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.shape.Rectangle;
import lombok.NonNull;

public class PriorKnowledgeCell extends Cell {
    public PriorKnowledgeCell( @NonNull String id ) {
        super( id, new Rectangle( 50, 50) );
        getStyleClass().add("prior-knowledge_cell");
    }
}