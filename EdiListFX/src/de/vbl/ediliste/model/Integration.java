package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import de.vbl.ediliste.model.DokuLink;

/**
 * Entity implementation class for Entity: Anbindung
 * 
 */
@Entity 
public class Integration {

	private StringProperty name;
	private long id;
	private Collection<Konfiguration> konfiguration;
	private String beschreibung;
	private Collection<Vorhaben> vorhaben;
	private Collection<DokuLink> dokuLink;

	public Integration() {
		name = new SimpleStringProperty();
	}
	
	public Integration(String newName) {
		this();
		name.set(newName);
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

	// ------------------------------------------------------------------------ 
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	// ------------------------------------------------------------------------ 
	@OneToMany(mappedBy = "integration")
	public Collection<Konfiguration> getKonfiguration() {
		return konfiguration;
	}

	public void setKonfiguration(Collection<Konfiguration> konfiguration) {
		this.konfiguration = konfiguration;
	}

	@ManyToMany(mappedBy = "integration")
	public Collection<Vorhaben> getVorhaben() {
	    return vorhaben; 
	}

	public void setVorhaben(Collection<Vorhaben> param) {
	    this.vorhaben = param;
	}

	@ManyToMany
	public Collection<DokuLink> getDokuLink() {
	    return dokuLink;
	}

	public void setDokuLink(Collection<DokuLink> param) {
	    this.dokuLink = param;
	}

//	public boolean equalDokuLinkList (Integration integration2) {
//		Collection dokuLink2 = integration2.dokuLink;
//		return dokuLink.equals(integration2.dokuLink);
//	}
}
