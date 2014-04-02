package de.vbl.ediliste.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EdiNrListElement    {
	private LongProperty ediId;
	private IntegerProperty ediNr;
	private StringProperty ediKurzbez;
	
	public EdiNrListElement(Long id, Integer nr, String kurzbez) {
		this.ediId = new SimpleLongProperty(id);
		this.ediNr = new SimpleIntegerProperty(nr);
		this.ediKurzbez = new SimpleStringProperty(kurzbez);
	}
	
	public Long getEdiId() {
		return ediId.getValue();
	}

	public IntegerProperty ediNrProperty() {
		return ediNr;
	}
	
	public StringProperty ediKurzbezProperty() {
		return ediKurzbez;
	}

}