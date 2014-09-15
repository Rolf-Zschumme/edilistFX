package de.vbl.ediliste;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.vbl.ediliste.controller.EdiMainController;


public class EdiListMain extends Application {
	
	private static final Logger logger = LogManager.getLogger(EdiListMain.class.getName());
	
	public static void main(String[] args) {
		
		logger.info("Java-Version: " + System.getProperty("java.version"));
		try {
			launch(args);
		} catch (Exception e) {
			logger.error("Fehler in der Anwendung", e);
			e.printStackTrace();
		}
	}
	
	private static Stage primaryStage;
	
	@Override
	public void start(Stage stage) {
		primaryStage = stage;
		
		Parent root = loadAndStartController();
		
		Scene scene = new Scene(root); 
		scene.getStylesheets().add(getClass().getResource("view/application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	

	public Parent loadAndStartController() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("view/EdiMain.fxml"));
		Parent root = null;
		try {
			root = (Parent) loader.load();
		} catch (Exception e) {
			logger.error("Fehler beim Laden der fxml-Resource", e);
			e.printStackTrace();
		
		}
		EdiMainController controller = loader.getController();
		if (primaryStage == null) {
			primaryStage = new Stage();
		}
		controller.start(primaryStage);
		return root;
	}
	
}
