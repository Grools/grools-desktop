package fr.cea.ig.grools.desktop.gui.javafx;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;
import lombok.NonNull;

//credit:  Roland http://stackoverflow.com/users/1844265/roland

public class ZoomableScrollPane extends ScrollPane {
    private Group   zoomGroup;
    private Scale   scaleTransform;
    private Node    content;
    private double  scaleValue = 1.0;
    private double  delta = 0.1;

    public ZoomableScrollPane(@NonNull final Node contentNode ) {
        content     = contentNode;
        zoomGroup   = new Group();
        final Group contentGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(content);
        setContent(contentGroup);
        scaleTransform = new Scale(scaleValue, scaleValue, 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);

        zoomGroup.setOnScroll(new ZoomHandler());
        getStyleClass().add( "zoomable_scroll_pane" );
    }

    public double getScaleValue() {
        return scaleValue;
    }

    public void zoomToActual() {
        zoomTo(1.0);
    }

    public void zoomTo(final double value) {
        scaleValue = value;
        scaleTransform.setX(scaleValue);
        scaleTransform.setY(scaleValue);

    }

    public void zoomActual() {
        scaleValue = 1;
        zoomTo(scaleValue);
    }

    public void zoomOut() {
        scaleValue -= delta;
        if (Double.compare(scaleValue, 0.1) < 0)
            scaleValue = 0.1;
        zoomTo(scaleValue);
    }

    public void zoomIn() {
        scaleValue += delta;
        if (Double.compare(scaleValue, 10) > 0)
            scaleValue = 10;
        zoomTo(scaleValue);

    }

    /**
     *
     * @param minimizeOnly
     *            If the content fits already into the viewport, then we don't
     *            zoom if this parameter is true.
     */
    public void zoomToFit(final boolean minimizeOnly) {

        double scaleX = getViewportBounds().getWidth() / getContent().getBoundsInLocal().getWidth();
        double scaleY = getViewportBounds().getHeight() / getContent().getBoundsInLocal().getHeight();

        // consider current scale (in content calculation)
        scaleX *= scaleValue;
        scaleY *= scaleValue;

        // distorted zoom: we don't want it => we search the minimum scale
        // factor and apply it
        double scale = Math.min(scaleX, scaleY);

        // check precondition
        if (minimizeOnly) {

            // check if zoom factor would be an enlargement and if so, just set
            // it to 1
            if (Double.compare(scale, 1) > 0) {
                scale = 1;
            }
        }

        // apply zoom
        zoomTo(scale);

    }

    private class ZoomHandler implements EventHandler<ScrollEvent> {

        @Override
        public void handle(@NonNull final ScrollEvent scrollEvent) {
            scaleValue = (scrollEvent.getDeltaY() < 0)  ? scaleValue - delta
                                                        : scaleValue + delta;
            zoomTo(scaleValue);
            scrollEvent.consume();
        }
    }
}