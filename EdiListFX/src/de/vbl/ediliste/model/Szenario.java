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
public class Szenario {
	private StringProperty name;
	private long id;
	private Anbindung anbindung;
	public Szenario() {
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
	public Anbindung getAnbindung() {
	    return anbindung;
	}

	public void setAnbindung(Anbindung param) {
	    this.anbindung = param;
	}
   
}
