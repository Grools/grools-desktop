package fr.cea.ig.grools.desktop.gui;


import javafx.fxml.Initializable;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceController implements Initializable {
    
    @NonNull @Getter
    private final Stage preferenceStage;
    
    
    public PreferenceController( @NonNull final Stage preferenceStage ) {
        this.preferenceStage = preferenceStage;
    }
    
    
    @Override
    public void initialize( URL fxmlFileLocation, ResourceBundle resources ) {
        
    }
}
