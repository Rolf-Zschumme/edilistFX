package de.vbl.im.test;

import javax.persistence.EntityManager;

import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.IntegrationManagerController;
import de.vbl.im.controller.subs.DokumentAuswaehlenController;
import de.vbl.im.model.DokuLink;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class TestDokumentAuswaehlen extends Application {

	@Override
	public void start(Stage primaryStage) {
		IntegrationManagerController mainCtr = new IntegrationManagerController();
		mainCtr.setupEntityManager();
		EntityManager em = mainCtr.getEntityManager();
		mainCtr.setPrimaryStage(primaryStage);
		Scene scene = new Scene(new Pane());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Test:KontaktPersonAuswaehlen");
		
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	DokumentAuswaehlenController controller = mainCtr.loadDokumentAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		if (controller.getResponse() == Actions.OK) {
    			System.out.println("ok");
    			DokuLink dokuLink = controller.getSelectedDokuLink();
    			System.out.println("Name : " + dokuLink.getName());
    			System.out.println("Datum: " + dokuLink.getDatum());
    			System.out.println("Repo : " + dokuLink.getRepository().getName());
    			em.getTransaction().begin();
    			em.persist(dokuLink);
    			System.out.println("ID  : " + dokuLink.getId());
    			em.getTransaction().commit();
    		}
    	}
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
