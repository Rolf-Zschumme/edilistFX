package de.vbl.ediliste.model;

import java.util.Collection;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.JoinColumn;

import de.vbl.ediliste.model.EdiAnbindung;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: Szenario
 * 
 */ 
@Entity
public class EdiSzenario {
	private StringProperty name;
	private long id;
	private EdiAnbindung ediAnbindung;
	private Collection<EdiEintrag> ediEintrag;
	private String beschreibung;

	public EdiSzenario() {
		super();
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
 
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	// ------------------------------------------------------------------------
	@ManyToOne
	@JoinColumn(referencedColumnName = "id", nullable = false)
	public EdiAnbindung getEdiAnbindung() {
		return ediAnbindung;
	}

	public void setEdiAnbindung(EdiAnbindung param) {
		this.ediAnbindung = param;
	}
 
	// ------------------------------------------------------------------------
	@OneToMany(mappedBy = "ediSzenario")
	public Collection<EdiEintrag> getEdiEintrag() {
		return ediEintrag;
	}

	public void setEdiEintrag(Collection<EdiEintrag> ediEintrag) {
		this.ediEintrag = ediEintrag;
	}

	// ------------------------------------------------------------------------
	public String getBeschreibung() { 
		return beschreibung;
	}
	
	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
	
}
