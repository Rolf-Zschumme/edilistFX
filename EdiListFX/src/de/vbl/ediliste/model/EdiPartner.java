package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: EdiPartner
 * 
 */
@Entity
public class EdiPartner {
	private StringProperty name = new SimpleStringProperty();

	private long id;
	private Collection<EdiSystem> ediSystem;

	private String beschreibung;

	public EdiPartner() {
	}

	public EdiPartner(String name) {
		setName(name);
	}

	// ------------------------------------------------------------------------
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

	@Column(unique = true)
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	// ------------------------------------------------------------------------
	@OneToMany(mappedBy = "ediPartner")
	public Collection<EdiSystem> getEdiSystem() {
		return ediSystem;
	}

	public void setEdiSystem(Collection<EdiSystem> param) {
		this.ediSystem = param;
	}

	// ------------------------------------------------------------------------
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	// ------------------------------------------------------------------------
}
