package de.vbl.ediliste.model;

import java.io.Serializable;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import de.vbl.ediliste.model.Szenario;
import java.util.Collection;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: Anbindung
 *
 */
@Entity

public class Anbindung implements Serializable {

	   
	private Long id;
	private StringProperty name;
	private static final long serialVersionUID = 1L;
	private Collection<Szenario> szenario;
	public Anbindung() {
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

	@OneToMany(mappedBy = "anbindung")
	public Collection<Szenario> getSzenario() {
	    return szenario;
	}

	public void setSzenario(Collection<Szenario> param) {
	    this.szenario = param;
	}
   
}
