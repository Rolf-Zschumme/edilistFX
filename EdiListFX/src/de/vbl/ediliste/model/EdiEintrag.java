package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;


@Entity
public class EdiEintrag {
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty kurzBez = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();
	private long id;
	private Collection<Dokument> dokument;
	private Szenario szenario;
	private Komponente komponente;
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
	public StringProperty kurzBezProperty() {
		return kurzBez;
	}

	@Column(length = 30)
	public String getKurzBez() {
		return kurzBez.get();
	}

	public void setKurzBez(String param) {
		kurzBez.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty senderNameProperty() {
		return senderName;
	}

//	public void setSenderName(String param) {
//		senderName.set(param);
//	}

//	public String getSenderName () {
//		return senderKomponente == null ? "" : senderKomponente.getName();
//	}
	// ------------------------------------------------------------------------
	@ManyToMany
	public Collection<Dokument> getDokument() {
	    return dokument;
	}
	public void setDokument(Collection<Dokument> param) {
	    this.dokument = param;
	    
	}
	// ------------------------------------------------------------------------
	@ManyToOne
	public Szenario getSzenario() {
	    return szenario;
	}
	public void setSzenario(Szenario param) {
	    this.szenario = param;
	}
	@ManyToOne
	@JoinTable(name = "KOMPONENTE")
	public Komponente getKomponente() {
	    return komponente;
	}
	public void setKomponente(Komponente param) {
	    this.komponente = param;
	}
}

