package de.vbl.ediliste;
	

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import de.vbl.ediliste.controller.EdiMainController;


public class EdiListMain extends Application {
	
//	@Override
//	public void start(Stage primaryStage) throws IOException {
//		Scene scene = new Scene(FXMLLoader.<Parent>load(getClass().getResource("view/EdiMain.fxml")));
//		primaryStage.setScene(scene);
//		primaryStage.show();
//	}
//		Handler handler;
//		try {
//			handler = new FileHandler("EDI-List.log");
//			Logger.getLogger("").addHandler(handler);
//		} catch (SecurityException | IOException e) {
//			e.printStackTrace();
//		}

//	private final static Logger LOGGER = Logger.getLogger(EdiListMain.class.getName());
	
	public static void main(String[] args) {
		System.out.println("Java-Version: " + System.getProperty("java.version"));
		launch(args);
//		try {
//			ApplicationLogger.setup(LOGGER);
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("Problem with creating the log files");
//		}
//		try {
//			LOGGER.setLevel(Level.INFO);
//			launch(args);
//		} catch (Exception e) {
//			LOGGER.severe(e.getLocalizedMessage());
//			throw new RuntimeException("Problem by launing the application");
//		}

	}
	
	private static Stage primaryStage;
	
	@Override
	public void start(Stage stage) {
//		LOGGER.info("Start");
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
		} catch (IOException e) {
			e.printStackTrace();
//			LOGGER.severe(e.getLocalizedMessage());
		}
		EdiMainController controller = loader.getController();
		if (primaryStage == null) {
			primaryStage = new Stage();
		}
		controller.start(primaryStage);
		return root;
	}
	
}
