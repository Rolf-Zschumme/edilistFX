package de.vbl.im.tools;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumberField extends TextField {
	/**
     * The DoubleProperty linked with this field
     */
    private final DoubleProperty doubleProperty;
 
    /**
     * Creates a new NumberField that is linked to the given DoubleProperty.
     *
     * @param doubleProperty the DoubleProperty to sync with this field
     */
    public NumberField(DoubleProperty doubleProperty) {
        super(doubleProperty.toString());
        this.doubleProperty = doubleProperty;
 
        /**
         * Add a ChangeListener to this field's textProperty so that the associated DoubleProperty is changed whenever
         * this field's text changes.
         */
        textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    try {
                        doubleProperty.set(Double.valueOf(newValue));
                    } catch (NumberFormatException ex) {
                    }
                });
 
        /**
         * Add a ChangeListener to this field's associated DoubleProperty to change this field's text whenever the
         * associated DoubleProperty is changed.
         */
        doubleProperty.addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    setText(String.valueOf(newValue));
                });
    }
 
    /**
     * Returns the DoubleProperty that is linked with this field.
     *
     * @return this field's DoubleProperty
     */
    public DoubleProperty doubleProperty() {
        return doubleProperty;
    }
}
