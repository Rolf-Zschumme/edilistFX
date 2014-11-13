package de.vbl.im.model;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import de.vbl.im.model.Konfiguration;
import java.util.Set;
import javax.persistence.OneToMany;
import de.vbl.im.model.DokuLink;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import de.vbl.im.model.Ansprechpartner;
import java.util.Collection;


/**
 * Entity implementation class for Entity: Anbindung
 * 
 */
@Entity  
public class Iszenario {

	private StringProperty name;
	private long id;
	private String beschreibung;
	private Set<Konfiguration> konfiguration;
	private Set<DokuLink> dokuLink;
	private Collection<Ansprechpartner> ansprechpartner;
	// ==================================
	public Iszenario() {
		name = new SimpleStringProperty();
	}
	
	public Iszenario(String newName) {
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

	@OneToMany(mappedBy = "iszenario")
	public Set<Konfiguration> getKonfiguration() {
	    return konfiguration;
	}

	public void setKonfiguration(Set<Konfiguration> param) {
	    this.konfiguration = param;
	}

	@ManyToMany 
	@JoinTable(joinColumns = @JoinColumn(name = "Iszenario_id", referencedColumnName = "ID"))
	public Set<DokuLink> getDokuLink() {
	    return dokuLink;
	}

	public void setDokuLink(Set<DokuLink> param) {
	    this.dokuLink = param;
	}

	@ManyToMany
	public Collection<Ansprechpartner> getAnsprechpartner() {
	    return ansprechpartner;
	}

	public void setAnsprechpartner(Collection<Ansprechpartner> param) {
	    this.ansprechpartner = param;
	}

}
