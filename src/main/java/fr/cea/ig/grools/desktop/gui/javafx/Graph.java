package fr.cea.ig.grools.desktop.gui.javafx;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.NonNull;

public class Graph {

    @Getter
    private final Model model;

    private final Group canvas;

    @NonNull @Getter
    private final ZoomableScrollPane scrollPane;

    private final MouseGestures mouseGestures;

    /**
     * the pane wrapper is necessary or else the scrollpane would always align
     * the top-most and left-most child to the top and left eg when you drag the
     * top child down, the entire scrollpane would move down
     */
    @NonNull @Getter
    private Pane rootLayer;

    public Graph() {
        model           = new Model();
        canvas          = new Group();
        rootLayer       = new Pane();
        mouseGestures   = new MouseGestures(this);
        scrollPane      = new ZoomableScrollPane(canvas);
        canvas.getChildren().add(rootLayer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
    }

    public void beginUpdate() {
    }

    public void endUpdate() {

        // add components to graph pane
        rootLayer.getChildren().addAll(model.getAddedEdges());
        rootLayer.getChildren().addAll(model.getAddedCells());

        // remove components from graph pane
        rootLayer.getChildren().removeAll(model.getRemovedCells());
        rootLayer.getChildren().removeAll(model.getRemovedEdges());

        // enable dragging of cells
        model.getAddedCells( ).forEach( mouseGestures::makeDraggable );

        // every cell must have a parent, if it doesn't, then the graphParent is
        // the parent
        getModel().attachOrphansToGraphParent(model.getAddedCells());

        // remove reference to graphParent
        getModel().disconnectFromGraphParent(model.getRemovedCells());

        // merge added & removed cells with all cells
        getModel().merge();

    }

    public double getScale() {
        return this.scrollPane.getScaleValue();
    }
}