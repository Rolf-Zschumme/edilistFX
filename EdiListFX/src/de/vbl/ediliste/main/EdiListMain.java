package de.vbl.ediliste.main;
	

import java.io.IOException;

import de.vbl.ediliste.controller.EdiMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class EdiListMain extends Application {
	
//	@Override
//	public void start(Stage primaryStage) throws IOException {
//		Scene scene = new Scene(FXMLLoader.<Parent>load(getClass().getResource("../view/EdiMain.fxml")));
//		primaryStage.setScene(scene);
//		primaryStage.show();
//	}

	
	
	public static void main(String[] args) {
		System.out.println("Java-Version: " + System.getProperty("java.version"));
		launch(args);
	}
	
	private static Stage primaryStage;
	
	@Override
	public void start(Stage stage) {
		primaryStage = stage;

		Parent root = loadAndStartController();
		
		Scene scene = new Scene(root); 
		scene.getStylesheets().add(getClass().getResource("../view/application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	

	public Parent loadAndStartController() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/EdiMain.fxml"));
		Parent root = null;
		try {
			root = (Parent) loader.load();
		} catch (IOException e) {
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
