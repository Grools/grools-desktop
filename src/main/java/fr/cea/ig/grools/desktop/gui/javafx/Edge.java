package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NonNull;

public class Edge extends Group {

    @Getter
    protected final Cell source;
    @Getter
    protected final  Cell target;

    protected Line line;

    public Edge( @NonNull final Cell source, @NonNull final Cell target ) {

        this.source = source;
        this.target = target;

        source.addCellParent(target);
        target.addCellChild(source);

        line = new Line();

        line.startXProperty().bind( source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0));
        line.startYProperty().bind( source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0));

        line.endXProperty().bind( target.layoutXProperty().add( target.getBoundsInParent().getWidth() / 2.0));
        line.endYProperty().bind( target.layoutYProperty().add( target.getBoundsInParent().getHeight() / 2.0));

        getChildren().add( line);

    }

}