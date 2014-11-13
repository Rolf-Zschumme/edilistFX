package de.vbl.im;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.vbl.im.controller.IMController;


public class IM_Main extends Application {
	
	private static final Logger logger = LogManager.getLogger(IM_Main.class.getName());
	IMController controller;
	
	public static void main(String[] args) {
		Date date = java.util.Calendar.getInstance().getTime();
		SimpleDateFormat dateFormatter =  new SimpleDateFormat("dd.MM.yyyy");
		String dateString = dateFormatter.format(date);		
		logger.info("Java-Version:" + System.getProperty("java.version") + " Datum: " + dateString);
		try {
			launch(args);
		} catch (Exception e) {
			logger.error("Fehler: " + e.getMessage());
		}
	}
	
	private static Stage primaryStage;
	
	@Override
	public void start(Stage stage) {
		primaryStage = stage;
		try {
			Parent root = loadAndStartController();
			Scene scene = new Scene(root); 
			scene.getStylesheets().add(getClass().getResource("view/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			logger.error("Fehler in IM_Main.start()");
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop () {
		logger.info("stopped");
	}

	public Parent loadAndStartController() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("view/IM.fxml"));
		Parent root = null;

		try {
			root = (Parent) loader.load();
		} catch (Exception e) {
			logger.error("Fehler beim Laden der fxml-Resource", e);
		}
		
		controller = loader.getController();
		if (primaryStage == null) {
			primaryStage = new Stage();
		}
		controller.start(primaryStage);
		return root;
	}
	
}
