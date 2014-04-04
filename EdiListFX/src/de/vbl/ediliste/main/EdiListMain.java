package de.vbl.ediliste.main;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import de.vbl.ediliste.controller.MainController;


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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/Main.fxml"));
			rootLayout = (BorderPane) loader.load();
			MainController controller = loader.getController();
			controller.setStage(primaryStage);
			
			Scene scene = new Scene(rootLayout); 
			scene.getStylesheets().add(getClass().getResource("../view/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
//	public Stage getPrimaryStage() {
//		return primaryStage;
//	}
	
//	public void showEdiListe() {
//		try {
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/EdiListe.fxml"));
//			AnchorPane ediListView = (AnchorPane) loader.load();
//			rootLayout.setCenter(ediListView);
//			
//			xxController controller = loader.getController();
//			controller.setStage(primaryStage);
//			
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
}
