package de.vbl.im.test;

import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.IMController;
import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TestAnsprechpartnerAuswaehlen extends Application {

	@Override
	public void start(Stage primaryStage) {
		IMController mainCtr = new IMController();
		mainCtr.setupEntityManager();
		mainCtr.setPrimaryStage(primaryStage);
		primaryStage.setTitle("Test: Ansprechpartner-Auswaehlen");
		
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	AnsprechpartnerAuswaehlenController controller = mainCtr.loadAnsprechpartnerAuswahl(dialog);
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
