package de.vbl.im.customControl;

import java.io.IOException;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;

public class NumberField extends TextField {
	
	private final DoubleProperty doubleProperty = new SimpleDoubleProperty(0);

	public NumberField() {
		FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/de/vbl/im/customControl/NumberField.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    try {
                        doubleProperty.set(Double.valueOf(newValue));
                    } catch (NumberFormatException ex) {
                    	System.out.println("Fehler");
                    }
                }
        );

        doubleProperty.addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    setText(String.valueOf(newValue));
                }
        );
    }
   
	public DoubleProperty doubleProperty() {
		return doubleProperty;
	}

}
