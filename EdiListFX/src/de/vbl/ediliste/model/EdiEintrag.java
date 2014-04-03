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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import de.vbl.ediliste.model.Empfaenger;
import javax.persistence.OneToMany;


@Entity
public class EdiEintrag {
	private long id;
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty kurzBez = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();
	private Collection<Dokument> dokument;
	private Szenario szenario;
	private Komponente senderKomponente;
	private Collection<Empfaenger> empfaenger;
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

	public String getSenderName () {
		return senderKomponente == null ? "" : senderKomponente.getName();
	}
	@ManyToMany
	public Collection<Dokument> getDokument() {
	    return dokument;
	}
	public void setDokument(Collection<Dokument> param) {
	    this.dokument = param;
	}
	@ManyToOne
	public Szenario getSzenario() {
	    return szenario;
	}
	public void setSzenario(Szenario param) {
	    this.szenario = param;
	}
	public void setSender(Komponente param) {
	    this.senderKomponente = param;
	}
	@ManyToOne
	public Komponente getSender() {
	    return senderKomponente;
	}
	@OneToMany(mappedBy = "ediEintrag")
	public Collection<Empfaenger> getEmpfaenger() {
	    return empfaenger;
	}
	public void setEmpfaenger(Collection<Empfaenger> param) {
	    this.empfaenger = param;
	}
	
}

