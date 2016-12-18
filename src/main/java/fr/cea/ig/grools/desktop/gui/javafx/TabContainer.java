package fr.cea.ig.grools.desktop.gui.javafx;


import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public interface TabContainer {
    long    getId( );
    Tab     getTab( );
    TabPane getTabPane( );
    Stage   getStage( );
}
