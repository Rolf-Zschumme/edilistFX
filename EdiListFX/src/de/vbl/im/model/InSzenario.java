package de.vbl.im.model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;

import de.vbl.im.model.Konfiguration;

import java.util.Set;

import javax.persistence.OneToMany;

import de.vbl.im.model.DokuLink;

import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.GeneratedValue;
import javax.persistence.Query;

import static javax.persistence.GenerationType.IDENTITY;
import de.vbl.im.model.Ansprechpartner;

import java.util.Collection;


/**
 * Entity implementation class for Entity: IntegrationsSzenario
 * 
 */
@Entity  
public class InSzenario {

	private StringProperty name;
	private long id;
	private IntegerProperty isNr;
	private String beschreibung;
	private Set<Konfiguration> konfiguration;
	private Set<DokuLink> dokuLink;
	private Collection<Ansprechpartner> ansprechpartner;
	
	// ========================================================================
	public InSzenario() {
		isNr = new SimpleIntegerProperty();
		name = new SimpleStringProperty();
	}
	
	public InSzenario(final String newName) {
		this();
		name.set(newName);
	}

	// ------------------------------------------------------------------------
	@Id 
	@GeneratedValue(strategy = IDENTITY)
	public final long getId() {
		return this.id;
	}

	public final void setId(final long id) {
		this.id = id;
	}
	
	// ------------------------------------------------------------------------
	public final IntegerProperty isNrProperty() {
		return this.isNr;
	}

	public final String getIsNrStr() {
		return String.format("%03d", this.isNr.get());
	}
	
	public final int getIsNr() {
		return this.isNrProperty().get();
	}

	public final void setIsNr(final int isNr) {
		this.isNrProperty().set(isNr);
	}
	
	public final int getMaxIsNr (final EntityManager em) {
		int maxID=-1;
		try {
			Query query = em.createQuery("SELECT MAX(s.isNr) FROM InSzenario s");
			maxID = (int) query.getSingleResult();
		} catch (Exception e) {
			throw(e);
		}
		return maxID; 
	}

	// ------------------------------------------------------------------------
	public final StringProperty nameProperty() {
		return name;
	}

	public final String getName() {
		return name.get();
	}

	public final void setName(final String param) {
		name.set(param);
	}

	// ------------------------------------------------------------------------ 
	public final String getBeschreibung() {
		return beschreibung;
	}

	public final void setBeschreibung(final String param) {
		this.beschreibung = param;
	}

	// ------------------------------------------------------------------------ 
	@OneToMany(mappedBy = "inSzenario")
	public final Set<Konfiguration> getKonfiguration() {
	    return konfiguration;
	}

	public final void setKonfiguration(final Set<Konfiguration> param) {
	    this.konfiguration = param;
	}

	// ------------------------------------------------------------------------ 
	@ManyToMany 
	@JoinTable
	public final Set<DokuLink> getDokuLink() {
	    return dokuLink;
	}

	public final void setDokuLink(final Set<DokuLink> param) {
	    this.dokuLink = param;
	}

	// ------------------------------------------------------------------------ 
	@ManyToMany
	public final Collection<Ansprechpartner> getAnsprechpartner() {
	    return ansprechpartner;
	}

	public final void setAnsprechpartner(final Collection<Ansprechpartner> param) {
	    this.ansprechpartner = param;
	}
}
