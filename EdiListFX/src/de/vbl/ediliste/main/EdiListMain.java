package de.vbl.ediliste.main;
	
import java.awt.Button;

import de.vbl.ediliste.controller.EdiListController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;


public class EdiListMain extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("EdiListe");
		
		try {
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			Scene scene = new Scene(rootLayout); 
			scene.getStylesheets().add(getClass().getResource("../view/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		showEdiListe();
	}
	
//	public Stage getPrimaryStage() {
//		return primaryStage;
//	}
	
	public void showEdiListe() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/EdiListe.fxml"));
			AnchorPane ediListView = (AnchorPane) loader.load();
			rootLayout.setCenter(ediListView);
			
			EdiListController controller = loader.getController();
			controller.setStage(primaryStage);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
