package de.vbl.ediliste.model;

import java.util.Collection;
import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.DATE;

@Entity
public class EdiEintrag {
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty bezeichnung = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();
	private long id;
	private EdiSzenario ediSzenario;
	private EdiKomponente ediKomponente;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private Collection<EdiDokuLink> ediDokuLink;
	private String beschreibung;
	private Date vonDatum;
	private Date bisDatum; 

	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public IntegerProperty ediNrProperty() {
		return ediNr;
	}

	public Integer getEdiNr() {
		return ediNr.get();
	}

	public void setEdiNr(Integer param) {
		ediNr.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty bezeichnungProperty() {
		return bezeichnung;
	}

	@Column(length = 40)
	public String getBezeichnung() {
		return bezeichnung.get();
	}

	public void setBezeichnung(String param) {
		bezeichnung.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty senderNameProperty() {
		return senderName;
	}

	// public void setSenderName(String param) {
	// senderName.set(param);
	// }

	// ------------------------------------------------------------------------
	@ManyToOne
	public EdiSzenario getSzenario() {
		return ediSzenario;
	}

	public void setSzenario(EdiSzenario param) {
		this.ediSzenario = param;
	}

	@ManyToOne
	public EdiKomponente getKomponente() {
		return ediKomponente;
	}

	public void setKomponente(EdiKomponente param) {
		this.ediKomponente = param;
	}

	@OneToMany(mappedBy = "ediEintrag")
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
		return ediEmpfaenger;
	}

	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
		this.ediEmpfaenger = param;
	}

	@ManyToMany
	public Collection<EdiDokuLink> getEdiDokuLink() {
		return ediDokuLink;
	}

	public void setEdiDokuLink(Collection<EdiDokuLink> param) {
		this.ediDokuLink = param;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	@Temporal(DATE) 
	public Date getVonDatum() {
		return vonDatum;
	}

	public void setVonDatum(Date param) {
		this.vonDatum = param;
	}

	@Temporal(DATE) 
	public Date getBisDatum() {
		return bisDatum;
	}

	public void setBisDatum(Date param) {
		this.bisDatum = param;
	}
}
