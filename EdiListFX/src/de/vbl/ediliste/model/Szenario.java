package de.vbl.ediliste.model;

import java.io.Serializable;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import de.vbl.ediliste.model.Anbindung;
import javax.persistence.ManyToOne;

/**
 * Entity implementation class for Entity: Szenario
 *
 */
@Entity

public class Szenario implements Serializable {

	private long id;
	private StringProperty name;
	private static final long serialVersionUID = 1L;
	private Anbindung anbindung;
	public Szenario() {
		super();
	}   

	@Id
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
