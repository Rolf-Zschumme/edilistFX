package de.vbl.ediliste.model;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import de.vbl.ediliste.model.EdiEintrag;

/**
 * Entity implementation class for Entity: Empfaenger
 *
 */
@Entity

public class Empfaenger implements Serializable {

	
	private Long id;
	private static final long serialVersionUID = 1L;
	private StringProperty datenart = new SimpleStringProperty();
	private Komponente komponente;
	private EdiEintrag ediEintrag;
	public Empfaenger() {
		super();
	}   
	@Id    
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
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
	public Komponente getKomponente() {
	    return komponente;
	}
	public void setKomponente(Komponente param) {
	    this.komponente = param;
	}
	@ManyToOne
	public EdiEintrag getEdiEintrag() {
	    return ediEintrag;
	}
	public void setEdiEintrag(EdiEintrag param) {
	    this.ediEintrag = param;
	}


}
