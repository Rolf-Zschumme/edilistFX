package de.vbl.im.model;


import java.util.Collection;
import java.util.Set;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import static javax.persistence.GenerationType.IDENTITY;


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
	private Set<Integration> integration;
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
		int maxInNr = -1;
		try {
			Query query = em.createQuery("SELECT MAX(s.isNr) FROM InSzenario s");
			maxInNr = (int) query.getSingleResult();
		} catch (Exception e) {
			throw(e);
		}
		return maxInNr; 
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
	public final Set<Integration> getIntegration() {
	    return integration;
	}

	public final void setIntegration(final Set<Integration> param) {
	    this.integration = param;
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
