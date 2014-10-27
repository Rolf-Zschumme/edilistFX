package de.vbl.ediliste;

import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.ediliste.controller.EdiMainController;
import de.vbl.ediliste.controller.subs.KontaktPersonAuswaehlenController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TestKontaktPersonAuswaehlen extends Application {

	@Override
	public void start(Stage primaryStage) {
		EdiMainController mainCtr = new EdiMainController();
		mainCtr.setupEntityManager();
		mainCtr.setPrimaryStage(primaryStage);
		primaryStage.setTitle("Test:KontaktPersonAuswaehlen");
		
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	KontaktPersonAuswaehlenController controller = mainCtr.loadKontaktPersonAuswahl(dialog);
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
