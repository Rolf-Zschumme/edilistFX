package de.vbl.im.test;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import de.vbl.im.controller.IntegrationController;
import de.vbl.im.controller.EdiKomponenteController;
import de.vbl.im.controller.EdiPartnerController;
import de.vbl.im.controller.EdiSystemController;
import de.vbl.im.controller.IMController;

public class TestEdiSystem extends Application {

	@Override
	public void start(Stage primaryStage) {
		String src;
		src = EdiKomponenteController.class.getName();
		src = EdiPartnerController.class.getName();
		src = EdiSystemController.class.getName();
		src = IntegrationController.class.getName();
		src = IMController.class.getName();
		int lastPkt = src.lastIndexOf(".");
		src = src.substring(lastPkt + 1).replace("Controller", "");
		System.out.println(src);
		
		URL url = getClass().getResource("../view/" + src + ".fxml");
		System.out.println("url:" + url);
		FXMLLoader loader = new FXMLLoader(url);
		if (loader.getLocation() == null) {
			System.out.println("loader.getLocation() ist NULL");
		}
		Parent root = null;
		try {
			root = (Parent) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		EdiSystemController controller = loader.getController();
//		controller.setParent();
		
		Scene scene = new Scene (root);
		scene.getStylesheets().add(getClass().getResource("../view/application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
