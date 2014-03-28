package de.vbl.ediliste.main;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class EdiListMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			System.out.println("start1");
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("../view/EdiListe.fxml"));
			System.out.println("start2");
			Scene scene = new Scene(root); 
			System.out.println("start3");
			scene.getStylesheets().add(getClass().getResource("../view/application.css").toExternalForm());
			System.out.println("start4");
			primaryStage.setScene(scene);
			System.out.println("start5");
			primaryStage.show();
			System.out.println("start6");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
