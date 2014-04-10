package de.vbl.ediliste.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EdiNrListElement    {
	private LongProperty ediId;
	private IntegerProperty ediNr;
	private StringProperty bezeichnung;
	
	public EdiNrListElement(long id, Integer nr, String kurzBez) {
		this.ediId = new SimpleLongProperty(id);
		this.ediNr = new SimpleIntegerProperty(nr);
		this.bezeichnung = new SimpleStringProperty(kurzBez);
	}
	
	public Long getEdiId() {
		return ediId.getValue();
	}

	public IntegerProperty ediNrProperty() {
		return ediNr;
	}
	
	public StringProperty bezeichnungProperty() {
		return bezeichnung;
	}

}