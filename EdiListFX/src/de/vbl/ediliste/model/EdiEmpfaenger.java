package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import de.vbl.ediliste.model.EdiEintrag;

/**
 * Entity implementation class for Entity: Empfaenger
 *
 */
@Entity

public class EdiEmpfaenger {
	private StringProperty datenart = new SimpleStringProperty();
	private long id;
	private EdiKomponente ediKomponente;
	private EdiEintrag ediEintrag;
	@Id    
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public StringProperty datenartProperty() {
		return datenart;
	}
	
	public String getDatenart() {
		return datenart.get();
	}

	public void setDatenart(String param) {
		datenart.set(param);
	}

	@ManyToOne
	public EdiKomponente getKomponente() {
	    return ediKomponente;
	}

	public void setKomponente(EdiKomponente param) {
	    this.ediKomponente = param;
	}

	@ManyToOne
	public EdiEintrag getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(EdiEintrag param) {
	    this.ediEintrag = param;
	}


}
