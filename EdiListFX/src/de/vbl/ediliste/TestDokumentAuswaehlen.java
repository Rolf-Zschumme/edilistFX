package de.vbl.ediliste;

import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.ediliste.controller.EdiMainController;
import de.vbl.ediliste.controller.subs.DokumentAuswaehlenController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TestDokumentAuswaehlen extends Application {

	@Override
	public void start(Stage primaryStage) {

		
		EdiMainController mainCtr = new EdiMainController();
		mainCtr.setupEntityManager();
		mainCtr.start(primaryStage);
		
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	DokumentAuswaehlenController controller = mainCtr.loadDokumentAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		if (controller.getResponse() == Actions.OK) {
    			System.out.println("ok");
    		}
    	}
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
