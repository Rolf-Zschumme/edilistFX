package de.vbl.ediliste.model;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Entity implementation class for Entity: Szenario
 *
 */
@Entity
public class EdiSzenario {
	private StringProperty name;
	private long id;
	private EdiAnbindung ediAnbindung;
	public EdiSzenario() {
		super();
	}   

	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   

	// ------------------------------------------------------------------------
	public StringProperty nameProperty() {
		return name;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	@ManyToOne
	public EdiAnbindung getAnbindung() {
	    return ediAnbindung;
	}

	public void setAnbindung(EdiAnbindung param) {
	    this.ediAnbindung = param;
	}
   
}
