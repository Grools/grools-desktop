package fr.cea.ig.grools.desktop;

import fr.cea.ig.grools.desktop.gui.ViewController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.NonNull;

import java.awt.*;

public class Main  extends Application {
    
    @Override
    public void start( @NonNull final Stage primaryStage ) {
        final Dimension      screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
        final double         width      = screenSize.getWidth();
        final double         height     = screenSize.getHeight();
        final ViewController controller = new ViewController( primaryStage );
        final Pane           mainPane   = Tools.loadFXML( "main.fxml" , controller );
        final Scene          myScene    = new Scene( mainPane );

        primaryStage.setTitle( "Grools Desktop" );
        primaryStage.setScene( myScene );
        primaryStage.setWidth( width );
        primaryStage.setHeight( height );
        primaryStage.show();
    }
}
