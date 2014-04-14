package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: Anbindung
 * 
 */
@Entity 
public class EdiAnbindung {

	private StringProperty name;
	private long id;
	private Collection<EdiSzenario> ediSzenario;
	private String beschreibung;

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

	// ------------------------------------------------------------------------ 
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	// ------------------------------------------------------------------------ 
	@OneToMany(mappedBy = "ediAnbindung")
	public Collection<EdiSzenario> getEdiSzenario() {
		return ediSzenario;
	}

	public void setEdiSzenario(Collection<EdiSzenario> ediSzenario) {
		this.ediSzenario = ediSzenario;
	}

}
