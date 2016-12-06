package fr.cea.ig.grools.desktop.gui;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;

public class AnalyzeDialogueBoxController {
    
    @NonNull @Getter
    private final Stage dialogBoxStage;
    
    @FXML
    private RadioButton radioButtonFalsehood;
    
    @FXML
    private Label labelHeader;
    
    @FXML
    private CheckBox checkBoxDispensable;
    
    @FXML
    private CheckBox checkBoxSpecific;
    
    @FXML
    private ComboBox<?> comboBoxPriorKnowledgeGraph;
    
    @FXML
    private RadioButton radioButtonNormal;
    
    @FXML
    private Button buttonCancel;
    
    @FXML
    private Button buttonStart;
    
    @FXML
    private Button buttonOpenObservationsFile;
    
    
    private List<File> list;
    
    public AnalyzeDialogueBoxController( @NonNull final Stage dialogBoxStage ) {
        this.dialogBoxStage = dialogBoxStage;
    }
    
    @FXML
    void initialize() {
        assert radioButtonFalsehood != null : "fx:id=\"radioButtonFalsehood\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert labelHeader != null : "fx:id=\"labelHeader\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert checkBoxDispensable != null : "fx:id=\"checkBoxDispensable\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert checkBoxSpecific != null : "fx:id=\"checkBoxSpecific\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert comboBoxPriorKnowledgeGraph != null : "fx:id=\"comboBoxPriorKnowledgeGraph\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert radioButtonNormal != null : "fx:id=\"radioButtonNormal\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert buttonCancel != null : "fx:id=\"buttonCancel\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        assert buttonStart != null : "fx:id=\"buttonStart\" was not injected: check your FXML file 'analyze_dialog_box.fxml'.";
        
        comboBoxPriorKnowledgeGraph.setOnAction( event ->{
            if( comboBoxPriorKnowledgeGraph.getValue() != null && list != null && list.size() > 0 )
                buttonStart.setDisable( false );
            else
                buttonStart.setDisable( true );
        } );
        
        buttonOpenObservationsFile.setOnAction( event -> {
            final FileChooser fileChooser = new FileChooser();
            // Set extension filter
            final FileChooser.ExtensionFilter csvFilter =  new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters()
                       .add(csvFilter);
            fileChooser.setTitle( "Select observations files" );
            list = fileChooser.showOpenMultipleDialog(dialogBoxStage);
            if( comboBoxPriorKnowledgeGraph.getValue() != null && list != null && list.size() > 0 ) {
                list.forEach( this::openFile );
                buttonStart.setDisable( false );
            }
            else
                buttonStart.setDisable( true );
        } );
    
        buttonCancel.setOnAction( event -> dialogBoxStage.close( ) );
    
    }
    
    @Override
    protected void finalize( ){
        dialogBoxStage.close();
    }
    
    private void openFile( @NonNull final File file) {
    
    }
}
