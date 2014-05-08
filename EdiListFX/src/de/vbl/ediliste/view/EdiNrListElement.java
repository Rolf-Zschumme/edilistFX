package de.vbl.ediliste.view;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import de.vbl.ediliste.model.EdiEintrag;

public class EdiNrListElement    {
	private LongProperty ediId;
	private StringProperty ediNr;
	private StringProperty bezeichnung;
	
	public EdiNrListElement(long id, Integer nr, String kurzBez) {
		this.ediId = new SimpleLongProperty(id);
		
		String nrStr = Integer.toString(nr);
		while(nrStr.length()<EdiEintrag.EDI_NR_MIN_LEN) 
			nrStr = "0"+ nrStr;

		this.ediNr = new SimpleStringProperty(nrStr);
		this.bezeichnung = new SimpleStringProperty(kurzBez);
	}
	
	public Long getEdiId() {
		return ediId.getValue();
	}

	public StringProperty ediNrProperty() {
		return ediNr;
	}
	
	public StringProperty bezeichnungProperty() {
		return bezeichnung;
	}

}