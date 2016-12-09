package fr.cea.ig.grools.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.BuilderFactory;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tools {
    public static final String APP_ID   = "GROOLSDesktop";
    public static final String APP_NAME = "GROOLS Desktop";
    
    public static Path applicationDataDirectoryDirectory( ) {
        final Path applicationPath;
        final String OS = System.getProperty("os.name")
                                .toUpperCase();
        if (OS.contains("WIN")){
            applicationPath = Paths.get( System.getenv( "APPDATA" ), APP_ID );
        }
        else if (OS.contains("MAC"))
            applicationPath = Paths.get( System.getProperty("user.home"), "/Library/", APP_ID );
        else if (OS.contains("NUX"))
            applicationPath= ( System.getenv("XDG_DATA_HOME") == null ) ? Paths.get( System.getProperty("user.home")      , ".local/share", APP_ID )
                                                                               : Paths.get( System.getProperty("XDG_DATA_HOME")  , APP_ID );
        else
            applicationPath = null;
        return applicationPath;
    }
    
    public static Region loadFXML( @NonNull final String fxmlFileName, @NonNull final Object controller ){
        Region content = null;
        try {
            final FXMLLoader loader = new FXMLLoader( Tools.class.getResource( "/fxml/"+fxmlFileName ) );
            loader.setController(controller);
            content = loader.load();
        }
        catch( IOException e ) {
            System.err.println( "Error -> Can not to load fxml files: " + fxmlFileName+  ". Please contact the support. Thank for your understanding.");
            System.exit( 1 );
        }
        return content;
    }
    
    public static String  getCssFile( @NonNull final String cssFileName ){
        return Tools.class.getResource( "/css/"+cssFileName ).toExternalForm();
    }
}
